package com.inu8bit.pathfinder;

import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

public class InfoActivity extends AppCompatActivity {

    private TTSManager ttsManager;
    private ImageView imageView;
    private GoogleAPI googleAPI;
    private List<String> places;
    private GPSInfo gpsInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        ttsManager = new TTSManager();
        ttsManager.init(this);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                ttsManager.initQueue("주번 건물 정보 페이지입니다.");
                ttsManager.addQueue("화면을 터치하면 검색을 시작합니다.");
            }
        }, 1000);

        imageView = findViewById(R.id.imageView);
        imageView.setOnTouchListener(new SwipeListener (getApplicationContext()){
            @Override
            public void onLeft(){
                InfoActivity.super.onBackPressed();
            }

            @Override
            public void onTapUp(){
                // TODO: beacon
                ttsManager.initQueue("주변 건물 탐색 중입니다.");
                googleAPI = new GoogleAPI();
                try {
                    gpsInfo = new GPSInfo(getApplicationContext());
                    gpsInfo.getCurrentLocation();
                    places = googleAPI.getNearbyPlace(gpsInfo.getLatitude(), gpsInfo.getLongitude());
                } catch (Exception e){
                    Log.e("Error", "Exception happened: " + e.getMessage());
                }

                ttsManager.addQueue("총" + Math.min(places.size(), 3) + "개의 건물이 검색되었습니다");
                int i = 1;
                for (String place: places) {
                    if(i > 3) break;
                    ttsManager.addQueue(i++ + "번째 " + place);
                }
                ttsManager.addQueue("입니다");
            }
        });
    }

    @Override
    protected void onPause(){
        ttsManager.stop();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ttsManager.stop();
        ttsManager.shutDown();
    }

}
