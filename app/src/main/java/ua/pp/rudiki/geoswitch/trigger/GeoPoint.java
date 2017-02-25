package ua.pp.rudiki.geoswitch.trigger;

import android.location.Location;

import ua.pp.rudiki.geoswitch.peripherals.HashUtils;

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


    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object object) {
        if(this == object)
            return true;
        if(object == null)
            return false;
        if(getClass() != object.getClass())
            return false;

        GeoPoint point = (GeoPoint)object;
        return latitude == point.latitude && longitude == point.longitude;
    }

    @Override
    public int hashCode() {
        int hash = HashUtils.combineHashCode(1, latitude);
        hash = HashUtils.combineHashCode(hash, longitude);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(latitude);
        sb.append(",");
        sb.append(longitude);
        sb.append(")");
        return sb.toString();
    }

}