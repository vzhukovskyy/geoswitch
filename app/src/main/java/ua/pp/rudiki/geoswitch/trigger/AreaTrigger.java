package ua.pp.rudiki.geoswitch.trigger;

import java.util.Objects;

public class AreaTrigger implements GeoTrigger {

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

    @Override
    public void changeLocation(double latitude, double longitude) {
        GeoPoint point = new GeoPoint(latitude, longitude);

        locationAtPreviousTick = locationAtThisTick;
        locationAtThisTick = inTriggerArea(point) ? Location.INSIDE : Location.OUTSIDE;
    }

    @Override
    public boolean isTriggered() {
        return entered();
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

    // Java methods override

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        AreaTrigger areaTrigger = (AreaTrigger)o;
        return Objects.equals(area, areaTrigger.getArea());
    }

    public String toString() {
        return area.toString();
    }

}

