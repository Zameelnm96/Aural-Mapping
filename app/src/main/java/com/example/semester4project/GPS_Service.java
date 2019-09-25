package com.example.semester4project;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.semester4project.MyApplication;

/**
 * Created by filipp on 6/16/2016.
 */
public class GPS_Service extends Service {

    private LocationListener listener;
    private LocationManager locationManager;
    public static final double sensor1Lati = 7.095764;
    public static final double sensor2Longi = 80.111980;
    public static int dangerZoneRadSensor1 = MapActivity.dangerZoneRadSensor1 ;
    private static final int NOTIFICATION_ID = 101;


    DatabaseReference  databaseReference;

    Service m_service;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {

        databaseReference = FirebaseDatabase.getInstance().getReference("Datas");

        //this is only for get value of dangerZoneRadSensor1.
        // below again addValueEventLister added
        databaseReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dangerZoneRadSensor1 = ((Long)dataSnapshot.child("Sensor1").child("radius").getValue()).intValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Log.i("GPS_Service", "onCreateMethod running");
        if (listener != null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

            //noinspection MissingPermission
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, listener);// So our on location change method will run every 3 second because of
            // we made min distance is zero.


        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            //noinspection MissingPermission
            locationManager.removeUpdates(listener);
        }
        sendBroadcast(new Intent("YouWillNeverKillMe"));
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, final int flags, int startId) {

        Log.i("GPS_Service", "onStartCommand");


        //startForeground(NOTIFICATION_ID, notification);
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) { // this will run every 3 second because we made minimum distance as 0m
                Intent i = new Intent("location_update");
                i.putExtra("coordinates",location.getLongitude()+" "+location.getLatitude());
                sendBroadcast(i);
                Log.i("GPS_Service", "onLocationChanged: " + "coordinates"+location.getLongitude()+" "+location.getLatitude());
                double distance =  DistanceCalculator.distance(sensor1Lati,location.getLatitude(),sensor2Longi,+location.getLongitude(),0,0);
                if (distance <= dangerZoneRadSensor1){
                    ((MyApplication)getApplication()).triggerNotification(MapActivity.class,
                            getString(R.string.NEWS_CHANNEL_ID),
                            "Warning",
                            "Click here to view map",
                            "You are in danger zone ",
                            NotificationCompat.PRIORITY_HIGH,
                            true,
                            getResources().getInteger(R.integer.notificationId),
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    //startForeground(NOTIFICATION_ID, notification);
                    Log.i("GPS_Service", "onLocationChanged: " +"You are in danger zone. Move " + (dangerZoneRadSensor1 - distance) + " m backwards");
                }
                else{

                        ((MyApplication)getApplication()).cancelNotification(getResources().getInteger(R.integer.notificationId));

                }
                // THIS IS FOR UPDATE NOTIFICATION AT RUNTIME
                /*((MyApplication)getApplication()).updateNotification(MapActivity.class,
                        "Updated Notification",
                        "This is updatedNotification",
                        getString(R.string.NEWS_CHANNEL_ID),
                        getResources().getInteger(R.integer.notificationId),
                        "This is a updated information for bigpicture String",
                        PendingIntent.FLAG_UPDATE_CURRENT);*/
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        if (listener != null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

            //noinspection MissingPermission
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, listener);


        }

        return START_STICKY;
    }

    public class MyBinder extends Binder {
        public GPS_Service getService() {
            return GPS_Service.this;
        }
    }






}