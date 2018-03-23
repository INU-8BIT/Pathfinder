package com.inu8bit.pathfinder;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by fenslett on 3/23/2018.
 * GPS Module
 */

public class GPSInfo extends Service implements LocationListener {

    private final Context mContext;

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean gpsStatus = false;

    Location location;
    double lat;         // latitude
    double lon;         // longitude

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // minimum distance to update
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;  // millisecond

    protected LocationManager locationManager;

    public GPSInfo(Context context) {
        this.mContext = context;
    }

    public void getCurrentLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // GPS is unavailable and phone is not connected to the network
            } else {

                // request for location permission.
                if (Build.VERSION.SDK_INT >= 23 &&
                        ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    // ask for permission
                    ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }

                String[] providers = {LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER};


                /*
                Wi-Fi Positioning System(WPS) is more precise and faster.
                So, this module retrieves location with WPS at first, then with GPS if couldn't.
                 */
                for (String provider: providers){
                    locationManager.requestLocationUpdates(
                            provider,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    // there's no last known location
                    location = locationManager.getLastKnownLocation(provider);
                    lat = location.getLatitude();
                    lon = location.getLongitude();

                    if (location != null) {
                        gpsStatus = true;
                        break;
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //return location;
    }

    public void stopGPS(){
        locationManager.removeUpdates(GPSInfo.this);
    }

    public double getLatitude(){
        return lat;
    }
    public double getLongitude(){
        return lon;
    }


    public boolean getGpsStatus() {
        return this.gpsStatus;
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }
}
