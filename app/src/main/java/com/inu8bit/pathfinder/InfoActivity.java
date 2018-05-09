package com.inu8bit.pathfinder;

import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

public class InfoActivity extends AppCompatActivity {

    private TTSManager ttsManager = null;
    private ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        ttsManager = new TTSManager();
        ttsManager.init(this);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                ttsManager.initQueue("전방 정보 검색페이지입니다.");
                ttsManager.addQueue("RECO 단말기가 검색되었습니다");
                ttsManager.addQueue("현재 위치는 인천대 입구역입니다.");
                ttsManager.addQueue("다음 열차는 약 8분 뒤 도착 예정입니다.");
            }
        }, 1000);

        imageView.findViewById(R.id.imageView);
        imageView.setOnTouchListener(new SwipeListener (getApplicationContext()){
            @Override
            public void onLeft(){
            }

            public void onRight(){
            }

            public void onTop(){
            }

            public void onBottom(){

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ttsManager.stop();
        ttsManager.shutDown();
    }

}
