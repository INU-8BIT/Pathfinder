package com.inu8bit.pathfinder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private GPSInfo gpsInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gpsInfo = new GPSInfo(MainActivity.this);
        gpsInfo.getCurrentLocation();

        double lat = gpsInfo.getLatitude();
        double loc = gpsInfo.getLongitude();

        Toast.makeText(
                getApplicationContext(),
                "lat: " + String.valueOf(lat) + ", Loc: " + String.valueOf(loc),
                Toast.LENGTH_SHORT).show();

    }
}
