package ua.pp.rudiki.geoswitch.trigger;


/*
A and B - intersecting radial areas
Triggers when location transitions from zone A to zone B (just came out of A but still inside B)
 */

import java.util.Objects;

public class A2BTrigger implements GeoTrigger {
    private AreaTrigger aTrigger;
    private AreaTrigger bTrigger;

    public A2BTrigger(GeoPoint a, GeoPoint b) {
        double radius = calculateRadius(a, b);
        aTrigger = new AreaTrigger(a, radius);
        bTrigger = new AreaTrigger(b, radius);
    }

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
        return 2.0/3 * a.distanceTo(b);
    }

    public static double calculateRadius(double latitude1, double longitude1,
                                         double latitude2, double longitude2)
    {
        GeoPoint a = new GeoPoint(latitude1, longitude1);
        GeoPoint b = new GeoPoint(latitude2, longitude2);

        return calculateRadius(a, b);
    }

    // Java methods override

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        A2BTrigger a2bTrigger = (A2BTrigger)o;
        return Objects.equals(aTrigger, a2bTrigger.aTrigger) &&
               Objects.equals(bTrigger, a2bTrigger.bTrigger);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Transition from ");
        sb.append(aTrigger.getArea().toString());
        sb.append(" to ");
        sb.append(bTrigger.getArea().toString());
        return sb.toString();
    }



}
