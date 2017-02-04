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
import java.util.concurrent.atomic.AtomicReference;

import ua.pp.rudiki.geoswitch.action.ActionExecutor;
import ua.pp.rudiki.geoswitch.peripherals.AsyncResultCallback;
import ua.pp.rudiki.geoswitch.trigger.GeoPoint;
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

    GeoTrigger trigger;
    boolean activeMode;
    Location lastLocation;

    // ***********************************************
    // ***** Android Service overrides
    // ***********************************************

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        GeoSwitchApp.getGpsLog().log("Service created");

        activeMode = false;
        trigger = loadTrigger();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand, intent=" + intent);
        GeoTrigger newTrigger = loadTrigger();

        boolean switchToNewArea = false;
        if (trigger != null) {
            // there is already configured trigger
             if(newTrigger != null && !trigger.equals(newTrigger)) {
                // there is different area to monitor
                switchToNewArea = true;
            }
        }
        else {
            // no trigger being monitored yet
            if(newTrigger != null) {
                // and there is area to monitor
                switchToNewArea = true;
            }
        }

        if(switchToNewArea) {
            trigger = newTrigger;
            GeoSwitchApp.getGpsLog().log("Started monitoring "+newTrigger);
        } else {
            // commented out to reduce logging when screen orientation changed
            GeoSwitchApp.getGpsLog().log("Continue monitoring "+newTrigger);
        }

        determineMode();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        GeoSwitchApp.getGpsLog().log("Service destroyed");

        super.onDestroy();
    }

    // ***********************************************
    // ***** Class-specific methods
    // ***********************************************

    private void requestLastLocation() {
        GeoSwitchApp.getGeoSwitchGoogleApiClient().requestLastLocation(new AsyncResultCallback<Location>() {
            @Override
            public void onResult(Location location) {
                boolean lastLocationUpdated = false;
                if(location != null) {
                    // may be concurrently updated by location service
                    synchronized (this) {
                        // lastLocation may already be updated by GPS. Ignore if already not null
                        if(lastLocation == null) {
                            lastLocation = location;
                            lastLocationUpdated = true;
                            trigger.changeLocation(location.getLatitude(), location.getLongitude());
                        }
                    }

                    if(lastLocationUpdated) {
                        sendMessageToActivity(activeMode, location);
                        if(activeMode) {
                            updateStickyNotification(location);
                        }
                    }
                }

                if(lastLocationUpdated) {
                    GeoSwitchApp.getGpsLog().log("Retrieved last known location " + location);
                } else {
                    GeoSwitchApp.getGpsLog().log("Last location is unknown, start from scratch");
                }
            }
        });
    }


    private void determineMode() {
        unregisterLocationManagerListener();
        removeStickyNotification();
        activeMode = false;

        if(PowerReceiver.isCharging(GeoSwitchApp.getAppContext())) {
            registerLocationManagerListener();
            displayStickyNotification();
            activeMode = true;
        }

        if(lastLocation == null){
            requestLastLocation();
        } else {
            sendMessageToActivity(activeMode, lastLocation);
            if(activeMode) {
                updateStickyNotification(lastLocation);
            }
        }
    }

    private GeoTrigger loadTrigger() {
        GeoTrigger trigger;
        if(GeoSwitchApp.getPreferences().getTriggerType() == TriggerType.Bidirectional) {
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
        assert(!activeMode);

        String message;
        if(location != null) {
            Date date = new Date(location.getTime());
            message = "Last GPS fix received at "+dateFormat.format(date);
        } else {
            message = "Waiting for GPS fix";
        }

        Intent notificationIntent = new Intent(this, ActivityMain.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        // make service a foreground service
        Notification notification = new Notification.Builder(this)
                .setContentTitle("GeoSwitch: monitoring location")
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setTicker("Ticket text")
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
        GeoSwitchApp.getGpsLog().log("Registering for GPS events");

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
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    private void unregisterLocationManagerListener() {
        if(locationManager != null) {
            locationManager.removeUpdates(this);
            GeoSwitchApp.getGpsLog().log("Unregistered from GPS events");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(!activeMode)
            return;

//        Log.i(TAG, "onLocationChanged: " + location);
        GeoSwitchApp.getGpsLog().log(location);
        lastLocation = location;

        if(trigger != null) {
            boolean triggered;

            // may be concurrently updated by retrieving last know location from Google Api
            synchronized (this) {
                trigger.changeLocation(location.getLatitude(), location.getLongitude());
                triggered = trigger.isTriggered();
            }

            updateStickyNotification(location);
            sendMessageToActivity(true, location);

            if (triggered) {
                GeoSwitchApp.getGpsLog().log("Area entered.");
                String notificationMessage = "You've entered the trigger area.";
                if(GeoSwitchApp.getPreferences().getActionEnabled()) {
                    executeAction();
                    notificationMessage += " Action started.";
                }

                GeoSwitchApp.getNotificationUtils().displayNotification(notificationMessage, false);
                GeoSwitchApp.getSpeachUtils().speak(notificationMessage);
            }
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