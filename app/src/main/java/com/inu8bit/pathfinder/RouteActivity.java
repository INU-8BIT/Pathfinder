package com.inu8bit.pathfinder;

import android.content.Intent;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

public class RouteActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    GestureDetector detector;

    private GoogleAPI googleAPI;
    private TTSManager ttsManager;
    String start, end;


    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        detector = new GestureDetector(this);

        ttsManager = new TTSManager();
        ttsManager.init(this);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                ttsManager.initQueue("경로 안내 페이지입니다");
                ttsManager.addQueue("출발지는 왼쪽으로, ");
                ttsManager.addQueue("도착지는 오른쪽으로 스와이프 하세요.");
            }
        }, 1000);

    }

    // TODO: Make this as a class and get return value via Listener
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        List<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        switch(requestCode){
            case 0:
                Log.d("Start: ", matches.get(0));
                start = matches.get(0);
                break;
            case 1:
                Log.d("Destination: ", matches.get(0));
                end = matches.get(0);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    // TODO: Make Gesture as a class so that we can use it more convenient by inserting it
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return detector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        ttsManager.stop();
        try {
            // right to left swipe
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                //Toast.makeText(getApplicationContext(), "Left Swipe", Toast.LENGTH_SHORT).show();
                // TODO: Make this as a class and get return value via Listener
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);                // Create Intent
                i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());           // Call Package
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");                     // Set Language
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "출발지");                       // Prompt Message
                ttsManager.initQueue("출발지를 말씀하세요");
                startActivityForResult(i, 0);
            }
            // left to right swipe
            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                //Toast.makeText(getApplicationContext(), "Right Swipe", Toast.LENGTH_SHORT).show();
                Intent j = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);                // Create Intent
                j.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());           // Call Package
                j.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");                     // Set Language
                j.putExtra(RecognizerIntent.EXTRA_PROMPT, "도착지");                       // Prompt Message
                ttsManager.initQueue("도착지를 말씀하세요");
                startActivityForResult(j, 1);                                        // Run Google Voice Recognition
            }
            // down to up swipe
            else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                //Toast.makeText(getApplicationContext(), "Swipe up", Toast.LENGTH_SHORT).show();
                ttsManager.initQueue("경로 탐색 중입니다");
                googleAPI = new GoogleAPI();
                try {
                    List<String> route = googleAPI.getTransitRoute(start, end);
                    ttsManager.initQueue(start + "부터 " + end + "까지 경로 안내를 시작하겠습니다");
                    ttsManager.initQueue(route.toString());
                } catch (Exception e){
                    Log.e("Error", "Exception happened: " + e.getMessage());
                }
            }
            // up to down swipe
            else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                //Toast.makeText(getApplicationContext(), "Swipe down", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {

        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        //Toast.makeText(getApplicationContext(), "Long Press Gesture", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        //Toast.makeText(getApplicationContext(), "Scroll Gesture", Toast.LENGTH_LONG).show();
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        //Toast.makeText(getApplicationContext(), "Show Press gesture", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        //Toast.makeText(getApplicationContext(), "Single Tap Gesture", Toast.LENGTH_SHORT).show();
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ttsManager.stop();
        ttsManager.shutDown();
    }

}
