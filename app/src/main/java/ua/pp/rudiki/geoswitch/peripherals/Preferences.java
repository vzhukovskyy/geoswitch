package ua.pp.rudiki.geoswitch.peripherals;


import android.content.Context;
import android.content.SharedPreferences;

import ua.pp.rudiki.geoswitch.trigger.AreaTrigger;
import ua.pp.rudiki.geoswitch.trigger.ExitAreaTrigger;
import ua.pp.rudiki.geoswitch.trigger.GeoTrigger;
import ua.pp.rudiki.geoswitch.trigger.TransitionTrigger;
import ua.pp.rudiki.geoswitch.trigger.EnterAreaTrigger;
import ua.pp.rudiki.geoswitch.trigger.GeoArea;
import ua.pp.rudiki.geoswitch.trigger.GeoPoint;
import ua.pp.rudiki.geoswitch.trigger.TriggerType;

public class Preferences {
    private final static String TAG = Preferences.class.getSimpleName();

    public final static String triggerTypeKey = "triggerType";
    public final static String latitudeKey = "latitude";
    public final static String longitudeKey = "longitude";
    public final static String latitudeToKey = "latitudeTo";
    public final static String longitudeToKey = "longitudeTo";
    public final static String radiusKey = "radius";

    public final static String showNotificationKey = "showNotification";
    public final static String playSoundKey = "playSound";
    public final static String speakOutKey = "speakOut";
    public final static String sendPostKey = "sendPost";
    public final static String appendTokenKey = "appendToken";
    public final static String urlKey = "url";

    public final static String activateOnChargerKey = "activateOnCharger";
    public final static String gpsManuallyActivatedKey = "gpsManuallyActivated";

    private SharedPreferences sharedPrefs;

    public Preferences(Context context) {
        this.sharedPrefs = context.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
    }

    public TriggerType getTriggerType() {
        String triggerTypeString = sharedPrefs.getString(triggerTypeKey, TriggerType.Invalid.toString());

        TriggerType triggerType = TriggerType.valueOf(triggerTypeString);

        return triggerType;
    }

    public void storeTrigger(GeoTrigger trigger) {
        switch(trigger.getType()) {
            case EnterArea:
            case ExitArea:
                storeAreaTrigger((AreaTrigger) trigger);
                break;
            case Transition:
                storeTransitionTrigger((TransitionTrigger) trigger);
                break;
        }
    }

    private void storeAreaTrigger(AreaTrigger areaTrigger) {
        GeoArea area = areaTrigger.getArea();

        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(triggerTypeKey, areaTrigger.getType().name());

        editor.putString(latitudeKey, String.valueOf(area.getLatitude()));
        editor.putString(longitudeKey, String.valueOf(area.getLongitude()));
        editor.putString(radiusKey, String.valueOf(area.getRadius()));
        editor.commit();
    }


    private void storeTransitionTrigger(TransitionTrigger transitionTrigger) {
        GeoPoint pointA = transitionTrigger.getPointA();
        GeoPoint pointB = transitionTrigger.getPointB();

        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(triggerTypeKey, transitionTrigger.getType().name());

        editor.putString(latitudeKey, String.valueOf(pointA.getLatitude()));
        editor.putString(longitudeKey, String.valueOf(pointA.getLongitude()));
        editor.putString(latitudeToKey, String.valueOf(pointB.getLatitude()));
        editor.putString(longitudeToKey, String.valueOf(pointB.getLongitude()));
        // store radius as well for convenience
        editor.putString(radiusKey, String.valueOf(transitionTrigger.getRadius()));
        editor.commit();
    }


    public GeoTrigger loadTrigger() {
        TriggerType triggerType = getTriggerType();
        GeoArea area = loadArea(latitudeKey, longitudeKey, radiusKey);

        GeoTrigger trigger;
        switch(triggerType) {
            case EnterArea:
                trigger = new EnterAreaTrigger(area);
                break;
            case ExitArea:
                trigger = new ExitAreaTrigger(area);
                break;
            case Transition:
                GeoArea areaTo = loadArea(latitudeToKey, longitudeToKey, radiusKey);
                trigger = new TransitionTrigger(area, areaTo);
                break;
            default:
                trigger = null;

        }

        return trigger;
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

    public void storeAction(boolean showNotification, boolean playSound, boolean speakOut,
                            boolean sendPost, boolean appendToken, String url)
    {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(showNotificationKey, showNotification);
        editor.putBoolean(playSoundKey, playSound);
        editor.putBoolean(speakOutKey, speakOut);
        editor.putBoolean(sendPostKey, sendPost);
        editor.putBoolean(appendTokenKey, appendToken);
        editor.putString(urlKey, url);
        editor.commit();
    }

    public void storeActivationOptions(boolean activateOnCharger)
    {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(activateOnChargerKey, activateOnCharger);
        editor.commit();
    }

    public void storeGpsManuallyActivated(boolean activated)
    {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(gpsManuallyActivatedKey, activated);
        editor.commit();
    }

    public boolean getShowNotification() {
        return sharedPrefs.getBoolean(showNotificationKey, true);
    }

    public boolean getPlaySound() { return sharedPrefs.getBoolean(playSoundKey, false); }

    public boolean getSpeakOut() {
        return sharedPrefs.getBoolean(speakOutKey, true);
    }

    public boolean getSendPost() {
        return sharedPrefs.getBoolean(sendPostKey, false);
    }

    public boolean getAppendToken() {
        return sharedPrefs.getBoolean(appendTokenKey, false);
    }

    public String getUrl() {
        return sharedPrefs.getString(urlKey, getDefaultUrl());
    }

    public boolean getActivateOnCharger() {
        return sharedPrefs.getBoolean(activateOnChargerKey, false);
    }

    public boolean getGpsManuallyActivated() { return sharedPrefs.getBoolean(gpsManuallyActivatedKey, false); }

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
        return 256*1024;
    }

    public long getDefaultTimePeriodForKml() { return 3*60*60*1000;}

}
