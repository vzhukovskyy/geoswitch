package ua.pp.rudiki.geoswitch.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

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
    public static final String BROADCAST_ISACTIVEMODE_KEY = "MODE_ISACTIVE";
    public static final String BROADCAST_GPSFIXTIMESTAMP_KEY = "GPS_TIMESTAMP";
    public static final String BROADCAST_LATITUDE_KEY = "GPS_LATITUDE";
    public static final String BROADCAST_LONGITUDE_KEY = "GPS_LONGITUDE";

    private LocationManager locationManager;
    private DateFormat dateFormat = SimpleDateFormat.getTimeInstance();

    // fields protected by mutex
    private final Object mutex = new Object();
    private GeoTrigger trigger;
    private Location lastLocation;
    private boolean activeMode;
    // end of fields protected by mutex


    // ***********************************************
    // ***** Android Service overrides
    // ***********************************************

    @Override
    public void onCreate() {
        App.getLogger().info(TAG, "Service created");

//        GeoTrigger newTrigger = loadTrigger();
//        synchronized(mutex) {
//            trigger = newTrigger;
//            activeMode = false;
//        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        App.getLogger().debug(TAG, "onStartCommand");

        // Called from:
        // MainActivity
        // a. when user launches the app
        // b. upon first run after main activity has configured initial trigger
        // c. after user modified configuration (gps turn-on, trigger or action)
        // d. when running app comes to foreground, main activity restarted by OS, screen orientation changed
        // GpsServiceActivator
        // e. upon plug in/unplug from charger

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
        if(frozenLastLocation == null/* && frozenActiveMode*/) {
            requestLastLocation();
        }

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
                    boolean frozenActiveMode;

                    // may be concurrently updated by location service
                    synchronized (mutex) {
//                        if (!activeMode)
//                            return;
                        frozenActiveMode = activeMode;

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
                        App.getLogger().info(TAG, "Retrieved last known location " + location);

                        sendMessageToActivity(frozenActiveMode, location);
                        if(frozenActiveMode) {
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

    private void sendMessageToActivity(boolean isActiveMode, Location location) {
        Intent intent = new Intent(BROADCAST_ACTION);

        intent.putExtra(BROADCAST_ISACTIVEMODE_KEY, isActiveMode);
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
            if (!activeMode)
                return;

            lastLocation = location;
            if(trigger != null) {
                trigger.changeLocation(location.getLatitude(), location.getLongitude());
                triggered = trigger.isTriggered();
            }
        }

        App.getLogger().logLocation(location);
        updateStickyNotification(location);
        sendMessageToActivity(true, location);

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
            App.getLogger().info(TAG, "onStatusChanged: " + provider);
        }
    }


}