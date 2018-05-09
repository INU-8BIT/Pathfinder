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
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

public class RouteActivity extends AppCompatActivity {

    private GoogleAPI googleAPI;
    private TTSManager ttsManager;
    private ImageView imageView;

    String start, end;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

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

        imageView = findViewById(R.id.imageView);
        imageView.setOnTouchListener(new SwipeListener (getApplicationContext()){
            @Override
            public void onLeft(){
                Intent RFIDIntent = new Intent(getApplicationContext(), RFIDActivity.class);
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);                // Create Intent
                i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());           // Call Package
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");                     // Set Language
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "출발지");                       // Prompt Message
                ttsManager.initQueue("출발지를 말씀하세요");
                startActivityForResult(i, 0);

            }

            public void onRight(){
                Intent j = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);                // Create Intent
                j.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());           // Call Package
                j.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");                     // Set Language
                j.putExtra(RecognizerIntent.EXTRA_PROMPT, "도착지");                       // Prompt Message
                ttsManager.initQueue("도착지를 말씀하세요");
                startActivityForResult(j, 1);                                        // Run Google Voice Recognition
            }

            public void onTop(){
            }

            public void onBottom(){
            }
        });

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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ttsManager.stop();
        ttsManager.shutDown();
    }

}
