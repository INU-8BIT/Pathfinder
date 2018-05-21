package com.inu8bit.pathfinder;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.content.Context;

/**
 * Make a phone call
 */

public class CallPhone {
    static public void requestPermission(Context context) throws SecurityException{
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // ask for permission

            ActivityCompat.requestPermissions((Activity)context, new String[]{Manifest.permission.CALL_PHONE}, 1);
        }
    }
}
