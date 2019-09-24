package com.example.semester4project;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
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
import androidx.core.app.NotificationManagerCompat;

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
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Intent i = new Intent("location_update");
                i.putExtra("coordinates",location.getLongitude()+" "+location.getLatitude());
                sendBroadcast(i);
                Log.i("GPS_Service", "onLocationChanged: " + "coordinates"+location.getLongitude()+" "+location.getLatitude());
                double distance =  DistanceCalculator.distance(sensor1Lati,location.getLatitude(),sensor2Longi,+location.getLongitude(),0,0);
                 if (distance <= dangerZoneRadSensor1)

                     Log.i("GPS_Service", "onLocationChanged: " +"You are in danger zone. Move " + (dangerZoneRadSensor1 - distance) + " m backwards");

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

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //noinspection MissingPermission
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,0,listener);

        notificationManagerCompat = NotificationManagerCompat.from(this);

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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("GPS_Service", "onStartCommand");
        addNotification();
        startForeground(NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    public class MyBinder extends Binder {
        public GPS_Service getService() {
            return GPS_Service.this;
        }
    }

    NotificationManagerCompat notificationManagerCompat;
    Notification notification;
    private void addNotification() {
        // create the notification
        Notification.Builder m_notificationBuilder = new Notification.Builder(this)
                .setContentTitle("GPS_Service")
                .setContentText("service_status_monitor")
                .setSmallIcon(R.drawable.notification_small_icon);

        // create the pending intent and add to the notification
        Intent intent = new Intent(this, GPS_Service.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        m_notificationBuilder.setContentIntent(pendingIntent);

        notification =  m_notificationBuilder.build();
        // send the notification
        notificationManagerCompat.notify(NOTIFICATION_ID, m_notificationBuilder.build());

    }
    public void cancelNotification(int id, String tag)
    {
        //you can get notificationManager like this:
        //notificationManage r= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManagerCompat.cancel(tag, id);
    }




}