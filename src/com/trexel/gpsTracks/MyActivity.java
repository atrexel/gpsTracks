package com.trexel.gpsTracks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class MyActivity extends Activity {

    //class intent constants
    final int REQUEST_ENABLE_LOCATION = 35;

    GPSManager gps;
    Vector<Coordinate> coordinateList;

    Button locationToggle;
    Button startTracks;
    Button stopTracks;
    Button clearTracks;
    Button printTracks;
    TextView gpsStatusTextView;
    TextView mileageTextView;
    TextView indexTextView;
    private GoogleMap map;
    AlertDialog.Builder settingsAlert;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //map class variables to xml elements
        locationToggle = (Button) findViewById(R.id.locationToggleButton);
        startTracks = (Button) findViewById(R.id.startTracksButton);
        stopTracks = (Button) findViewById(R.id.stopTracksButton);
        clearTracks = (Button) findViewById(R.id.clearMilesButton);
        printTracks = (Button) findViewById(R.id.printButton);
        gpsStatusTextView = (TextView) findViewById(R.id.gpsState);
        mileageTextView = (TextView) findViewById(R.id.mileageTextView);
        indexTextView = (TextView) findViewById(R.id.indexTextView);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        //setup new instance of gps manager
        gps = new GPSManager(getApplicationContext(), map, mileageTextView);

        locationToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ask user if they want to change location in settings
                settingsAlert.show();
            }
        });

        startTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gps != null) {
                    gps.startUpdates();
                } else {
                    Toast.makeText(getApplicationContext(), "GPS is not connected", Toast.LENGTH_SHORT);
                }
            }
        });

        stopTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gps != null) {
                    gps.stopUpdates();
                } else {
                    Toast.makeText(getApplicationContext(), "GPS is not connected", Toast.LENGTH_SHORT);
                }
            }
        });

        clearTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gps != null) {
                    gps.clearCoordinates();
                    gps.clearMap();
                } else {
                    Toast.makeText(getApplicationContext(), "GPS is not connected", Toast.LENGTH_SHORT);
                }
            }
        });

        printTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gps != null) {
                    coordinateList = gps.getCoordinates();
                    Toast.makeText(getApplicationContext(), "Output points to File", Toast.LENGTH_SHORT).show();

                    Log.v("csvWriter", "attemptint to write to file");
                    //create a csv file of the coordinate points
                    CSVWriter writer = null;
                    Format filedateformatter = new SimpleDateFormat("MM-dd-yyyy_HH-mm");
                    try {
                        Log.v("csvWriter", "Output filepath: " + Environment.getExternalStorageDirectory().getPath());
                        Log.v("csvWriter", "Output filepath: " + Environment.getRootDirectory().getPath());
                        /* for actual data logging:
                        writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory().getPath()+
                                "trackPoints.csv"), ',');*/
                        //logging for testing:
                        writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory().getPath() +
                                "/GpsTracksData/trackPoints_" + filedateformatter.format(new Date()) + ".csv"), ',');
                        //writer = new CSVWriter(new FileWriter(Environment.getRootDirectory().getPath()+
                        //        "/GpsTracksData/trackPoints_"+filedateformatter.format(date)+".csv"), ',');

                        for (Coordinate point : coordinateList) {
                            String myPoint = point.toString();
                            String[] entries = myPoint.split(",");
                            writer.writeNext(entries);
                        }
                        writer.close();
                    } catch (IOException e) {
                        Log.v("csvWriter", "ERROR:: " + e);
                        //error
                    }
                    Log.v("csvWriter", "finished attempting to write to file");

                } else {
                    Toast.makeText(getApplicationContext(), "GPS is not connected", Toast.LENGTH_SHORT);
                }
            }
        });

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
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

}
