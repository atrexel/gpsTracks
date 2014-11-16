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

import java.util.ArrayList;

public class MyActivity extends Activity {

    private class Coordinate{
        double latitude;
        double longitude;
        double timestamp;
    }

    ArrayList<Coordinate>[] coordList;

    GPSManager gps;
    Location location;

    Button locationToggle;
    Button getPosition;
    Button getProvider;
    TextView gpsStatusTextView;
    EditText displayEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //map class variables to xml elements
        locationToggle = (Button) findViewById(R.id.locationToggleButton);
        getPosition = (Button) findViewById(R.id.gpsGetPosition);
        getProvider = (Button) findViewById(R.id.listproviders);
        gpsStatusTextView = (TextView) findViewById(R.id.gpsState);
        displayEditText = (EditText) findViewById(R.id.displayEditText);

        Log.v("MainApp", "Setting up GPSManager Object");
        gps = new GPSManager(this);

        //check if gps is endabled
        if(gps.isGPSEnabled && gps.isNetworkEnabled){
            //means gps is on and it found providers for GPS and Network
            gpsStatusTextView.setText("location is enabled");

            location = gps.getLocation();
            //displayEditText.append("location:\n"+location+"\n\n");

            //allow user to click the getPosition button
            getPosition.setEnabled(true);
            getProvider.setEnabled(true);
        }
        else{
            gpsStatusTextView.setText("location service is disabled");
            getPosition.setEnabled(false);
            getProvider.setEnabled(false);
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

                /*
                // Start loction service
                LocationManager locationManager = (LocationManager)[OUTERCLASS].
                this.getSystemService(Context.LOCATION_SERVICE);

                Criteria locationCritera = new Criteria();
                locationCritera.setAccuracy(Criteria.ACCURACY_COARSE);
                locationCritera.setAltitudeRequired(false);
                locationCritera.setBearingRequired(false);
                locationCritera.setCostAllowed(true);
                locationCritera.setPowerRequirement(Criteria.NO_REQUIREMENT);

                String providerName = locationManager.getBestProvider(locationCritera, true);

                if (providerName != null && locationManager.isProviderEnabled(providerName)) {
                    // Provider is enabled
                    locationManager.requestLocationUpdates(providerName, 20000, 100,[OUTERCLASS]. this.locationListener)
                    ;
                } else {
                    // Provider not enabled, prompt user to enable it
                    Toast.makeText([OUTERCLASS]. this, R.string.please_turn_on_gps, Toast.LENGTH_LONG).show();
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    [OUTERCLASS].this.startActivity(myIntent);
                }
            */
            }
        });

        getPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Clicked get position Button", Toast.LENGTH_SHORT).show();

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
                    getProvider.setEnabled(false);
                    Toast.makeText(getApplicationContext(), "Please enable location", Toast.LENGTH_SHORT).show();
                    gps.showSettingsAlert();
                }
            }
        });

        getProvider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Clicked get providers Button", Toast.LENGTH_SHORT).show();

                if(gps.isGPSEnabled && gps.isNetworkEnabled) {
                    displayEditText.append("Provider: " + location.getProvider() + "\n\n");
                }else{
                    gpsStatusTextView.setText("location service is disabled");
                    Toast.makeText(getApplicationContext(), "Location must be enabled", Toast.LENGTH_SHORT).show();
                    getProvider.setEnabled(false);
                    getPosition.setEnabled(false);
                }
            }
        });


    }//end onCreate()


    @Override
    public void onRestart() {
        super.onRestart();
        if(gps.isGPSEnabled && gps.isNetworkEnabled){
            gpsStatusTextView.setText("location service is enabled");
            gps = new GPSManager(this);
            location = gps.getLocation();
            getPosition.setEnabled(true);
            getProvider.setEnabled(true);
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
        getProvider.setEnabled(false);
    }//end onStop()


}
