package com.example.semester4project;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";
    private final int LOCATION_REQUEST_CODE = 100;


    public static final double sensor1Lati = 7.095764;
    public static final double sensor2Longi = 80.111980;
    public static int dangerZoneRadSensor1 , warningZoneRadSensor1 ;
    private static final int NOTIFICATION_ID = 101;

    private BroadcastReceiver broadcastReceiver;


    GoogleMap map;// this the map we going to edit




    Location currentLoc;

    double preLati,preLogi;// this variable save previous status before change


    FusedLocationProviderClient fusedLocationProviderClient;//FusedLocationProviderClient is for interacting with
    private LatLng currentLatLang;// THIS CONTAINS CURRENT COORDINATES

    // the location using fused location provider.
    // just commented for check

    private DatabaseReference databaseReference;// using this refernce only we are going to retreive data
    private Circle dangerCircleSensor1, dangerCircleSensor2;//later we can remove circles using this
    private Circle warningCircleSensor1,warningCircleSensor2;
    private Marker dangerMarkerSensor1,dangerMarkerSensor2;
    private Marker warningMarkerSensor1,warningMarkerSensor2;

    //this will run location change by 1m and update in 2s. 1m and 2s mention above.
    /*LocationListener locationListenerGPS=new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            double latitude=location.getLatitude();
            double longitude=location.getLongitude();
            double distance =  DistanceCalculator.distance(sensor1Lati,latitude,sensor2Longi,longitude,0,0); //gives the distance changed
            preLati = latitude;
            preLogi = longitude;
            // String msg="New Latitude: "+latitude + "New Longitude: "+longitude;

            if (distance <= dangerZoneRadSensor1)
                Toast.makeText(MapActivity.this,"You are in danger zone. Move " + (dangerZoneRadSensor1 - distance) + " m backwards" ,Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };*/
    //

    GPS_Service gps_service;
    ServiceConnection m_serviceConnection;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        databaseReference = FirebaseDatabase.getInstance().getReference("Datas");

        //this is only for get value of dangerZoneRadSensor1.
        // below again addValueEventLister added
        databaseReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dangerZoneRadSensor1 = ((Long)dataSnapshot.child("Sensor1").child("dangerRadius").getValue()).intValue();
                warningZoneRadSensor1 = ((Long)dataSnapshot.child("Sensor1").child("warningRadius").getValue()).intValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Intent i =new Intent(getApplicationContext(),GPS_Service.class);
        startService(i);

        m_serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                gps_service = ((GPS_Service.MyBinder)service).getService();
            }

            public void onServiceDisconnected(ComponentName className) {
                gps_service = null;
            }
        };
        Intent intent = new Intent(this, GPS_Service.class);
        bindService(intent, m_serviceConnection, BIND_AUTO_CREATE);




        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        //REQUEST PERMISSION TO ACCESS LOCATION
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_REQUEST_CODE);//the automatically onreques permission will call

        } else {
            fetchLastLocation(); //we have to override onRequestPermissionResult method
        }


        /*locationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,
                2000,
                1, locationListenerGPS);*/
        //isLocationEnabled();


    }




    //This methodccalled after requestPermission method called
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                if(grantResults.length>0&& grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    fetchLastLocation();
                }
                break;
        }
    }


    //currentLoc will intialize here
    private void fetchLastLocation() {
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        Toast.makeText(MapActivity.this,"fetchLastLocation method 2",Toast.LENGTH_SHORT).show();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                currentLoc = location;
                preLati = currentLoc.getLatitude();
                preLogi = currentLoc.getLongitude();


                SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().
                        findFragmentById(R.id.map);

                supportMapFragment.getMapAsync(MapActivity.this);//should this activity class implement
                //                                                                   OnMapReadyCsllback interface and should
                //                                                                  implement all method
                //  i think getMapAsync call the onMapReady

            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Failed with"+e.toString(),Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFailure: "+e.toString() );
            }
        });
    }
    //

    //here is the method of OnMapReadyCallback interFace
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);
        currentLatLang = new LatLng(currentLoc.getLatitude(),currentLoc.getLongitude());
        preLati = currentLoc.getLatitude();
        preLogi = currentLoc.getLongitude();
        //map.moveCamera(CameraUpdateFactory.newLatLng(currentLatLang));
       // map.animateCamera(CameraUpdateFactory.newLatLng(currentLatLang));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLang,13));//zoom to current location animation
        //map.addMarker(new MarkerOptions().position(currentLatLang).title("You are here"));


        //after map assign we are going to do database work here
        //hierarchy is on top Datas there
        //after that Sensor name comes, each sensor child contains two values location and radius
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //dataSnapshot.key will give "Datas" but here i am not using that key
                int sensorReading1,sensorReading2;

                if( dangerCircleSensor1!=null)   dangerCircleSensor1.remove();    // every time data changes on database
                if( dangerCircleSensor2!=null)   dangerCircleSensor2.remove(); // here we delete all marker and

                if( warningCircleSensor1!=null)   warningCircleSensor1.remove();
                if( warningCircleSensor2!=null)   warningCircleSensor2.remove();

                if(dangerMarkerSensor1!=null)  dangerMarkerSensor1.remove(); // circle if it is on map
                if(dangerMarkerSensor2!=null)  dangerMarkerSensor2.remove(); // later we adding marker and circle again using database value

                if(warningMarkerSensor1!=null)  warningMarkerSensor1.remove();
                if(warningMarkerSensor2!=null)  warningMarkerSensor2.remove();

                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    String sensor = postSnapshot.getKey().toLowerCase().trim();//it give the sensor name
                    //we have to handle the cases for all sensors in our list
                    double latitude,longitude;//location in database look like this "12.3,45.754" we have to decode this
                    // for decoding we use locationDecoder and it will return an array of length 2
                    // index 0 gives latitude
                    // index 1 gives longitude
                    if (sensor.equalsIgnoreCase("sensor1")){
                        String  location = postSnapshot.child("location").getValue().toString() ;
                        double[] locationArray = Calculator.getLocation(location);
                        latitude = locationArray[0];
                        longitude = locationArray[1];
                        LatLng latLng = new LatLng(latitude,longitude);//create location coordinates with lati and longi
                        // using latitude and longitude we can mark position in map using below line
                        dangerMarkerSensor1 = map.addMarker(new MarkerOptions().position(latLng).title("Sensor 1 is here"));// added into
                        // Marker object
                        int dangerRadius = ((Long)postSnapshot.child("dangerRadius").getValue()).intValue();//radius is in long we haveto
                        // convert it into int
                        int warningRadius = ((Long)postSnapshot.child("warningRadius").getValue()).intValue();
                        dangerZoneRadSensor1 = dangerRadius;
                        warningZoneRadSensor1 = warningRadius;
                        dangerCircleSensor1 = map.addCircle(getCircleOption(latLng,dangerRadius,Color.RED));//draw the circle on map added
                        // into Circle object
                        warningCircleSensor1 = map.addCircle(getCircleOption(latLng,warningRadius,Color.GREEN));
                    }
                    else if (sensor.equalsIgnoreCase("sensor2")){
                        String  location = postSnapshot.child("location").getValue().toString().trim() ;
                        double[] locationArray = Calculator.getLocation(location);
                        latitude = locationArray[0];
                        longitude = locationArray[1];
                        LatLng latLng = new LatLng(latitude,longitude);
                        dangerMarkerSensor2 = map.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title("Sensor 2 is here"));
                        int dangerRadius = ((Long)postSnapshot.child("dangerRadius").getValue()).intValue();
                        int warningRadius = ((Long)postSnapshot.child("warningRadius").getValue()).intValue();
                        dangerCircleSensor2 = map.addCircle(getCircleOption(latLng,dangerRadius,Color.BLACK));
                        warningCircleSensor2 = map.addCircle(getCircleOption(latLng,warningRadius,Color.GREEN));
                    }
                }

                double latitude=currentLoc.getLatitude();
                double longitude=currentLoc.getLongitude();
                double distance =  Calculator.distance(sensor1Lati,latitude,sensor2Longi,longitude,0,0); //gives the distance changed
                preLati = latitude;
                preLogi = longitude;
                // String msg="New Latitude: "+latitude + "New Longitude: "+longitude;

                if (distance <= dangerZoneRadSensor1)
                    Toast.makeText(MapActivity.this,"You are in danger zone. Move " + (dangerZoneRadSensor1 - distance) + " m backwards" ,Toast.LENGTH_LONG).show();
                else if (distance <= warningZoneRadSensor1 )
                    Toast.makeText(MapActivity.this,"You are in warning zone. " ,Toast.LENGTH_LONG).show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //in this CircleOption we modifies the Circle appearance;
    //edit here for Circle deco
    private CircleOptions getCircleOption(LatLng point, int radius, int color){

        // Instantiating CircleOptions to draw a circle around the marker
        CircleOptions circleOptions = new CircleOptions();

        // Specifying the center of the circle
        circleOptions.center(point);

        // Radius of the circle
        circleOptions.radius(radius);

        // Border color of the circle
        circleOptions.strokeColor(color);

        // Fill color of the circle
        circleOptions.fillColor(0x30ff00f0);

        // Border width of the circle
        circleOptions.strokeWidth(2);

        // Adding the circle to the GoogleMap

        return circleOptions;
    }


    @Override
    protected void onResume() {
        super.onResume();

        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    Log.d(TAG, "onReceive: " + intent.getExtras().get("coordinates"));

                }
            };
        }
        registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
    }
  /*  NotificationManagerCompat notificationManagerCompat;
    Notification notification;*/
/*    private void addNotification() {
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
    }*/

    // earlier here had method call isLocationEnabled() now it in Main Activity.
}

