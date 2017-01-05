package ua.pp.rudiki.geoswitch.trigger;


/*
A and B - intersecting radial areas
Triggers when location transitions from zone A to zone B (just came out of A but still inside B)
 */

public class A2BTrigger {
    AreaTrigger aTrigger;
    AreaTrigger bTrigger;

    public A2BTrigger(GeoPoint a, GeoPoint b) {
        double radius = 2.0/3 * a.distanceTo(b);
        aTrigger = new AreaTrigger(a, radius);
        bTrigger = new AreaTrigger(b, radius);
    }

    public void changeLocation(double latitude, double longitude) {
        aTrigger.changeLocation(latitude, longitude);
        bTrigger.changeLocation(latitude, longitude);
    }

    public boolean isTriggered() {
        return aTrigger.exited() && bTrigger.inside();
    }
}
