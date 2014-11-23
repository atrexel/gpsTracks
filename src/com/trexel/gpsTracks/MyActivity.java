package com.trexel.gpsTracks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MyActivity extends Activity {

    //class intent constants
    final int REQUEST_ENABLE_LOCATION = 35;
    //class constants
    final int SLEEP_SECONDS = 5;

    private class Coordinate{
        double latitude = 0.0;
        double longitude = 0.0;
        String timestamp = "";
    }

    ArrayList<Coordinate> coordinateList = new ArrayList<Coordinate>();
    boolean pollGPS = false;
    boolean isCleared = true;
    Thread getCoordinates;
    double totalMiles = 0.0;
    double totalNaticalMiles = 0.0;
    int coordIndex = 0;

    GPSManager gps;
    //Location location; //seems unecessary

    Button locationToggle;
    Button getPosition;
    Button startTracks;
    Button stopTracks;
    Button clearTracks;
    Button printTracks;
    TextView gpsStatusTextView;
    TextView mileageTextView;
    TextView indexTextView;
    EditText displayEditText;
    AlertDialog.Builder settingsAlert;

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
        printTracks = (Button) findViewById(R.id.printButton);
        gpsStatusTextView = (TextView) findViewById(R.id.gpsState);
        mileageTextView = (TextView) findViewById(R.id.mileageTextView);
        indexTextView = (TextView) findViewById(R.id.indexTextView);
        displayEditText = (EditText) findViewById(R.id.displayEditText);

        //setup new instance of gps manager
        gps = new GPSManager(MyActivity.this);

        //check if user has already enabled gps
        if(gps.isGPSEnabled && gps.isNetworkEnabled){
            //means gps is on and it found providers for GPS and Network
            gpsStatusTextView.setText("location is enabled");

            try {
                //gps should already be instantited from the onStart method
                gps.getLocation();  //should not save the returned location
                //location = gps.getLocation();
                //displayEditText.append("location:\n"+location+"\n\n");
            }catch(Exception e){
                Log.e("GPS", "ERROR: "+e);
            }

            //allow user to click location dependant buttons
            getPosition.setEnabled(true);
            startTracks.setEnabled(true);
            stopTracks.setEnabled(true);
            clearTracks.setEnabled(true);
            printTracks.setEnabled(true);
        }
        else{
            gpsStatusTextView.setText("location service is disabled");
            //disable user from clicking location dependant buttons
            getPosition.setEnabled(false);
            startTracks.setEnabled(false);
            stopTracks.setEnabled(false);
            clearTracks.setEnabled(false);
            printTracks.setEnabled(false);
            Toast.makeText(getApplicationContext(), "GPS and Network is disabled", Toast.LENGTH_SHORT).show();
        }

        locationToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gps = new GPSManager(getApplicationContext());
                if (gps.isGPSEnabled && gps.isNetworkEnabled) {
                    Toast.makeText(getApplicationContext(), "Location is currently Enabled", Toast.LENGTH_SHORT).show();
                    getPosition.setEnabled(true);
                    startTracks.setEnabled(true);
                    stopTracks.setEnabled(true);
                    clearTracks.setEnabled(true);
                    printTracks.setEnabled(true);

                    settingsAlert.show();
                } else {
                    Toast.makeText(getApplicationContext(), "Location is currently Disabled", Toast.LENGTH_SHORT).show();
                    //disable user from clicking location dependant buttons
                    getPosition.setEnabled(false);
                    startTracks.setEnabled(false);
                    stopTracks.setEnabled(false);
                    clearTracks.setEnabled(false);
                    printTracks.setEnabled(false);
                    //need to turn on gps; must ask user to do this in settings
                    gpsStatusTextView.setText("location service is disabled");
                    settingsAlert.show();
                }
            }
        });

        getPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gps.isGPSEnabled && gps.isNetworkEnabled){
                    if(gps.canGetLocation) {
                        gps.getLocation(); //updates latitude and longitude
                        Double latitude = gps.getLatitude();
                        Double longitude = gps.getLongitude();
                        displayEditText.append("Current Location:\n    " + latitude + "," + longitude + "\n\n");
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
                    printTracks.setEnabled(false);
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
                        Log.v("UpdateMileage", ".\n******************************************************\n"+
                                "START BUTTON:: starting new getGPSCoordinates Thread\n"+
                                "*******************************************************");
                        getCoordinates = new getGPSCoordinates();
                        getCoordinates.start();
                    }else{
                        Log.v("UpdateMileage", "START BUTTON:: !isClear, restarting previous polling");
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
                    printTracks.setEnabled(false);
                }
            }
        });

        stopTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gps.isGPSEnabled && gps.isNetworkEnabled) {
                    Toast.makeText(getApplicationContext(), "Tracks Stopped", Toast.LENGTH_SHORT).show();
                    pollGPS = false;

                    /*
                    //loop through and print out current coordinates in coordinateList
                    displayEditText.append("Current Coordinates:\n");
                    for (Coordinate coord : coordinateList) {
                        displayEditText.append("[" + coord.latitude + ", " +
                                coord.longitude + "] "+coordinateList.indexOf(coord)+"\n");
                    }
                    displayEditText.append("\n");
                    */
                }else{
                    gpsStatusTextView.setText("location service is disabled");
                    Toast.makeText(getApplicationContext(), "Location must be enabled", Toast.LENGTH_SHORT).show();
                    getPosition.setEnabled(false);
                    startTracks.setEnabled(false);
                    stopTracks.setEnabled(false);
                    clearTracks.setEnabled(false);
                    printTracks.setEnabled(false);
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
                    coordIndex = 0;
                    totalMiles = 0.0;
                    totalNaticalMiles = 0.0;
                    //reset the mileageTextView, displayEditText, and totalMiles
                    mileageTextView.setText("0.00 mi");
                    displayEditText.setText("");
                    indexTextView.setText("0");
                }else{
                    gpsStatusTextView.setText("location service is disabled");
                    Toast.makeText(getApplicationContext(), "Location must be enabled", Toast.LENGTH_SHORT).show();
                    getPosition.setEnabled(false);
                    startTracks.setEnabled(false);
                    stopTracks.setEnabled(false);
                    clearTracks.setEnabled(false);
                    printTracks.setEnabled(false);
                }
            }
        });

        printTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gps.isGPSEnabled && gps.isNetworkEnabled) {
                    Toast.makeText(getApplicationContext(), "Output points to File", Toast.LENGTH_SHORT).show();

                    Log.v("csvWriter", "attemptint to write to file");
                    //create a csv file of the coordinate points
                    CSVWriter writer = null;
                    try
                    {
                        Log.v("csvWriter", "Output filepath: "+Environment.getExternalStorageDirectory().getPath());
                        Log.v("csvWriter", "Output filepath: "+Environment.getRootDirectory().getPath());
                        writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory().getPath()+
                                "/trackPoints.csv"), ',');
                        //writer = new CSVWriter(new FileWriter(Environment.getRootDirectory().getPath()+
                        //        "/trackPoints.csv"), ',');

                        for (Coordinate point : coordinateList){
                            String myPoint = "";
                            myPoint = point.latitude+",";
                            myPoint += point.longitude+",";
                            myPoint += point.timestamp+"";
                            String[]entries = myPoint.split(",");
                            writer.writeNext(entries);
                        }
                        writer.close();
                    }
                    catch (IOException e)
                    {
                        Log.v("csvWriter", "ERROR:: "+e);
                        //error
                    }
                    Log.v("csvWriter", "finished attempting to write to file");

                }else{
                    gpsStatusTextView.setText("location service is disabled");
                    Toast.makeText(getApplicationContext(), "Location must be enabled", Toast.LENGTH_SHORT).show();
                    getPosition.setEnabled(false);
                    startTracks.setEnabled(false);
                    stopTracks.setEnabled(false);
                    clearTracks.setEnabled(false);
                    printTracks.setEnabled(false);
                }
            }
        });

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        Intent enableLocation = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        MyActivity.this.startActivityForResult(enableLocation, REQUEST_ENABLE_LOCATION);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        settingsAlert = new AlertDialog.Builder(this);
        settingsAlert.setMessage("Would you like to change your current location setting?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener);

    }//end onCreate()


    NumberFormat numformatter = new DecimalFormat("#0.00");
    private class getGPSCoordinates extends Thread implements Runnable {

        TextView threadMileageTextView = (TextView) findViewById(R.id.mileageTextView);
        TextView threadIndexTextView = (TextView) findViewById(R.id.indexTextView);
        EditText threadDisplayEditText = (EditText) findViewById(R.id.displayEditText);

        public void run() {
            //user has started a new tracking period
            Log.v("UpdateMileage", "getGPSCoordinates Thread Start");

            while(!isCleared) {
                //user may want to resume tracking
                while(pollGPS){
                    //add latest coordinate and update the mileage
                    double miles = updateMilage();
                    coordIndex = coordIndex + 1;
                    if(miles > 0) {
                        totalMiles = totalMiles + miles;
                        Log.v("UpdateMileage", "calc miles: "+miles+" mi");
                        Log.v("UpdateMileage", "tot miles:  "+totalMiles+" mi");
                        Log.v("UpdateMileage", "nat miles:  "+totalNaticalMiles+" mi");
                    }

                    // Must use the runOnUiThread method to update UI elements
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            threadMileageTextView.setText(numformatter.format(totalMiles)+" mi");
                            threadIndexTextView.setText(coordIndex+"");
                            threadDisplayEditText.append(coordinateList.get(coordIndex-1).latitude+","+
                                    coordinateList.get(coordIndex-1).longitude+"\n");
                        }
                    });

                    try{
                        sleep(SLEEP_SECONDS * 1000);
                    }catch(Exception e){
                        //error trying to sleep...
                    }
                }
            }
            Log.v("UpdateMileage", "getGPSCoordinates Thread End");
        }
    }//end getGPSCoordinates thread class


    java.util.Date date = new java.util.Date();
    Format formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    public double updateMilage(){
        double miles = 0.0;

        //get updated latitude and longitude
        gps.getLocation();
        //location = gps.getLocation(); //no need to save the location object

        //first create a new Coordinate
        Coordinate newCoord = new Coordinate();
        // add the current latitude and longitude and timestamp
        newCoord.latitude = gps.getLatitude(); //location.getLatitude();
        newCoord.longitude = gps.getLongitude(); //location.getLongitude();
        newCoord.timestamp = formatter.format(date);
        //add the Coordinate to the coordinateList
        coordinateList.add(newCoord);
        int index = coordinateList.indexOf(newCoord);

        Log.v("UpdateMileage", "["+index+"] nextCoord: ("+newCoord.latitude+",  "+
                newCoord.longitude+") ["+newCoord.timestamp+"]");

        if(index > 0) {
            double lat1 = coordinateList.get(index-1).latitude;
            double lon1 = coordinateList.get(index-1).longitude;
            double lat2 = coordinateList.get(index).latitude;
            double lon2 = coordinateList.get(index).longitude;

            if(!(lat1 == lat2 && lon1 == lon2)) {
                //if coordinate is different from previous, calculate distance
                Log.v("UpdateMileage", ".\n.\n........ updating miles ........\n.");
                totalNaticalMiles = totalNaticalMiles + distFrom(lat1, lon1, lat2, lon2);
                miles = (distance(lat1, lon1, lat2, lon2, 'M') * 0.00062137) * Math.pow(10, 3);
            }
        }
        return miles;
    }//end updateMileage


    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 3958.75;
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c;

        return dist;
    }

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



    @Override
    public void onStart() {
        super.onStart();
        gps = new GPSManager(MyActivity.this);
        if(gps.isGPSEnabled && gps.isNetworkEnabled){
            gpsStatusTextView.setText("location service is enabled");
            gps = new GPSManager(this);
            getCoordinates = new getGPSCoordinates();
            getPosition.setEnabled(true);
            startTracks.setEnabled(true);
            stopTracks.setEnabled(true);
            clearTracks.setEnabled(true);
            printTracks.setEnabled(true);
        }else{
            gpsStatusTextView.setText("location service is disabled");
            Toast.makeText(getApplicationContext(), "Need to enable location", Toast.LENGTH_SHORT).show();
        }
    }//end onStart()

    @Override
    public void onResume() {
        super.onResume();
        gps = new GPSManager(MyActivity.this);
        if(gps.isGPSEnabled && gps.isNetworkEnabled){
            gpsStatusTextView.setText("location service is enabled");
            gps = new GPSManager(this);
            getCoordinates = new getGPSCoordinates();
            getPosition.setEnabled(true);
            startTracks.setEnabled(true);
            stopTracks.setEnabled(true);
            clearTracks.setEnabled(true);
            printTracks.setEnabled(true);
        }else{
            gpsStatusTextView.setText("location service is disabled");
            Toast.makeText(getApplicationContext(), "Need to enable location", Toast.LENGTH_SHORT).show();
        }
    }//end onResume()

    /* handled in onResume() now
    @Override
    public void onRestart() {
        super.onRestart();
        if(gps.isGPSEnabled && gps.isNetworkEnabled){
            gpsStatusTextView.setText("location service is enabled");
            gps = new GPSManager(this);
            getCoordinates = new getGPSCoordinates();
            getPosition.setEnabled(true);
            startTracks.setEnabled(true);
            stopTracks.setEnabled(true);
            clearTracks.setEnabled(true);
            printTracks.setEnabled(true);
        }else{
            gpsStatusTextView.setText("location service is disabled");
            Toast.makeText(getApplicationContext(), "Need to enable location", Toast.LENGTH_SHORT).show();
        }

    }//end onRestart()
    */


    @Override
    public void onPause() {
        super.onPause();
        gps.stopUsingGPS();
        getPosition.setEnabled(false);
        startTracks.setEnabled(false);
        stopTracks.setEnabled(false);
        clearTracks.setEnabled(false);
        printTracks.setEnabled(false);
    }//end onPause()

    @Override
    public void onStop() {
        super.onStop();
        gps.stopUsingGPS();
        getPosition.setEnabled(false);
        startTracks.setEnabled(false);
        stopTracks.setEnabled(false);
        clearTracks.setEnabled(false);
        printTracks.setEnabled(false);
    }//end onStop()


    //handles intent callbacks
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(requestCode == REQUEST_ENABLE_LOCATION){
            gps = new GPSManager(MyActivity.this);
            if(gps.isGPSEnabled && gps.isNetworkEnabled){
                gpsStatusTextView.setText("location service is enabled");
                gps = new GPSManager(this);
                getPosition.setEnabled(true);
                startTracks.setEnabled(true);
                stopTracks.setEnabled(true);
                clearTracks.setEnabled(true);
                printTracks.setEnabled(true);
            }else{
                gpsStatusTextView.setText("location service is disabled");
                Toast.makeText(getApplicationContext(), "Need to enable location", Toast.LENGTH_SHORT).show();
            }
        }//if (requestCode == *another activity constant*){
            /*
            if(resultCode == RESULT_OK){
                if(!bluetoothAdapter.isEnabled()) {
                    CheckBlueToothState();
                }
            }
            */
        //}
    }

}
