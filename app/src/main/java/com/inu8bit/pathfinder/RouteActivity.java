package com.inu8bit.pathfinder;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RouteActivity extends AppCompatActivity {

    private GoogleAPI googleAPI;
    private TTSManager ttsManager;
    private ImageView imageView;
    List<Route> routes;
    Iterator<Route> nextRoute;
    String agencyNumber;

    String start="호구포역", end="서울대입구";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        ttsManager = new TTSManager();
        ttsManager.init(this);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Log.d("ttsManager","Initialized");
                ttsManager.initQueue("경로 안내 페이지입니다");
                ttsManager.addQueue("위쪽 출발지");
                ttsManager.addQueue("아래쪽 도착지");

            }
        }, 1000);

        imageView = findViewById(R.id.imageView);
        imageView.setOnTouchListener(new SwipeListener(getApplicationContext()){
            @Override
            public void onTapUp(){
                // no current route or not initialized.
                if(routes == null || routes.isEmpty())  return;

                // speak if the next exists.
                if(nextRoute.hasNext()){
                    Route currRoute = nextRoute.next();
                    switch(currRoute.travel_mode){
                        case "WALKING":
                            ttsManager.initQueue("이번에는 " + currRoute.instruction + "입니다");
                            ttsManager.addQueue("이동거리는 " + currRoute.length + "입니다");
                            agencyNumber = null;
                            break;
                        case "TRANSIT":
                            ttsManager.initQueue("이번에는 " + currRoute.instruction + "입니다" );
                            ttsManager.addQueue(currRoute.agency + " " + currRoute.method + "를 탑승하세요.");
                            ttsManager.addQueue("하차역은 " + currRoute.destination + "입니다. ");
                            ttsManager.addQueue(currRoute.length + "개의 정류장을 지나 ");
                            ttsManager.addQueue("약 " + currRoute.arrival_time + "에 도착 예정입니다.");
                            if(currRoute.agencyNumber != null) {
                                ttsManager.addQueue("운수회사로 전화를 거시려면 화면을 길게 누르세요.");
                                agencyNumber = currRoute.agencyNumber;
                                //startActivity(new Intent("android.intent.action.CALL", Uri.parse("010-9049-0841")));
                                call("010-9049-0941");
                            }
                            else {
                                agencyNumber = null;
                            }

                            break;
                    }

                    if(!nextRoute.hasNext()){
                        ttsManager.initQueue("목적지 부근입니다");
                    }
                }
            }
            @Override
            public void onLongTouch(){
                if(agencyNumber != null){
                    for (int i = 0; i < 2; i++) {
                        try {
                            if (Build.VERSION.SDK_INT >= 23 &&
                                    ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                // ask for permission
                                ActivityCompat.requestPermissions(RouteActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                            }

                            break;
                        } catch (SecurityException e){
                            // SecurityException when permission is not granted
                            ttsManager.initQueue("전화 권한이 필요합니다.");
                            e.getStackTrace();
                        }
                    }
                }
            }
            @Override
            public void onRight(){
                ttsManager.initQueue("경로 탐색 중입니다");
                googleAPI = new GoogleAPI();
                try {
                    routes = googleAPI.getTransitRoute(start, end);
                    ttsManager.initQueue(start + "부터 " + end + "까지 경로를 검색하였습니다.");
                    nextRoute = routes.iterator();

                } catch (Exception e) {
                    Log.e("Error", "Exception happened: " + e.getMessage());
                }
            }

            public void onTop(){
                ttsManager.initQueue("출발지를 말씀하세요");
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());           // Call Package
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");                     // Set Language
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "출발지");                       // Prompt Message
                startActivityForResult(i, 0);
            }
            public void onBottom(){
                ttsManager.initQueue("도착지를 말씀하세요");
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());           // Call Package
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");                     // Set Language
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "도착지");                       // Prompt Message
                startActivityForResult(i, 1);                                        // Run Google Voice Recognition
            }
        });
    }

    // TODO: Make this as a class and get return value via Listener
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            List<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            switch (requestCode) {
                case 0:
                    Log.d("Start: ", matches.get(0));
                    start = matches.get(0);
                    break;
                case 1:
                    Log.d("Destination: ", matches.get(0));
                    end = matches.get(0);
                    break;
                case 2:
                    break;
            }
        } catch (Exception e){
            // When Voice Recognition View is stopped or user touched outside the box
            super.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    protected void call(String str){
        startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + "010-9049-0841")));
    }
    @Override
    protected void onPause(){
        ttsManager.stop();
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        ttsManager.stop();
        ttsManager.shutDown();
        super.onDestroy();
    }
}
