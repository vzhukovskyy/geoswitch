package ua.pp.rudiki.geoswitch.peripherals;


import android.content.Context;
import android.content.SharedPreferences;

import ua.pp.rudiki.geoswitch.trigger.A2BTrigger;
import ua.pp.rudiki.geoswitch.trigger.AreaTrigger;
import ua.pp.rudiki.geoswitch.trigger.GeoArea;
import ua.pp.rudiki.geoswitch.trigger.GeoPoint;
import ua.pp.rudiki.geoswitch.trigger.TriggerType;

public class Preferences {
    final String TAG = getClass().getSimpleName();

    public final static String triggerTypeKey = "triggerType";
    public final static String latitudeKey = "latitude";
    public final static String longitudeKey = "longitude";
    public final static String latitudeToKey = "latitudeTo";
    public final static String longitudeToKey = "longitudeTo";
    public final static String radiusKey = "radius";

    public final static String actionEnabledKey = "actionEnabled";
    public final static String appendTokenKey = "appendToken";
    public final static String urlKey = "url";


    SharedPreferences sharedPrefs;

    public Preferences(Context context) {
        this.sharedPrefs = context.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
    }

    public void storeTriggerType(TriggerType triggerType) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(triggerTypeKey, triggerType.toString());
        editor.commit();
    }


    public TriggerType getTriggerType() {
        String triggerTypeString = sharedPrefs.getString(triggerTypeKey, TriggerType.Invalid.toString());

        TriggerType triggerType = TriggerType.valueOf(triggerTypeString);

        return triggerType;
    }

    public void storeAreaTrigger(AreaTrigger areaTrigger) {
        GeoArea area = areaTrigger.getArea();

        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(latitudeKey, String.valueOf(area.getLatitude()));
        editor.putString(longitudeKey, String.valueOf(area.getLongitude()));
        editor.putString(radiusKey, String.valueOf(area.getRadius()));
        editor.commit();
    }


    public AreaTrigger loadAreaTrigger() {
        GeoArea area = loadArea(latitudeKey, longitudeKey, radiusKey);

        AreaTrigger areaTrigger = null;
        if(area != null)
            areaTrigger = new AreaTrigger(area);

        return areaTrigger;
    }

    private GeoArea loadArea(String latitudeKey, String longitudeKey, String radiusKey) {
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

    public void storeA2BTrigger(A2BTrigger a2bTrigger) {
        GeoPoint pointA = a2bTrigger.getPointA();
        GeoPoint pointB = a2bTrigger.getPointB();

        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(latitudeKey, String.valueOf(pointA.getLatitude()));
        editor.putString(longitudeKey, String.valueOf(pointA.getLongitude()));
        editor.putString(latitudeToKey, String.valueOf(pointB.getLatitude()));
        editor.putString(longitudeToKey, String.valueOf(pointB.getLongitude()));
        // store radius as well for convenience
        editor.putString(radiusKey, String.valueOf(a2bTrigger.getRadius()));
        editor.commit();
    }

    public A2BTrigger loadA2BTrigger() {
        GeoArea areaA = loadArea(latitudeKey, longitudeKey, radiusKey);
        GeoArea areaB = loadArea(latitudeToKey, longitudeToKey, radiusKey);

        A2BTrigger a2bTrigger = null;
        if(areaA != null && areaB != null){
            a2bTrigger = new A2BTrigger(areaA.getCenter(), areaB.getCenter());
        }

        return a2bTrigger;
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
        return getDouble(latitudeKey);
    }

    public String getLongitudeAsString() {
        return sharedPrefs.getString(longitudeKey, "");
    }

    public Double getLongitude() {
        return getDouble(longitudeKey);
    }

    public String getLatitudeToAsString() {
        return sharedPrefs.getString(latitudeToKey, "");
    }

    public Double getLatitudeTo() {
        return getDouble(latitudeToKey);
    }

    public String getLongitudeToAsString() {
        return sharedPrefs.getString(longitudeToKey, "");
    }

    public Double getLongitudeTo() {
        return getDouble(longitudeToKey);
    }

    public String getRadiusAsString() {
        return sharedPrefs.getString(radiusKey, String.valueOf(getDefaultRadius()));
    }

    public Double getRadius() {
        return getDouble(radiusKey, getDefaultRadius());
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

    // generic accessors

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

    // Default values

    public double getDefaultRadius() {
        return 100.0;
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
