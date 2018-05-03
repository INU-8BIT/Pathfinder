package com.inu8bit.pathfinder;


import android.content.Context;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.Map;

public class BusActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    GestureDetector detector;
    private GPSInfo gpsInfo;
    private DataAPI dataAPI;
    private TTSManager ttsManager;
    private double lat, lon;
    Map<Integer, String[]> list;

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus);
        detector = new GestureDetector(this);
        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(1000);

        ttsManager = new TTSManager();
        ttsManager.init(this);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                ttsManager.initQueue("주변 정류장 검색페이지입니다.");
                ttsManager.addQueue("검색을 시작하려면 왼쪽으로 스와이프를.");
                ttsManager.addQueue("기존에 저장된 정류장을 보시려면 오른쪽으로 스와이프를 하세요.");
            }
        }, 1000);

    }


    // TODO: Make Gesture as a class so that we can use it more convenient by inserting it
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return detector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        //Toast.makeText(getApplicationContext(), "OnDown Gesture", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        //Toast.makeText(getApplicationContext(), "Fling Gesture", Toast.LENGTH_SHORT).show();
        try {
            ttsManager.stop();
            //if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
            //    return false;

            // right to left swipe
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                //Toast.makeText(getApplicationContext(), getLocalClassName() + "Left Swipe", Toast.LENGTH_SHORT).show();

                ttsManager.initQueue("근처 정류장을 탐색하겠습니다.");

                gpsInfo = new GPSInfo(this);
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
                        //ttsManager.addQueue("정류장 번호 " + entry.getKey());
                        i++;
                        //Log.d("Route: " , entry.getKey() + "/" + entry.getValue());

                    }

                    ttsManager.addQueue("입니다. ");
                } catch (Exception e){
                    Log.e("Error", "Exception happened: " + e.getMessage());
                }
            }
            // left to right swipe
            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                //Toast.makeText(getApplicationContext(), getLocalClassName() + "Right Swipe", Toast.LENGTH_SHORT).show();

            }
            // down to up swipe
            else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                Toast.makeText(getApplicationContext(), getLocalClassName() + "Swipe up", Toast.LENGTH_SHORT).show();
                finish();
            }
            // up to down swipe
            else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                Toast.makeText(getApplicationContext(), getLocalClassName() + "Swipe down", Toast.LENGTH_SHORT).show();
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
