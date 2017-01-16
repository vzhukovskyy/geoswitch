package ua.pp.rudiki.geoswitch;

import android.app.Application;
import android.app.Notification;
import android.content.Context;

import ua.pp.rudiki.geoswitch.peripherals.GpsLog;
import ua.pp.rudiki.geoswitch.peripherals.HttpUtils;
import ua.pp.rudiki.geoswitch.peripherals.NotificationUtils;
import ua.pp.rudiki.geoswitch.peripherals.Preferences;

public class GeoSwitchApp extends Application {
    private static Context context;
    private static Preferences preferences;
    private static HttpUtils httpUtils;
    private static GoogleSignIn googleSignIn;
    private static GpsLog gpsLog;
    private static NotificationUtils notificationUtils;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        preferences = new Preferences(context);
        httpUtils = new HttpUtils();
        googleSignIn = new GoogleSignIn(context);
        gpsLog = new GpsLog(context);
        notificationUtils = new NotificationUtils();
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

}
