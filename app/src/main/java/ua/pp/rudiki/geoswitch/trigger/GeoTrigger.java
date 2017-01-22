package ua.pp.rudiki.geoswitch.trigger;

public interface GeoTrigger {
    void changeLocation(double latitude, double longitude);
    boolean isTriggered();
}
