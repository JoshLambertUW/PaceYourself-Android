package com.example.paceyourself;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, RunMenu.startRun {

    private GoogleMap mMap;
    private LatLng prev;
    private LatLng latLng;
    private LatLng mPrevLastLocation;
    private Context context;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Location mCurrentLocation;
    Marker mStartLocationMarker;
    Marker mEndLocationMarker;
    LocationRequest mLocationRequest;

    RunMenu currentRunMenu;
    Run currentRun;
    Run previousRun;
    List<LatLng> previousRunList;

    runData rundata;
    long initTime = 0;
    long elapsedTime = 0;
    long prevRunTime = 0;
    float distanceTraveled = 0;
    float distanceTraveledPrev = 0;
    long millis = 0;
    long currentRunCompTime = 0;
    int position = -1;
    int prevRunIndex = 0;

    public static final String PREFS_NAME = "SAVED_PREFS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_maps);

        Intent myIntent = getIntent();

        position = myIntent.getIntExtra("position", -1);
        rundata = new runData();

        if (position > -1){
            List<Run> runHistory = rundata.getRunHistory(context);
            previousRun = runHistory.get(position);
            prevRunTime = previousRun.getTotalTime();
        }


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    public void onLocationChanged(Location location) {
        if (mCurrentLocation == null) mCurrentLocation = location;

        mLastLocation = mCurrentLocation;
        mCurrentLocation = location;

        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        prev = latLng;

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        StartMenu StartFragment = new StartMenu();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, StartFragment);
        transaction.commit();

        if (position > -1){
            previousRunList = previousRun.getCoordList();
            mPrevLastLocation = previousRunList.get(0);
            Polyline previousRunLine = mMap.addPolyline(new PolylineOptions()
                    .width(5)
                    .color(Color.RED));
            previousRunLine.setPoints(previousRunList);
        }

    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            millis = elapsedTime + System.currentTimeMillis() - initTime;

            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            currentRunMenu.setTimerTextView(String.format("%d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 500);
        }

    };

    Handler distanceHandler = new Handler();
    Runnable distanceRunnable = new Runnable() {
        @Override
        public void run() {

            distanceTraveled += mLastLocation.distanceTo(mCurrentLocation);
            ///add to Line
            mMap.addPolyline((new PolylineOptions())
                    .add(prev, latLng).width(6).color(Color.BLUE)
                    .visible(true));
            currentRun.addCoord(latLng);
            prev = latLng;

            comparePrev();

            currentRunMenu.setDistanceTextView(convertUnits(distanceTraveled));
            timerHandler.postDelayed(this, 2000);
        }

    };

    public void comparePrev(){

        if ((position > -1)) {
            prevRunIndex++;
            if (prevRunIndex >= previousRunList.size()) position = -2;
            else {
                float[] results = new float[1];
                LatLng temp = previousRunList.get(prevRunIndex);
                Location.distanceBetween(mPrevLastLocation.latitude, mPrevLastLocation.longitude,
                        temp.latitude, temp.longitude,
                        results);
                distanceTraveledPrev += results[0];
                mPrevLastLocation = temp;
                if (distanceTraveled < distanceTraveledPrev) {
                    currentRunMenu.setPrevDistanceTextView(convertUnits(distanceTraveledPrev - distanceTraveled) + " behind");
                } else {
                    currentRunMenu.setPrevDistanceTextView(convertUnits(distanceTraveled - distanceTraveledPrev) +  " ahead!");
                }
                currentRunCompTime = millis;
            }
        }

        if (position == -2) {
            if (distanceTraveled < previousRun.getTotalDistance()) {
                long millisBehind = millis - currentRunCompTime;
                String timeBehind = "";

                long hours = TimeUnit.MILLISECONDS.toHours(millisBehind) % 24;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisBehind) % 60;
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisBehind) % 60;
                long milliseconds = millisBehind % 1000;

                timeBehind = String.format("%d:%02d:%02d:%02d",
                        hours, minutes, seconds, milliseconds);

                currentRunMenu.setPrevDistanceTextView("You are " + timeBehind + " behind");
            } else {
                long millisAhead = currentRunCompTime - millis;

                currentRunMenu.setPrevDistanceTextView("You finished " + convertTime(millisAhead) + " ahead");
                position = -3;
            }
        }
    }

    private String convertUnits (float distance){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        //settings = PreferenceManager.getDefaultSharedPreferences(context);
        int unit = Integer.parseInt(settings.getString("unit_list", "0"));
        if (unit == 1){
            distance = distance * (float)0.001;
        }
        else {
            distance = distance * (float)0.000621371;
        }

        String d = Float.toString(distance);

        return d;
        //return String.format("%04f", d);
    }

    private String convertTime(long time){

        long hours = TimeUnit.MILLISECONDS.toHours(time) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(time) % 60;
        long milliseconds = time % 1000;

        return String.format("%d:%02d:%02d:%02d",
                hours, minutes, seconds, milliseconds);
    }

    @Override
    public void startRun(int status) {

        currentRunMenu = (RunMenu)
                getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (status == 0) {

            float[] results = new float[1];
            final LatLng temp = latLng;
            Location.distanceBetween(mPrevLastLocation.latitude, mPrevLastLocation.longitude,
                    temp.latitude, temp.longitude,
                    results);
            if (results[0] > (float).1){
                Snackbar.make(findViewById(R.id.mapsActivity), R.string.too_far_snackbar, Snackbar.LENGTH_LONG).show();
                position = -1;
            }

            distanceTraveled = 0;

            if (mStartLocationMarker != null) {
                mStartLocationMarker.remove();
            }

            if (mEndLocationMarker != null) {
                mEndLocationMarker.remove();
            }

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Starting Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mStartLocationMarker = mMap.addMarker(markerOptions);

            initTime = System.currentTimeMillis();
            Calendar calendar = Calendar.getInstance();

            currentRun = new Run(calendar.getTime());
            currentRun.addCoord(latLng);

            timerHandler.postDelayed(timerRunnable, 0);
            distanceHandler.postDelayed(distanceRunnable, 0);
        }

        else if (status == 1){
            initTime = System.currentTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0);
            distanceHandler.postDelayed(distanceRunnable, 0);
        }

        else if (status == 2){
            elapsedTime += System.currentTimeMillis() - initTime;
            timerHandler.removeCallbacks(timerRunnable);
            distanceHandler.removeCallbacks(distanceRunnable);
        }

        else {
            elapsedTime += System.currentTimeMillis() - initTime;
            timerHandler.removeCallbacks(timerRunnable);
            distanceHandler.removeCallbacks(distanceRunnable);

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Ending Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            mEndLocationMarker = mMap.addMarker(markerOptions);
            finishRun();
        }
    }

    public void finishRun(){
        currentRun.setTotalTime(elapsedTime);
        currentRun.setTotalDistance(distanceTraveled);
        currentRunMenu.setResultsTextView("Results: " + currentRun.getTotalDistanceText(context) + " \nTime: " + currentRun.getTotalTimeText());

        if (position > -1){
            position = -1;
            if (distanceTraveled < distanceTraveledPrev){
                currentRunMenu.setPrevDistanceTextView("You were " + convertUnits(distanceTraveledPrev - distanceTraveled) + " behind");
            }
            else {
                currentRunMenu.setPrevDistanceTextView("You were" + convertUnits(distanceTraveled - distanceTraveledPrev) + " ahead!");
            }
        }

        currentRun.setMapPreview(this);
    }

    public void done() {
        saveRun();
    }

    public void saveRun(){
        rundata.addRun(context, currentRun);
    }

}