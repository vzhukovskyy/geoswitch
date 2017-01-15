package ua.pp.rudiki.geoswitch;

import android.app.Application;
import android.content.Context;

import ua.pp.rudiki.geoswitch.peripherals.GpsLog;
import ua.pp.rudiki.geoswitch.peripherals.HttpUtils;
import ua.pp.rudiki.geoswitch.peripherals.Preferences;

public class GeoSwitchApp extends Application {
    private static Context context;
    private static Preferences preferences;
    private static HttpUtils httpUtils;
    private static GoogleSignIn googleSignIn;
    private static GpsLog gpsLog;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        preferences = new Preferences(context);
        httpUtils = new HttpUtils();
        googleSignIn = new GoogleSignIn(context);
        gpsLog = new GpsLog(context);
    }

    public static Context getAppContext() {
        return context;
    }

    public static Preferences getPreferences() {
        return preferences;
    }

    public static HttpUtils getHttpUtils() {
        return httpUtils;
    }

    public static GoogleSignIn getGoogleSignIn() {
        return googleSignIn;
    }

    public static GpsLog getGpsLog() {
        return gpsLog;
    }
}
