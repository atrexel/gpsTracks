package com.trexel.gpsTracks;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Date;
import java.util.Vector;

public class GPSManager implements LocationListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private GoogleApiClient client;
    private LocationRequest request;
    private Context context;
    private Vector<Coordinate> points;
    private GoogleMap map;
    private double totalMiles;
    private TextView text;

    public GPSManager(Context newContext, GoogleMap newMap, TextView view) {
        map = newMap;
        context = newContext;
        text = view;
        points = new Vector<Coordinate>();
        Log.v("GPS", "Begin GoogleClientBuilder");
        client = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        client.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        //add a new Coordinate to the vector
        points.addElement(new Coordinate(location.getLatitude(), location.getLongitude(), new Date()));

        //draw a line for route on map
        LatLng coords = new LatLng(location.getLatitude(), location.getLongitude());
        map.clear();
        if(points.size() > 1) {
            Vector<LatLng> pt = new Vector<LatLng>();
            for(int i=0; i<points.size(); i++) {
                pt.addElement(new LatLng(points.get(i).getLatitude(), points.get(i).getLongitude()));
            }
            map.addPolyline(new PolylineOptions().addAll(pt));
        }
        map.addMarker(new MarkerOptions().position(coords));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(coords, 18));

        //calculate the new total distance
        if(points.size() == 1){
            totalMiles = 0;
        } else {
            float[] results = new float[3];
            Location.distanceBetween(points.get(points.size()-2).getLatitude(),
                    points.get(points.size()-2).getLongitude(),
                    points.get(points.size()-1).getLatitude(),
                    points.get(points.size()-1).getLongitude(),
                    results);
            totalMiles = totalMiles + meters2Miles(results[0]);
        }
        String msg = String.format("%2.2f mi", totalMiles);
        text.setText(msg);
    }

    public void startUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this);
    }

    public void stopUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
    }

    public Vector<Coordinate> getCoordinates() {
        return points;
    }

    public void clearCoordinates() {
        points.clear();
        text.setText("0.00 mi");
    }

    public void clearMap() {
        map.clear();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(context, "Connection Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(Bundle bundle) {
        request = LocationRequest.create();
        request.setInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setFastestInterval(3000);
    }

    @Override
    public void onConnectionSuspended(int i) {
        client.disconnect();
    }

    public double meters2Miles(double meters) {
        //miles x 1609.3 = meters
        return meters / 1609.3;
    }
}