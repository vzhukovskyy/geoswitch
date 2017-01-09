package ua.pp.rudiki.geoswitch;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import ua.pp.rudiki.geoswitch.trigger.GeoArea;

public class Preferences {
    final String TAG = getClass().getSimpleName();

    public final static String latitudeKey = "latitude";
    public final static String longitudeKey = "longitude";
    public final static String radiusKey = "radius";
    public final static String actionEnabledKey = "actionEnabled";
    public final static String appendSigninKey = "appendSignin";
    public final static String urlKey = "url";

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

    public void storeAction(boolean enabled, boolean appendSignin, String url) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(actionEnabledKey, enabled);
        editor.putBoolean(appendSigninKey, appendSignin);
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

    public boolean getAppendSignin() {
        return sharedPrefs.getBoolean(appendSigninKey, true);
    }

    public String getUrl() {
        return sharedPrefs.getString(urlKey, getDefaultUrl());
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
