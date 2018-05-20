package com.inu8bit.pathfinder;


import android.content.Context;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class BusActivity extends AppCompatActivity {

    private GPSInfo gpsInfo;
    private DataAPI dataAPI;
    private TTSManager ttsManager;
    private double lat, lon;
    Map<Integer, String[]> list;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus);
        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(1000);

        ttsManager = new TTSManager();
        ttsManager.init(this);

        gpsInfo = new GPSInfo(getApplicationContext());

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                ttsManager.initQueue("주변 정류장 검색페이지입니다.");
                ttsManager.addQueue("위쪽, 주변 탐색");
                ttsManager.addQueue("아래쪽, ");
            }
        }, 1000);

        imageView = findViewById(R.id.imageView);
        imageView = findViewById(R.id.imageView);
        imageView.setOnTouchListener(new SwipeListener(getApplicationContext()){
            @Override
            public void onTop(){
                ttsManager.initQueue("근처 정류장을 탐색하겠습니다.");
                gpsInfo.getCurrentLocation();

                lat = gpsInfo.getLatitude();
                lon = gpsInfo.getLongitude();

                dataAPI = new DataAPI();
                try {
                    list = dataAPI.getNearbyBusStop(lat, lon);
                    ttsManager.addQueue(Integer.toString(list.size()) + "개의 정류장이 검색되었습니다");
                    ttsManager.addQueue("가장 가까운 정류장 2개소의 이름은 ");
                    int i = 1;
                    for (Map.Entry<Integer, String[]> entry : list.entrySet()){
                        if (i >= 3)
                            break;
                        ttsManager.addQueue(entry.getValue()[1] + ", ");
                        i++;
                    }
                    ttsManager.addQueue("입니다. ");
                } catch (InterruptedException | ExecutionException | JSONException e){
                    // TODO: Differenciate Exception
                    Log.e("Error", "Exception happened: " + e.getMessage());
                }
            }

            @Override
            public void onBottom(){
                // TODO: Retreive from database
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
