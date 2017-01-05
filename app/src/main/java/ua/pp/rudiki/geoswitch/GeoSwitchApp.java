package ua.pp.rudiki.geoswitch;

import android.app.Application;
import android.content.Context;

public class GeoSwitchApp extends Application {
    private static Context context;
    private static Preferences preferences;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        preferences = new Preferences(context);
    }

    public static Context getAppContext() {
        return context;
    }

    public static Preferences getPreferences() {
        return preferences;
    }
}
