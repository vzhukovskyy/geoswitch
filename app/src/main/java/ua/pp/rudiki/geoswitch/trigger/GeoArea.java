package ua.pp.rudiki.geoswitch.trigger;

import ua.pp.rudiki.geoswitch.peripherals.HashBuilder;

public class GeoArea {
    private GeoPoint center;
    private double radius;

    public GeoArea() {
        center = new GeoPoint();
        radius = Double.NaN;
    }

    public GeoArea(GeoPoint point, double radius) {
        this.center = point;
        this.radius = radius;
    }

    public GeoArea(double latitude, double longitude, double radius) {
        this.center = new GeoPoint(latitude, longitude);
        this.radius = radius;
    }

    public double getLatitude() {
        return center.latitude;
    }

    public void setLatitude(double latitude) {
        this.center.latitude = latitude;
    }

    public double getLongitude() {
        return center.longitude;
    }

    public void setLongitude(double longitude) {
        this.center.longitude = longitude;
    }

    public GeoPoint getCenter() {
        return center;
    }

    public void setCenter(GeoPoint center) {
        this.center = center;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public boolean equals(Object object) {
        if(this == object)
            return true;
        if(object == null)
            return false;
        if(getClass() != object.getClass())
            return false;

        GeoArea area = (GeoArea)object;
        return center.equals(area.center) && radius == area.radius;
    }

    @Override
    public int hashCode() {
        return new HashBuilder()
                .combine(center)
                .combine(radius)
                .build();
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(center.toString())
                .append(" R=")
                .append(radius)
                .toString();
    }
}
