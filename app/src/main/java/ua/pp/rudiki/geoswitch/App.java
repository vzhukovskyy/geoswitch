package ua.pp.rudiki.geoswitch;

import android.app.Application;
import android.content.Context;

import ua.pp.rudiki.geoswitch.peripherals.GeoSwitchGoogleApiClient;
import ua.pp.rudiki.geoswitch.peripherals.GeoSwitchLog;
import ua.pp.rudiki.geoswitch.peripherals.ResourceUtils;
import ua.pp.rudiki.geoswitch.service.GpsServiceActivator;
import ua.pp.rudiki.geoswitch.peripherals.NotificationUtils;
import ua.pp.rudiki.geoswitch.peripherals.Preferences;
import ua.pp.rudiki.geoswitch.peripherals.SpeechUtils;

public class App extends Application {
    private final static String TAG = App.class.getSimpleName();

    // refer to https://nfrolov.wordpress.com/2014/07/12/android-using-context-statically-and-in-singletons/

    private static Context appContext;

    // Application object is guaranteed to live so long as the process
    // These fields will hold references to the objects preventing them to be unloaded by Android
    // They will hog some memory, but I prefer to reduce number of bugs related to object/class unload by Android

    private static Preferences preferences;
    private static GeoSwitchGoogleApiClient geoSwitchGoogleApiClient;
    private static GeoSwitchLog geoSwitchLog;
    private static NotificationUtils notificationUtils;
    private static SpeechUtils speechUtils;
    private static GpsServiceActivator gpsServiceActivator;
    private static ResourceUtils resourceUtils;

    public void onCreate() {
        super.onCreate();

        appContext = getApplicationContext();

        preferences = new Preferences(appContext);
        geoSwitchGoogleApiClient = new GeoSwitchGoogleApiClient();
        geoSwitchLog = new GeoSwitchLog();
        notificationUtils = new NotificationUtils();
        speechUtils = new SpeechUtils();
        gpsServiceActivator = new GpsServiceActivator();
        resourceUtils = new ResourceUtils();

        geoSwitchLog.info(TAG, "onCreate complete");
    }

    public static Context getAppContext() {
        return appContext;
    }

    public static Preferences getPreferences() {
        return preferences;
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
