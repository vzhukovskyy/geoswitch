package ua.pp.rudiki.geoswitch.trigger;

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

    public boolean equals(GeoArea area) {
        if(area == null)
            return false;

        return center.equals(area.center) && radius == area.radius;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(center.toString());
        sb.append(" R=");
        sb.append(radius);
        return sb.toString();
    }
}
