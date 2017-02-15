package ua.pp.rudiki.geoswitch;

import android.app.Application;
import android.content.Context;

import ua.pp.rudiki.geoswitch.peripherals.GeoSwitchGoogleApiClient;
import ua.pp.rudiki.geoswitch.peripherals.GeoSwitchLog;
import ua.pp.rudiki.geoswitch.peripherals.ResourceUtils;
import ua.pp.rudiki.geoswitch.service.GpsServiceActivator;
import ua.pp.rudiki.geoswitch.peripherals.HttpUtils;
import ua.pp.rudiki.geoswitch.peripherals.NotificationUtils;
import ua.pp.rudiki.geoswitch.peripherals.Preferences;
import ua.pp.rudiki.geoswitch.peripherals.SpeechUtils;

public class GeoSwitchApp extends Application {
    private static Context context;
    private static Preferences preferences;
    private static HttpUtils httpUtils;
    private static GeoSwitchGoogleApiClient geoSwitchGoogleApiClient;
    private static GeoSwitchLog geoSwitchLog;
    private static NotificationUtils notificationUtils;
    private static SpeechUtils speechUtils;
    private static GpsServiceActivator gpsServiceActivator;
    private static ResourceUtils resourceUtils;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        preferences = new Preferences(context);
        httpUtils = new HttpUtils();
        geoSwitchGoogleApiClient = new GeoSwitchGoogleApiClient(context);
        geoSwitchLog = new GeoSwitchLog(context);
        notificationUtils = new NotificationUtils();
        speechUtils = new SpeechUtils(context);
        gpsServiceActivator = new GpsServiceActivator(context);
        resourceUtils = new ResourceUtils(context);
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

    public static GeoSwitchGoogleApiClient getGoogleApiClient() {
        return geoSwitchGoogleApiClient;
    }

    public static GeoSwitchLog getLogger() {
        return geoSwitchLog;
    }

    public static NotificationUtils getNotificationUtils() {
        return notificationUtils;
    }

    public static SpeechUtils getSpeechUtils() {
        return speechUtils;
    }

    public static GpsServiceActivator getGpsServiceActivator() { return gpsServiceActivator; }

    public static ResourceUtils getResourceUtils() { return resourceUtils; }
}
