package com.inu8bit.pathfinder;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private GPSInfo gpsInfo;
    private DataAPI dataAPI;
    private GoogleAPI googleAPI;
    private BluetoothService bluetoothService;

    //private Navigation navi;

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


    }
}
