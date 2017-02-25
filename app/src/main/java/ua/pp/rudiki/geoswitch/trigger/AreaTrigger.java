package ua.pp.rudiki.geoswitch.trigger;

import java.util.Objects;

import ua.pp.rudiki.geoswitch.peripherals.HashUtils;

// Base class for Enter Area and Exit Area triggers
// Also used as is in Transition trigger

public class AreaTrigger {

    protected enum Location {
        INVALID,
        INSIDE,
        OUTSIDE
    }

    protected GeoArea area;
    protected transient Location locationAtThisTick = Location.INVALID;
    protected transient Location locationAtPreviousTick = Location.INVALID;

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

    public TriggerType getType() {
        // this is base class, descendants must override this
        return TriggerType.Invalid;
    }

    public void changeLocation(double latitude, double longitude) {
        GeoPoint point = new GeoPoint(latitude, longitude);

        locationAtPreviousTick = locationAtThisTick;
        locationAtThisTick = inTriggerArea(point) ? Location.INSIDE : Location.OUTSIDE;
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

    @Override
    public boolean equals(Object object) {
        if(this == object)
            return true;
        if(object == null)
            return false;
        if (getClass() != object.getClass())
            return false;

        AreaTrigger areaTrigger = (AreaTrigger)object;
        return Objects.equals(area, areaTrigger.getArea());
    }

    @Override
    public int hashCode() {
        return area.hashCode();
    }

}

