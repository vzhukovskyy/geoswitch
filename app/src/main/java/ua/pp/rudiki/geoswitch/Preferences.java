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

    SharedPreferences sharedPrefs;

    public Preferences(Context context) {
        this.sharedPrefs = context.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
    }

    public void storeArea(GeoArea area) {
        storeArea(area.getLatitude(), area.getLongitude(), area.getRadius());
    }

    public void storeArea(Double latitude, Double longitude, Double radius) {
        storeArea(Double.toString(latitude), Double.toString(longitude), Double.toString(radius));
    }

    public void storeArea(String latitude, String longitude, String radius) {
        Log.i(TAG, "storeAre ENTER");
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(latitudeKey, latitude);
        editor.putString(longitudeKey, longitude);
        editor.putString(radiusKey, radius);
        editor.commit();
        Log.i(TAG, "storeArea EXIT");
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

    public double getDefaultRadius() {
        return 50.0;
    }

    public float getDefaultMapZoomLevel() {
        return 17; // 2..21
    }

}
