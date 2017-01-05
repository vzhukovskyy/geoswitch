package ua.pp.rudiki.geoswitch.trigger;

import android.location.Location;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class GeoPoint {
    public double latitude = Double.NaN;
    public double longitude = Double.NaN;

    public GeoPoint() {
    }

    public GeoPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double distanceTo(GeoPoint p) {
        float[] results = new float[1];
        Location.distanceBetween(latitude, longitude, p.latitude, p.longitude, results);
        float distanceInMeters = results[0];
        return distanceInMeters;
    }
}