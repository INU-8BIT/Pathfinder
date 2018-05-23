package com.inu8bit.pathfinder;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.util.List;

public class RouteActivity extends AppCompatActivity {

    private GoogleAPI googleAPI;
    private TTSManager ttsManager;
    private ImageView imageView;
    private List<Route> routes;
    private int remainedRoutes;
    private String agencyNumber;

    private String start = "", end = "";
    private String inputStart = "", inputEnd = "";

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
                ttsManager.addQueue("위로 스와이프: 출발지");
                ttsManager.addQueue("아래로 스와이프: 도착지");
                ttsManager.addQueue("오른쪽으로 스와이프: 경로 검색 시작");
            }
        }, 1000);

        imageView = findViewById(R.id.imageView);
        imageView.setOnTouchListener(new SwipeListener(getApplicationContext()){
            @Override
            public void onTapUp(){
                // no current route or not initialized.
                if(routes == null || routes.isEmpty())  return;
                if(remainedRoutes - routes.size() >= 0){
                    Route route = routes.get(remainedRoutes - routes.size());
                    switch(route.travel_mode){
                        case "WALKING":
                            ttsManager.initQueue("이번에는 " + route.instruction + "입니다");
                            ttsManager.addQueue("이동거리는 " + route.length + "입니다");
                            agencyNumber = null;
                            break;
                        case "TRANSIT":
                            ttsManager.initQueue("이번에는 " + route.instruction + "입니다" );
                            if(route.method.contains("의정부경전철")){
                                ttsManager.addQueue("인천 1호선을 탑승하세요");
                            }
                            else {
                                ttsManager.addQueue(route.agency + " " + route.method + "를 탑승하세요.");
                            }
                            ttsManager.addQueue("하차역은 " + route.destination + "입니다. ");
                            ttsManager.addQueue(route.length + "개의 정류장을 지나 ");
                            ttsManager.addQueue("약 " + route.arrival_time + "에 도착 예정입니다.");
                            if(route.agencyNumber != null) {
                                ttsManager.addQueue("운수회사로 전화를 거시려면 화면을 길게 누르세요.");
                                agencyNumber = route.agencyNumber;
                            }
                            else {
                                agencyNumber = null;
                            }
                            break;
                    }
                    remainedRoutes++;
                }

                else {
                    try {
                        routes.clear();
                    } catch (Exception e){

                    }
                }
            }

            @Override
            public void onLongTouch(){
                if(agencyNumber != null){
                    for (int i = 0; i < 2; i++) {
                        try {
                            CallPhone.requestPermission(getApplicationContext());
                            startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + agencyNumber)));
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
                if(inputStart == "" && inputEnd == "") {
                    ttsManager.initQueue("출발지나 도착지가 입력되지 않았습니다");
                    return;
                }

                ttsManager.initQueue("경로 탐색 중입니다");

                if(inputStart == "" | inputStart.contains("여기") | inputStart.contains("이곳") | inputStart.contains("이 곳")) {
                    GPSInfo gpsinfo = new GPSInfo(RouteActivity.this);
                    gpsinfo.getCurrentLocation();
                    double lat = gpsinfo.getLatitude();
                    double lon = gpsinfo.getLongitude();
                    start = String.valueOf(lat) + "," + String.valueOf(lon);
                    inputStart = "이곳에서";
                }
                else {
                    start = inputStart;
                }

                end = inputEnd;
                googleAPI = new GoogleAPI();
                try {
                    routes = googleAPI.getTransitRoute(start, end);
                    ttsManager.addQueue(inputStart + "부터 " + inputEnd + "까지 경로를 검색하였습니다.");
                    ttsManager.addQueue("안내를 받으시려면 화면을 터치하세요.");
                    remainedRoutes = routes.size();

                } catch (Exception e) {
                    Log.e("Error", "Exception happened: " + e.getMessage());
                    ttsManager.addQueue("경로가 존재하지 않습니다.");
                }
            }

            public void onTop(){
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());           // Call Package
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");                     // Set Language
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "출발지");                       // Prompt Message
                startActivityForResult(i, 0);
                if(routes != null && !routes.isEmpty())
                    routes.clear();
            }
            public void onBottom(){
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());           // Call Package
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");                     // Set Language
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "도착지");                       // Prompt Message
                startActivityForResult(i, 1);                                        // Run Google Voice Recognition
                if(routes != null && !routes.isEmpty())
                    routes.clear();
            }

            @Override
            public void onLeft(){
                RouteActivity.super.onBackPressed();
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
                    inputStart = matches.get(0);
                    break;
                case 1:
                    Log.d("Destination: ", matches.get(0));
                    inputEnd = matches.get(0);
                    break;
                case 2:
                    break;
            }
        } catch (Exception e){
            // When Voice Recognition View is stopped or user touched outside the box
            ttsManager.initQueue("입력이 취소되었습니다");
            super.onActivityResult(requestCode, resultCode, data);

        }
        super.onActivityResult(requestCode, resultCode, data);
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
