package ua.pp.rudiki.geoswitch.trigger;


// A and B - intersecting radial areas
// Triggers when location transitions from zone A to zone B (just comes out of A but still inside B)

import java.util.Objects;

import ua.pp.rudiki.geoswitch.peripherals.HashUtils;

public class TransitionTrigger implements GeoTrigger {
    private AreaTrigger aTrigger;
    private AreaTrigger bTrigger;

    public TransitionTrigger(GeoPoint a, GeoPoint b) {
        double radius = calculateRadius(a, b);
        aTrigger = new AreaTrigger(a, radius);
        bTrigger = new AreaTrigger(b, radius);
    }

    public TransitionTrigger(GeoArea areaA, GeoArea areaB) {
        this(areaA.getCenter(), areaB.getCenter());
    }

    @Override
    public TriggerType getType() { return TriggerType.Transition; }

    @Override
    public void changeLocation(double latitude, double longitude) {
        aTrigger.changeLocation(latitude, longitude);
        bTrigger.changeLocation(latitude, longitude);
    }

    @Override
    public boolean isTriggered() {
        return aTrigger.exited() && bTrigger.inside();
    }

    public GeoPoint getPointA() {
        return aTrigger.getArea().getCenter();
    }

    public GeoPoint getPointB() {
        return bTrigger.getArea().getCenter();
    }

    public double getRadius() {
        return aTrigger.getArea().getRadius();
    }

    public static double calculateRadius(GeoPoint a, GeoPoint b) {
        return Math.ceil(2.0/3 * a.distanceTo(b));
    }

    public static double calculateRadius(double latitude1, double longitude1,
                                         double latitude2, double longitude2)
    {
        GeoPoint a = new GeoPoint(latitude1, longitude1);
        GeoPoint b = new GeoPoint(latitude2, longitude2);

        return calculateRadius(a, b);
    }

    // Java methods override

    @Override
    public boolean equals(Object object) {
        if(this == object)
            return true;
        if(object == null)
            return false;
        if(getClass() != object.getClass())
            return false;

        TransitionTrigger transitionTrigger = (TransitionTrigger)object;
        return Objects.equals(aTrigger, transitionTrigger.aTrigger) &&
               Objects.equals(bTrigger, transitionTrigger.bTrigger);
    }

    @Override
    public int hashCode() {
        int hash = HashUtils.combineHashCode(1, aTrigger);
        hash = HashUtils.combineHashCode(hash, bTrigger);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Transition from ");
        sb.append(aTrigger.getArea().toString());
        sb.append(" to ");
        sb.append(bTrigger.getArea().toString());
        return sb.toString();
    }



}
