package ua.pp.rudiki.geoswitch;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import ua.pp.rudiki.geoswitch.peripherals.ActionExecutor;
import ua.pp.rudiki.geoswitch.peripherals.AsyncResultCallback;
import ua.pp.rudiki.geoswitch.trigger.GeoTrigger;
import ua.pp.rudiki.geoswitch.trigger.TriggerType;

public class GeoSwitchGpsService extends Service implements android.location.LocationListener
{
    public static final String SERVICE_BROADCAST_ACTION = "GPSSERVICE_BROADCAST";
    public static final String SERVICE_BROADCAST_ISACTIVEMODE_KEY = "MODE_ISACTIVE";
    public static final String SERVICE_BROADCAST_GPSFIXTIMESTAMP_KEY = "GPS_TIMESTAMP";

    final String TAG = getClass().getSimpleName();
    private final int ONGOING_NOTIFICATION_ID = 9004;

    private LocationManager locationManager;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    // fields protected by mutex
    private Object mutex = new Object();
    private GeoTrigger trigger;
    private Location lastLocation;
    private boolean activeMode;
    // end of fields protected by mutex


    // ***********************************************
    // ***** Android Service overrides
    // ***********************************************

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        GeoSwitchApp.getLogger().log("Service created");

        GeoTrigger newTrigger = loadTrigger();
        synchronized(mutex) {
            trigger = newTrigger;
            activeMode = false;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand, intent=" + intent);
        GeoTrigger newTrigger = loadTrigger();

        // copies which can be used outside of synchronized block
        boolean frozenActiveMode = PowerReceiver.isCharging(GeoSwitchApp.getAppContext());
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

        if (switchToNewTrigger) {
            GeoSwitchApp.getLogger().log("Start monitoring " + newTrigger);
        } else {
            GeoSwitchApp.getLogger().log("Continue monitoring " + newTrigger);
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
        if(frozenLastLocation == null && frozenActiveMode) {
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
        Log.d(TAG, "onDestroy");
        GeoSwitchApp.getLogger().log("Service destroyed");

        super.onDestroy();
    }

    // ***********************************************
    // ***** Class-specific methods
    // ***********************************************

    private void requestLastLocation() {
        GeoSwitchApp.getGeoSwitchGoogleApiClient().requestLastLocation(new AsyncResultCallback<Location>() {
            @Override
            public void onResult(Location location) {
                if(location != null) {
                    boolean lastLocationUpdated = false;

                    // may be concurrently updated by location service
                    synchronized (mutex) {
                        if (!activeMode)
                            return;

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
                        sendMessageToActivity(true, location);
                        updateStickyNotification(location);
                        GeoSwitchApp.getLogger().log("Retrieved last known location " + location);
                    } else {
                        GeoSwitchApp.getLogger().log("Retrieved last known location " + location + " but too late. Ignored it.");
                    }
                } else {
                    GeoSwitchApp.getLogger().log("Last location is unknown, start from scratch");
                }
            }
        });
    }


    private GeoTrigger loadTrigger() {
        GeoTrigger trigger;
        if(GeoSwitchApp.getPreferences().getTriggerType() == TriggerType.EnterArea) {
            trigger = GeoSwitchApp.getPreferences().loadAreaTrigger();
        } else {
            trigger = GeoSwitchApp.getPreferences().loadA2BTrigger();
        }

        return trigger;
    }

    private void displayStickyNotification() {
        updateStickyNotification(null);
    }

    private void updateStickyNotification(Location location) {
        String message;
        if(location != null) {
            Date date = new Date(location.getTime());
            message = getString(R.string.sticky_status_gps_time) + dateFormat.format(date);
        } else {
            message = getString(R.string.sticky_status_waiting_gps);
        }

        Intent notificationIntent = new Intent(this, ActivityMain.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        // make service a foreground service
        Notification notification = new Notification.Builder(this)
                .setContentTitle(getString(R.string.sticky_title))
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    private void removeStickyNotification() {
        stopForeground(true);
    }

    private void sendMessageToActivity(boolean mode, Location location) {
        Intent intent = new Intent(SERVICE_BROADCAST_ACTION);
        intent.putExtra(SERVICE_BROADCAST_ISACTIVEMODE_KEY, mode);
        if(location != null) {
            intent.putExtra(SERVICE_BROADCAST_GPSFIXTIMESTAMP_KEY, location.getTime());
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    // ***********************************************
    // ***** GPS location tracking
    // ***********************************************

    private void registerLocationManagerListener() {
        GeoSwitchApp.getLogger().log("Registering for GPS events");

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
            GeoSwitchApp.getLogger().log("Failed to request location updates: "+ex.getMessage());
        }
    }

    private void unregisterLocationManagerListener() {
        if(locationManager != null) {
            try {
                locationManager.removeUpdates(this);
            } catch(SecurityException e) {
                // ignore
            }

            GeoSwitchApp.getLogger().log("Unregistered from GPS events");
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

        GeoSwitchApp.getLogger().log(location);
        updateStickyNotification(location);
        sendMessageToActivity(true, location);

        if (triggered) {
            GeoSwitchApp.getLogger().log("Trigger fired");
            String notificationMessage = getString(R.string.trigger_fired);
            if(GeoSwitchApp.getPreferences().getActionEnabled()) {
                executeAction();
                notificationMessage += getString(R.string.action_started);
            }

            GeoSwitchApp.getNotificationUtils().displayNotification(notificationMessage, false);
            GeoSwitchApp.getSpeechUtils().speak(notificationMessage);
        }
    }

    void executeAction() {
        String url = GeoSwitchApp.getPreferences().getUrl();
        ActionExecutor.execute(url);
    }

    // ***********************************************
    // ***** LocationListener interface implementation
    // ***********************************************

    @Override
    public void onProviderDisabled(String provider) {
//        Log.i(TAG, "onProviderDisabled: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
//        Log.i(TAG, "onProviderEnabled: " + provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
//        Log.i(TAG, "onStatusChanged: " + provider);
    }


}