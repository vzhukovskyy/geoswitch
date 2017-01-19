package ua.pp.rudiki.geoswitch.trigger;

public class AreaTrigger {

    private enum Location {
        INVALID,
        INSIDE,
        OUTSIDE
    }

    private GeoArea area;
    private Location locationAtThisTick = Location.INVALID;
    private Location locationAtPreviousTick = Location.INVALID;

    public AreaTrigger(GeoArea area) {
        this.area = area;
    }

    public AreaTrigger(GeoPoint point, double radius) {
        this.area = new GeoArea(point, radius);
    }

    public GeoArea getArea() {
        return area;
    }

    private boolean inTriggerArea(GeoPoint p) {
        return area.getCenter().distanceTo(p) < area.getRadius();
    }

    public void changeLocation(double latitude, double longitude) {
        changeLocation(new GeoPoint(latitude, longitude));
    }

    public void changeLocation(GeoPoint p) {
        locationAtPreviousTick = locationAtThisTick;
        locationAtThisTick = inTriggerArea(p) ? Location.INSIDE : Location.OUTSIDE;
    }

    public boolean entered() {
        return locationAtThisTick == Location.INSIDE && locationAtPreviousTick == Location.OUTSIDE;
    }

    public boolean exited() {
        return locationAtThisTick == Location.OUTSIDE && locationAtPreviousTick == Location.INSIDE;
    }

    public boolean inside() {
        return locationAtThisTick == Location.INSIDE;
    }
}

