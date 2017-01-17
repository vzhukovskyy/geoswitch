package ua.pp.rudiki.geoswitch;

import android.app.Application;
import android.content.Context;

import ua.pp.rudiki.geoswitch.peripherals.GoogleSignIn;
import ua.pp.rudiki.geoswitch.peripherals.GpsLog;
import ua.pp.rudiki.geoswitch.peripherals.HttpUtils;
import ua.pp.rudiki.geoswitch.peripherals.NotificationUtils;
import ua.pp.rudiki.geoswitch.peripherals.Preferences;
import ua.pp.rudiki.geoswitch.peripherals.SpeachUtils;

public class GeoSwitchApp extends Application {
    private static Context context;
    private static Preferences preferences;
    private static HttpUtils httpUtils;
    private static GoogleSignIn googleSignIn;
    private static GpsLog gpsLog;
    private static NotificationUtils notificationUtils;
    private static SpeachUtils speachUtils;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        preferences = new Preferences(context);
        httpUtils = new HttpUtils();
        googleSignIn = new GoogleSignIn(context);
        gpsLog = new GpsLog(context);
        notificationUtils = new NotificationUtils();
        speachUtils = new SpeachUtils(context);
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

    public static NotificationUtils getNotificationUtils() {
        return notificationUtils;
    }

    public static SpeachUtils getSpeachUtils() {
        return speachUtils;
    }

}
