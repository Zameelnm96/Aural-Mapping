package com.example.semester4project;

import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

class Helper extends TimerTask
{
    public static int i = 0;
    public void run()
    {
        if (MapActivity.obj.currentLoc != null) {
            double latitude = MapActivity.obj.currentLoc.getLatitude();
            double longitude = MapActivity.obj.currentLoc.getLongitude();
            Toast.makeText(MapActivity.obj, "Lati - " + latitude + " Long - " + longitude, Toast.LENGTH_SHORT).show();
        }
    }

}