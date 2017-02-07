package ua.pp.rudiki.geoswitch.trigger;

public interface GeoTrigger {
    TriggerType getType();
    void changeLocation(double latitude, double longitude);
    boolean isTriggered();
}
