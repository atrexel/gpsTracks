package com.trexel.gpsTracks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

public class MyActivity extends Activity {

    private class Coordinate{
        double latitude;
        double longitude;
        String timestamp;
    }

    ArrayList<Coordinate> coordinateList = new ArrayList<Coordinate>();
    boolean pollGPS = false;
    boolean isCleared = true;
    Thread getCoordinates;
    double totalMiles = 0;

    GPSManager gps;
    Location location;

    Button locationToggle;
    Button getPosition;
    Button startTracks;
    Button stopTracks;
    Button clearTracks;
    TextView gpsStatusTextView;
    TextView mileageTextView;
    EditText displayEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //map class variables to xml elements
        locationToggle = (Button) findViewById(R.id.locationToggleButton);
        getPosition = (Button) findViewById(R.id.gpsGetPosition);
        startTracks = (Button) findViewById(R.id.startTracksButton);
        stopTracks = (Button) findViewById(R.id.stopTracksButton);
        clearTracks = (Button) findViewById(R.id.clearMilesButton);
        gpsStatusTextView = (TextView) findViewById(R.id.gpsState);
        mileageTextView = (TextView) findViewById(R.id.mileageTextView);
        displayEditText = (EditText) findViewById(R.id.displayEditText);

        Log.v("MainApp", "Setting up GPSManager Object");
        gps = new GPSManager(this);

        //check if gps is enabled
        if(gps.isGPSEnabled && gps.isNetworkEnabled){
            //means gps is on and it found providers for GPS and Network
            gpsStatusTextView.setText("location is enabled");

            location = gps.getLocation();
            //displayEditText.append("location:\n"+location+"\n\n");

            //allow user to click location dependant buttons
            getPosition.setEnabled(true);
            startTracks.setEnabled(true);
            stopTracks.setEnabled(true);
            clearTracks.setEnabled(true);
        }
        else{
            gpsStatusTextView.setText("location service is disabled");
            //disable user from clicking location dependant buttons
            getPosition.setEnabled(false);
            startTracks.setEnabled(false);
            stopTracks.setEnabled(false);
            clearTracks.setEnabled(false);
            Toast.makeText(getApplicationContext(), "GPS and Network is disabled", Toast.LENGTH_SHORT).show();
        }

        locationToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Clicked GPS on Button", Toast.LENGTH_SHORT).show();

                if (gps.isGPSEnabled && gps.isNetworkEnabled) {
                    Toast.makeText(getApplicationContext(), "Already Enabled", Toast.LENGTH_SHORT).show();
                } else {
                    //need to turn on gps; must ask user to do this in settings
                    gpsStatusTextView.setText("location service is disabled");
                    Toast.makeText(getApplicationContext(), "Please enable location", Toast.LENGTH_SHORT).show();
                    gps.showSettingsAlert();
                }
            }
        });

        getPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gps.isGPSEnabled && gps.isNetworkEnabled){
                    if(gps.canGetLocation) {
                        Double latitude = gps.getLatitude();
                        Double longitude = gps.getLongitude();
                        displayEditText.append("Current Position:\n  [" + latitude + ", " + longitude + "]\n\n");
                    }else{
                        Toast.makeText(getApplicationContext(), "Can't get location", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    //need to turn on location
                    gpsStatusTextView.setText("location service is disabled");
                    getPosition.setEnabled(false);
                    startTracks.setEnabled(false);
                    stopTracks.setEnabled(false);
                    clearTracks.setEnabled(false);
                    Toast.makeText(getApplicationContext(), "Please enable location", Toast.LENGTH_SHORT).show();
                    gps.showSettingsAlert();
                }
            }
        });

        startTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gps.isGPSEnabled && gps.isNetworkEnabled) {
                    Toast.makeText(getApplicationContext(), "Tracks Starting", Toast.LENGTH_SHORT).show();
                    if(isCleared) {
                        Log.v("UpdateMileage", "START BUTTON:: isClear");
                        //starts a new tracks thread
                        pollGPS = true;
                        isCleared = false;
                        Log.v("UpdateMileage", "START BUTTON:: starting new getGPSCoordinates Thread");
                        getCoordinates = new getGPSCoordinates();
                        getCoordinates.start();
                    }else{
                        Log.v("UpdateMileage", "START BUTTON:: !isClear");
                        //simply restart current tracks thread
                        pollGPS = true;
                    }
                }else{
                    gpsStatusTextView.setText("location service is disabled");
                    Toast.makeText(getApplicationContext(), "Location must be enabled", Toast.LENGTH_SHORT).show();
                    getPosition.setEnabled(false);
                    startTracks.setEnabled(false);
                    stopTracks.setEnabled(false);
                    clearTracks.setEnabled(false);
                }
            }
        });

        stopTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gps.isGPSEnabled && gps.isNetworkEnabled) {
                    Toast.makeText(getApplicationContext(), "Tracks Stopped", Toast.LENGTH_SHORT).show();
                    pollGPS = false;

                    mileageTextView.setText(totalMiles+" mi");
                    //loop through and print out current coordinates in coordinateList
                    displayEditText.append("Current Coordinates:\n");
                    for (Coordinate coord : coordinateList) {
                        displayEditText.append(coordinateList.indexOf(coord)+" [" + coord.latitude + ", " +
                                coord.longitude + "] "+coord.timestamp+"\n");
                    }
                    displayEditText.append("\n");
                }else{
                    gpsStatusTextView.setText("location service is disabled");
                    Toast.makeText(getApplicationContext(), "Location must be enabled", Toast.LENGTH_SHORT).show();
                    getPosition.setEnabled(false);
                    startTracks.setEnabled(false);
                    stopTracks.setEnabled(false);
                    clearTracks.setEnabled(false);
                }
            }
        });

        clearTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gps.isGPSEnabled && gps.isNetworkEnabled) {
                    Toast.makeText(getApplicationContext(), "Cleared Mileage", Toast.LENGTH_SHORT).show();
                    isCleared = true;
                    coordinateList = new ArrayList<Coordinate>();
                    //reset the mileage textview
                    mileageTextView.setText("0.0 mi");
                }else{
                    gpsStatusTextView.setText("location service is disabled");
                    Toast.makeText(getApplicationContext(), "Location must be enabled", Toast.LENGTH_SHORT).show();
                    getPosition.setEnabled(false);
                    startTracks.setEnabled(false);
                    stopTracks.setEnabled(false);
                    clearTracks.setEnabled(false);
                }
            }
        });


    }//end onCreate()

    private class getGPSCoordinates extends Thread implements Runnable {

        public void run() {
            //System.out.println("Hello from a thread!");
            //user has started a new tracking period
            Log.v("UpdateMileage", "getGPSCoordinates Thread Start");

            while(!isCleared) {
                //user may want to resume tracking
                while(pollGPS){
                    double miles = updateMilage();
                    totalMiles = totalMiles + miles;
                    //mileageTextView.setText(miles+" mi");
                    try{
                        sleep(5000);
                    }catch(Exception e){
                        //error while sleeping...
                    }
                }
            }
            Log.v("UpdateMileage", "getGPSCoordinates Thread End");
        }
    }//end getGPSCoordinates thread class


    java.util.Date date = new java.util.Date();
    Format formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"); //("yyyy-MM-dd_HH-mm-ss");
    public double updateMilage(){
        //first create a new Coordinate
        Coordinate newCoord = new Coordinate();

        // add the current latitude and longitude and timestamp
        if(location != null) {
            newCoord.latitude = location.getLatitude(); //gps.getLatitude();
            newCoord.longitude = location.getLongitude(); //gps.getLongitude();
        }else{
            //location is off....
        }

        String dateString = formatter.format(date);
        newCoord.timestamp = dateString;
        //add the Coordinate to the coordinateList
        coordinateList.add(newCoord);

        Log.v("UpdateMileage", "adding new coordinate:\n lat:"+newCoord.latitude+"\nlon: "+
                newCoord.longitude+"\ntime: "+newCoord.timestamp+"\n\n");

        //update the mileage in the textview
        int index = coordinateList.indexOf(newCoord);

        Log.v("UpdateMileage", "new coordinate index:"+index);

        double miles = 0.0;
        if(index > 0) {
            double lat1 = coordinateList.get(index).latitude;
            double lon1 = coordinateList.get(index).longitude;
            double lat2 = coordinateList.get(index).latitude;
            double lon2 = coordinateList.get(index).longitude;

            //calculate current miles
            miles = distance(lat1, lon1, lat2, lon2, 'M');
        }
        Log.v("UpdateMileage", "distance: "+miles);
        return miles;
    }


    @Override
    public void onRestart() {
        super.onRestart();
        if(gps.isGPSEnabled && gps.isNetworkEnabled){
            gpsStatusTextView.setText("location service is enabled");
            gps = new GPSManager(this);
            getCoordinates = new getGPSCoordinates();
            location = gps.getLocation();
            getPosition.setEnabled(true);
            startTracks.setEnabled(true);
            stopTracks.setEnabled(true);
            clearTracks.setEnabled(true);
        }else{
            gpsStatusTextView.setText("location service is disabled");
            Toast.makeText(getApplicationContext(), "Need to enable location", Toast.LENGTH_SHORT).show();
        }

    }//end onRestart()



    @Override
    public void onStop() {
        super.onStop();
        gps.stopUsingGPS();
        getPosition.setEnabled(false);
        startTracks.setEnabled(false);
        stopTracks.setEnabled(false);
        clearTracks.setEnabled(false);
    }//end onStop()



    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*:  This function calculates distance from latitude and longitude  :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) +
                Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == 'K') {
            dist = dist * 1.609344;
        } else if (unit == 'N') {
            dist = dist * 0.8684;
        }
        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts decimal degrees to radians             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts radians to decimal degrees             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

}
