package ua.pp.rudiki.geoswitch.peripherals;

public interface GpsLogListener {
    void onLog(String message);
    void onGpsCoordinatesLog(double latitude, double longitude);
}
