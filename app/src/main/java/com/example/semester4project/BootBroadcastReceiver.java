package com.example.semester4project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    private static final String TAG = "RestartServiceReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive");
        // BOOT_COMPLETED” start Service
        if (intent.getAction().equals(ACTION)) {
            //Service
            Intent serviceIntent = new Intent(context, GPS_Service.class);
            context.startService(serviceIntent);
            Log.i(TAG, "onReceive");
        }
    }
}