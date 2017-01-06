package ua.pp.rudiki.geoswitch;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import ua.pp.rudiki.geoswitch.trigger.AreaTrigger;
import ua.pp.rudiki.geoswitch.trigger.GeoArea;

public class GeoSwitchGpsService extends Service implements android.location.LocationListener
{
    final String TAG = getClass().getSimpleName();

    private LocationManager locationManager;
    Location lastLocation;

    AreaTrigger areaTrigger;

    private GpsLog gpsLog;

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");

        gpsLog = new GpsLog(this);

        registerLocationManagerListener();
    }

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
        lastLocation = location;
        gpsLog.log(location);

        if(areaTrigger != null) {
            areaTrigger.changeLocation(location.getLatitude(), location.getLongitude());
            if (areaTrigger.entered()) {
                sendNotification();
                gpsLog.log("Area entered. Notification displayed");
            }
        }
    }

    void sendNotification() {
        Intent intent = new Intent(this, ConfigActivity.class);
        int notificationId = 1;
        NotificationUtils.displayNotification(this, notificationId, "Ticker", "GeoSwitch", "You've entered trigger area", intent);
        //Log.i(TAG, "notification displayed");
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

    // ***********************************************
    // ***** Service overrides
    // ***********************************************

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand, intent=" + intent);
        GeoArea area = GeoSwitchApp.getPreferences().loadArea();

        boolean switchToNewArea = false;
        if (areaTrigger != null) {
            // there is already configured areaTrigger
            if(area != null && !areaTrigger.getArea().equals(area)) {
                // there is different area to monitor
                switchToNewArea = true;
            }
        }
        else {
            // no areaTrigger being monitored yet
            if(area != null) {
                // and there is area to monitor
                switchToNewArea = true;
            }
        }

        if(switchToNewArea) {
            areaTrigger = new AreaTrigger(area);
            gpsLog.log("Starting monitoring "+area);
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");

        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException ex) {
            Log.i(TAG, "fail to remove location listener, ignore", ex);
        }

        super.onDestroy();
    }
}