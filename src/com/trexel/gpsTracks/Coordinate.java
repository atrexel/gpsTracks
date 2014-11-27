package com.trexel.gpsTracks;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Coordinate {
    private double latitude;
    private double longitude;
    private Date timestamp;

    Format dateformatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    public Coordinate() {

    }

    public Coordinate(double lat, double lon, Date time) {
        this.latitude = lat;
        this.longitude = lon;
        this.timestamp = time;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String toString(){
        String output = "";
        output = latitude+",";
        output += longitude+",";
        output += dateformatter.format(timestamp);
        return output;
    }
}
