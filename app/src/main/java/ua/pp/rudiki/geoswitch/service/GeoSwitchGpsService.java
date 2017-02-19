package ua.pp.rudiki.geoswitch.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ua.pp.rudiki.geoswitch.App;
import ua.pp.rudiki.geoswitch.R;
import ua.pp.rudiki.geoswitch.RequestCode;
import ua.pp.rudiki.geoswitch.peripherals.ActionExecutor;
import ua.pp.rudiki.geoswitch.peripherals.AsyncResultCallback;
import ua.pp.rudiki.geoswitch.trigger.GeoTrigger;

public class GeoSwitchGpsService extends Service implements android.location.LocationListener
{
    private final static String TAG = GeoSwitchGpsService.class.getSimpleName();

    public static final String BROADCAST_ACTION = "GPSSERVICE_BROADCAST";
    //public static final String BROADCAST_ISACTIVEMODE_KEY = "MODE_ISACTIVE";
    public static final String BROADCAST_GPSFIXTIMESTAMP_KEY = "GPS_TIMESTAMP";
    public static final String BROADCAST_LATITUDE_KEY = "GPS_LATITUDE";
    public static final String BROADCAST_LONGITUDE_KEY = "GPS_LONGITUDE";
;
    public static final String START_REASON_KEY = "START_REASON";
    public static final String START_REASON_MAIN_ACTIVITY_CREATED = "MAIN_ACTIVITI_CREATED";
    public static final String START_REASON_USER_CHANGED_TRIGGER = "USER_CHANGED_TRIGGER";
    public static final String START_REASON_USER_CHANGED_ACTION = "USER_CHANGED_ACTION";
    public static final String START_REASON_USER_CHANGED_ACTIVATION = "USER_CHANGED_ACTIVATION";
    public static final String START_REASON_START_OR_STOP = "START_OR_STOP";
    public static final String START_REASON_INITIAL_CONFIGURATION_DONE = "INTITIALLY_CONFIGURED";


    private LocationManager locationManager;
    private DateFormat dateFormat = SimpleDateFormat.getTimeInstance();

    // fields protected by mutex
    private final Object mutex = new Object();
    private GeoTrigger trigger;
    private Location lastLocation;
    // end of fields protected by mutex


    // ***********************************************
    // ***** Android Service overrides
    // ***********************************************

    @Override
    public void onCreate() {
        App.getLogger().info(TAG, "Service created");

        GeoTrigger newTrigger = loadTrigger();
        synchronized(mutex) {
            trigger = newTrigger;
        }

        if(App.getGpsServiceActivator().isOn()) {
            requestLastLocation();
            registerLocationManagerListener();
            displayStickyNotification();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String reason = (intent != null) ? intent.getStringExtra(START_REASON_KEY) : null;

        Log.d(TAG, "onStartCommand reason="+reason);
        App.getLogger().debug(TAG, "onStartCommand reason="+reason);

        if(reason == null) {
            // - service restarted by OS
            // nothing here, everything needed is in onCreate
        }
        else if(reason.equals(START_REASON_MAIN_ACTIVITY_CREATED)) {
            // - if user launched app for the first time then service just created
            // - if user launched app after full device reboot then service just created
            // - user started app
            //       -- if gps monitoring is on then service has been running and still is running
            //           because of foreground notification which ensures service cannot be killed by OS
            //       -- if gps monitoring is off and OS killed service then it is just re-created
            //       -- if gps monitoring is off and OS did not kill the service, then it has been running
            // - user pulled app from long time being in background
            // - screen orientation changed from/to portrait/landscape
            // - Android OS recreated main activity (?)

            // update status on main activity

            if(App.getGpsServiceActivator().isOn()) {
                Location lastLocationCopy;
                synchronized (mutex) {
                    lastLocationCopy = lastLocation;
                }

                if (lastLocationCopy != null) {
                    sendMessageToActivity(lastLocationCopy);
                }
            }
        }
        else if(reason.equals(START_REASON_USER_CHANGED_TRIGGER) ||
                reason.equals(START_REASON_INITIAL_CONFIGURATION_DONE))
        {
            // trigger changed
            GeoTrigger newTrigger = loadTrigger();
            synchronized(mutex) {
                trigger = newTrigger;
                if(lastLocation != null) {
                    // let new trigger continue run from the last position
                    trigger.changeLocation(lastLocation.getLatitude(), lastLocation.getLongitude());
                }
            }

            App.getLogger().info(TAG, "Start monitoring new trigger");
            App.getLogger().logTrigger(trigger);
        }
        else if(reason.equals(START_REASON_USER_CHANGED_ACTION)) {
            // nothing to do
        }
        else if(reason.equals(START_REASON_USER_CHANGED_ACTIVATION) ||
                reason.equals(START_REASON_START_OR_STOP))
        {
            // - if user changed activation then need to check if should be turned on/off accordingly to new mode
            // - plug in/unplug from charger if activation mode is by charger
            // - manual turn on/off if activation is manual

            App.getLogger().logTrigger(trigger);

            boolean activeMode = App.getGpsServiceActivator().isOn();

            if(activeMode) {
                requestLastLocation();
                registerLocationManagerListener();
                displayStickyNotification();
            } else {
                unregisterLocationManagerListener();
                removeStickyNotification();
            }

            if(activeMode) {
                Location lastLocationCopy;
                synchronized (mutex) {
                    lastLocationCopy = lastLocation;
                }

                if (lastLocationCopy != null) {
                    sendMessageToActivity(lastLocationCopy);
                }
            }
        }

/*
        GeoTrigger newTrigger = loadTrigger();

        // copies which can be used outside of synchronized block
        boolean frozenActiveMode = App.getGpsServiceActivator().isOn();
        Location frozenLastLocation;

        boolean switchToNewTrigger = false;
        synchronized(mutex) {
            activeMode = frozenActiveMode;
            frozenLastLocation = lastLocation;

            if (trigger != null) {
                // there is already configured trigger
                if (newTrigger != null && !trigger.equals(newTrigger)) {
                    // there is different area to monitor
                    switchToNewTrigger = true;
                }
            } else {
                // no trigger being monitored yet
                if (newTrigger != null) {
                    // and there is area to monitor
                    switchToNewTrigger = true;
                }
            }

            if (switchToNewTrigger) {
                trigger = newTrigger;
                if (lastLocation != null) {
                    trigger.changeLocation(lastLocation.getLatitude(), lastLocation.getLongitude());
                    // trigger cannot fire here because it needs at least 2 fixes
                }
            }
        }

        App.getLogger().info(TAG, "activeMode="+frozenActiveMode+", lastLocation="+frozenLastLocation);

        if (switchToNewTrigger) {
            App.getLogger().info(TAG, "Start monitoring " + newTrigger);
        } else {
            App.getLogger().info(TAG, "Continue monitoring " + newTrigger);
        }

        // for simplicity always unsubscribe and resubscribe if needed
        unregisterLocationManagerListener();
        removeStickyNotification();
        if(frozenActiveMode) {
            registerLocationManagerListener();
            displayStickyNotification();
        }

        // update UIs
        sendMessageToActivity(frozenActiveMode, frozenLastLocation);
        if(frozenLastLocation != null && frozenActiveMode) {
            updateStickyNotification(frozenLastLocation);
        }

        // request initial location from Google API
        if(frozenLastLocation == null) {
            requestLastLocation();
        }
*/
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        App.getLogger().debug(TAG, "onDestroy");

        super.onDestroy();
    }

    // ***********************************************
    // ***** Class-specific methods
    // ***********************************************

    private void requestLastLocation() {
        App.getGoogleApiClient().requestLastLocation(new AsyncResultCallback<Location>() {
            @Override
            public void onResult(Location location) {
                if(location != null) {
                    boolean lastLocationUpdated = false;

                    // may be concurrently updated by location service
                    synchronized (mutex) {

                        // lastLocation may already be updated by GPS. Drop out in that case
                        if(lastLocation == null) {
                            lastLocation = location;
                            if(trigger != null) {
                                trigger.changeLocation(location.getLatitude(), location.getLongitude());
                            }
                            lastLocationUpdated = true;
                        }
                    }

                    if(lastLocationUpdated) {
                        App.getLogger().logLocation(location);

                        if(App.getGpsServiceActivator().isOn()) {
                            sendMessageToActivity(location);
                            updateStickyNotification(location);
                        }
                    } else {
                        App.getLogger().info(TAG, "Retrieved last known location " + location + " but too late. Ignored it.");
                    }
                } else {
                    App.getLogger().info(TAG, "Last location is unknown, start from scratch");
                }
            }
        });
    }


    private GeoTrigger loadTrigger() {
        return App.getPreferences().loadTrigger();
    }

    private void displayStickyNotification() {
        updateStickyNotification(null);
    }

    private void updateStickyNotification(Location location) {
        String message;
        if(location != null) {
            Date date = new Date(location.getTime());
            message = getString(R.string.service_sticky_status_gps_time) + dateFormat.format(date);
        } else {
            message = getString(R.string.service_sticky_status_waiting_gps);
        }

        // make service a foreground service
        Notification notification =
                App.getNotificationUtils().displayStickyNotification(message);
        startForeground(RequestCode.STICKY_NOTIFICATION_ID, notification);
    }

    private void removeStickyNotification() {
        stopForeground(true);
    }

    private void sendMessageToActivity(Location location) {
        Intent intent = new Intent(BROADCAST_ACTION);

        if(location != null) {
            intent.putExtra(BROADCAST_GPSFIXTIMESTAMP_KEY, location.getTime());
            intent.putExtra(BROADCAST_LATITUDE_KEY, location.getLatitude());
            intent.putExtra(BROADCAST_LONGITUDE_KEY, location.getLongitude());
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    // ***********************************************
    // ***** GPS location tracking
    // ***********************************************

    private void registerLocationManagerListener() {
        App.getLogger().info(TAG, "Registering for GPS events");

        // request update every second BUT ONLY IF distance changed by more then 10m
        final int LOCATION_INTERVAL = 1000;
        final float LOCATION_DISTANCE = 10f;

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    //LocationManager.NETWORK_PROVIDER,
                    //LocationManager.PASSIVE_PROVIDER,
                    LOCATION_INTERVAL, LOCATION_DISTANCE, this);
        } catch (SecurityException ex) {
            App.getLogger().exception(TAG, ex);
        }
    }

    private void unregisterLocationManagerListener() {
        if(locationManager != null) {
            try {
                locationManager.removeUpdates(this);
            } catch(SecurityException e) {
                // ignore
            }

            App.getLogger().info(TAG, "Unregistered from GPS events");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        boolean triggered = false;
        // may be concurrently updated by retrieving last known location from Google Api
        synchronized (mutex) {
            lastLocation = location;
            if(trigger != null) {
                trigger.changeLocation(location.getLatitude(), location.getLongitude());
                triggered = trigger.isTriggered();
            }
        }

        App.getLogger().logLocation(location);
        updateStickyNotification(location);
        sendMessageToActivity(location);

        if (triggered) {
            App.getLogger().info(TAG, "Trigger fired");
            new ActionExecutor().execute();
        }
    }

    // ***********************************************
    // ***** LocationListener interface implementation
    // ***********************************************

    @Override
    public void onProviderDisabled(String provider) {
        if(provider.equals(LocationManager.GPS_PROVIDER)) {
            App.getLogger().info(TAG, "onProviderDisabled: " + provider);
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        if(provider.equals(LocationManager.GPS_PROVIDER)) {
            App.getLogger().info(TAG, "onProviderEnabled: " + provider);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if(provider.equals(LocationManager.GPS_PROVIDER)) {
            String statusString;
            switch(status) {
                case LocationProvider.OUT_OF_SERVICE:
                    statusString = "OUT_OF_SERVICE";
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    statusString = "TEMPORARILY_UNAVAILABLE";
                    break;
                case LocationProvider.AVAILABLE:
                    statusString = "AVAILABLE";
                    break;
                default:
                    statusString = "unknown status "+status;
            }
            App.getLogger().info(TAG, "onStatusChanged(" + provider + ") " + statusString);
        }
    }


}