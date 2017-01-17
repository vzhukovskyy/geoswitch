package ua.pp.rudiki.geoswitch.peripherals;


import android.content.Context;
import android.content.SharedPreferences;

import ua.pp.rudiki.geoswitch.trigger.GeoArea;

public class Preferences {
    final String TAG = getClass().getSimpleName();

    public final static String latitudeKey = "latitude";
    public final static String longitudeKey = "longitude";
    public final static String radiusKey = "radius";
    public final static String actionEnabledKey = "actionEnabled";
    public final static String appendTokenKey = "appendToken";
    public final static String urlKey = "url";
    public final static String applicationKey = "log";
    public final static String gpsLogKey = "gpsLog";

    SharedPreferences sharedPrefs;

    public Preferences(Context context) {
        this.sharedPrefs = context.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
    }

//    public void storeValues(GeoArea area, String url) {
//        storeValues(area.getLatitude(), area.getLongitude(), area.getRadius(), url);
//    }
//
//    public void storeValues(Double latitude, Double longitude, Double radius, String url) {
//        storeValues(Double.toString(latitude), Double.toString(longitude), Double.toString(radius), url);
//    }

    public void storeArea(String latitude, String longitude, String radius) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(latitudeKey, latitude);
        editor.putString(longitudeKey, longitude);
        editor.putString(radiusKey, radius);
        editor.commit();
    }

    public GeoArea loadArea() {
        GeoArea area = new GeoArea();

        String latitudeString = sharedPrefs.getString(latitudeKey, "");
        String longitudeString = sharedPrefs.getString(longitudeKey, "");
        String radiusString = sharedPrefs.getString(radiusKey, "");
        try {
            area.setLatitude(Double.parseDouble(latitudeString));
            area.setLongitude(Double.parseDouble(longitudeString));
            area.setRadius(Double.parseDouble(radiusString));
        }
        catch(NumberFormatException e) {
            area = null;
        }

        return area;
    }

    public void storeAction(boolean enabled, boolean appendToken, String url) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(actionEnabledKey, enabled);
        editor.putBoolean(appendTokenKey, appendToken);
        editor.putString(urlKey, url);
        editor.commit();
    }

    public String getLatitudeAsString() {
        return sharedPrefs.getString(latitudeKey, "");
    }

    public Double getLatitude() {
        return getDouble(getLatitudeAsString());
    }

    public String getLongitudeAsString() {
        return sharedPrefs.getString(longitudeKey, "");
    }

    public Double getLongitude() {
        return getDouble(getLongitudeAsString());
    }

    public String getRadiusAsString() {
        return sharedPrefs.getString(radiusKey, "");
    }

    public Double getRadius() {
        return getDouble(getRadiusAsString(), getDefaultRadius());
    }

    private Double getDouble(String key) {
        return getDouble(key, null);
    }

    private Double getDouble(String key, Double defaultValue) {
        Double d = defaultValue;
        String s = sharedPrefs.getString(key, "");
        try {
            d = Double.parseDouble(s);
        }
        catch(NumberFormatException e) {
        }

        return d;
    }

    public boolean getActionEnabled() {
        return sharedPrefs.getBoolean(actionEnabledKey, true);
    }

    public boolean getAppendToken() {
        return sharedPrefs.getBoolean(appendTokenKey, true);
    }

    public String getUrl() {
        return sharedPrefs.getString(urlKey, getDefaultUrl());
    }


    public void storeShortGpsLog(String text) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(gpsLogKey, text);
        editor.commit();
    }

    public String getShortGpsLog() {
        return sharedPrefs.getString(gpsLogKey, "");
    }

    public void storeShortAppLog(String text) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(applicationKey, text);
        editor.commit();
    }

    public String getShortAppLog() {
        return sharedPrefs.getString(applicationKey, "");
    }

    // Default values

    public double getDefaultRadius() {
        return 50.0;
    }

    public String getDefaultUrl() {
        return "";
    }

    public float getDefaultMapZoomLevel() {
        return 17; // 2..21
    }

    public int getMaxLogFileSize() {
        return 200*1024;
    }

}
