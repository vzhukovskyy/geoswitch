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
import android.util.Log;

import ua.pp.rudiki.geoswitch.action.ActionExecutor;
import ua.pp.rudiki.geoswitch.trigger.A2BTrigger;
import ua.pp.rudiki.geoswitch.trigger.AreaTrigger;
import ua.pp.rudiki.geoswitch.trigger.GeoArea;
import ua.pp.rudiki.geoswitch.trigger.GeoPoint;
import ua.pp.rudiki.geoswitch.trigger.GeoTrigger;
import ua.pp.rudiki.geoswitch.trigger.TriggerType;

public class GeoSwitchGpsService extends Service implements android.location.LocationListener
{
    final String TAG = getClass().getSimpleName();
    private final int ONGOING_NOTIFICATION_ID = 9004;

    private LocationManager locationManager;

    GeoTrigger trigger;

    // ***********************************************
    // ***** Android Service overrides
    // ***********************************************

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        trigger = loadTrigger();

        // if service restarted, continue tracking from last known position
        GeoPoint lastLocation = getLastLocation();
        if(lastLocation != null) {
            trigger.changeLocation(lastLocation.latitude, lastLocation.longitude);
            GeoSwitchApp.getGpsLog().log("Service created. Continue from "+lastLocation);
        } else {
            GeoSwitchApp.getGpsLog().log("Service created. No last position known, start from scratch");
        }

        registerLocationManagerListener();

        updateNotification();
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
            //GeoSwitchApp.getShortGpsLog().log("Continue monitoring "+area);
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
        GeoSwitchApp.getGpsLog().log("Service destroyed");

        super.onDestroy();
    }

    // ***********************************************
    // ***** Class-specific methods
    // ***********************************************

    GeoPoint getLastLocation() {
        return GeoSwitchApp.getPreferences().loadLastLocation();
    }

    GeoTrigger loadTrigger() {
        GeoTrigger trigger;
        if(GeoSwitchApp.getPreferences().getTriggerType() == TriggerType.Bidirectional) {
            trigger = GeoSwitchApp.getPreferences().loadAreaTrigger();
        } else {
            trigger = GeoSwitchApp.getPreferences().loadA2BTrigger();
        }

        return trigger;
    }

    private void updateNotification() {
        Intent notificationIntent = new Intent(this, ActivityMain.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        // make service a foreground service
        Notification notification = new Notification.Builder(this)
                .setContentTitle("GeoSwitch")
                .setContentText("Monitoring location")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setTicker("Ticket text")
                .build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }



    // ***********************************************
    // ***** GPS location tracking
    // ***********************************************

    private void registerLocationManagerListener() {

        final int LOCATION_INTERVAL = 1000;
        final float LOCATION_DISTANCE = 10f;

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, this);
        } catch (SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
//        Log.i(TAG, "onLocationChanged: " + location);
        GeoSwitchApp.getPreferences().storeLastLocation(location.getLatitude(), location.getLongitude());
        GeoSwitchApp.getGpsLog().log(location);

        if(trigger != null) {
            trigger.changeLocation(location.getLatitude(), location.getLongitude());
            updateNotification();
            if (trigger.isTriggered()) {
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