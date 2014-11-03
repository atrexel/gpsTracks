package com.trexel.gpsTracks;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MyActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        Log.v("MainApp", "Setting up GPSManager Object");
        GPSManager gps = new GPSManager(this);
        if(gps.canGetLocation()){
            // gps enabled
            Log.d("MainApp", "canGetLocation() is true");
            Log.d("MainApp", "Latitude: "+gps.getLatitude());
            Log.d("MainApp", "Longitude: "+gps.getLongitude());
        } // return boolean true/false
        Log.v("MainApp", "Finished canGetLocation if Statement");


    }
}
