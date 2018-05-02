package com.inu8bit.pathfinder;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Bluetooth Service
 */

public class BluetoothService {
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        // TODO: LeScanCallback and LeStanStart were deprecated as of when Lollipop launched. Find replacement.
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d("BLUETOOTH", "BLE device found: " + device.getName() + "; MAC " + device.getAddress() + "; RSSI " + String.valueOf(rssi));
        }
    };

    Activity mActivity;

    BluetoothService(Activity _mActivity) {
        mActivity = _mActivity;
        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(enableBtIntent, 2);
        }
    }

    public void start(){
        if (mActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // BLE supported
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
        else {
            // TODO: What if BLE not supported?
            // BLE not support
        }
    }

    public void stop(){
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

}
