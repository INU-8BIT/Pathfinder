package com.inu8bit.pathfinder;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    private GPSInfo gpsInfo;
    private DataAPI dataAPI;
    private GoogleAPI googleAPI;
    private BluetoothService bluetoothService;

    //private Navigation navi;

    GestureDetector detector;

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        // TODO: LeScanCallback and LeStanStart were deprecated as of when Lollipop launched. Find replacement.
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d("BLUETOOTH", "BLE device found: " + device.getName() + "; MAC " + device.getAddress());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gpsInfo = new GPSInfo(MainActivity.this);
        gpsInfo.getCurrentLocation();

        bluetoothService = new BluetoothService(MainActivity.this);
        bluetoothService.start();
        bluetoothService.stop();

        double lat = gpsInfo.getLatitude();
        double lon = gpsInfo.getLongitude();

        dataAPI = new DataAPI();
        try {
            List<String> busStopLists = dataAPI.getNearbyBusStop(lat, lon);
            for(String b: busStopLists){
                Log.d("Bus Stop:", b);
            }
        } catch (Exception e){
            Log.d("Error", "Exception happened: " + e.getMessage());
        }

        googleAPI = new GoogleAPI();
        try {
            List<String> route = googleAPI.getTransitRoute("인천대입구", "호구포역");
            for(String r: route){
                Log.d("Route", r);
            }
        } catch (Exception e){
            Log.d("Error", "Exception happened: " + e.getMessage());
        }

        /*navi.setCoord(37.570841, 126.985302, 37.551135, 126.988205);
        List<String> res = navi.findWalkPath();

        StringBuilder sb = new StringBuilder();
        for (String s : res){
            sb.append(s);
            sb.append("\t");
        }
        tv.setText(sb);
        */
        detector = new GestureDetector(this);
    }

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
            //if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
            //    return false;

            // right to left swipe
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                Toast.makeText(getApplicationContext(), "Left Swipe", Toast.LENGTH_SHORT).show();
                Intent RFIDIntent = new Intent(getApplicationContext(), RFIDActivity.class);
                startActivity(RFIDIntent);
            }
            // left to right swipe
            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                Toast.makeText(getApplicationContext(), "Right Swipe", Toast.LENGTH_SHORT).show();
                Intent RouteIntent = new Intent(getApplicationContext(), RouteActivity.class);
                startActivity(RouteIntent);
            }
            // down to up swipe
            else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                Toast.makeText(getApplicationContext(), "Swipe up", Toast.LENGTH_SHORT).show();
                Intent InfoIntent = new Intent(getApplicationContext(), InfoActivity.class);
                startActivity(InfoIntent);
            }
            // up to down swipe
            else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                Toast.makeText(getApplicationContext(), "Swipe down", Toast.LENGTH_SHORT).show();
                Intent BusIntent = new Intent(getApplicationContext(), BusActivity.class);
                startActivity(BusIntent);
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

}
