package ua.pp.rudiki.geoswitch;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import java.sql.SQLOutput;

import ua.pp.rudiki.geoswitch.trigger.AreaTrigger;
import ua.pp.rudiki.geoswitch.trigger.GeoArea;
import ua.pp.rudiki.geoswitch.trigger.GeoPoint;

public class GeoSwitchGpsService extends Service implements android.location.LocationListener
{
    final String TAG = getClass().getSimpleName();

    private LocationManager locationManager;
    Location lastLocation;

    AreaTrigger areaTrigger;

    private GpsLog gpsLog;

    @Override
    public void onCreate() {
//        Log.i(TAG, "Service starting");

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

        areaTrigger.changeLocation(location.getLatitude(), location.getLongitude());
        if(areaTrigger.entered()) {
            sendNotification();
        }
    }

    void sendNotification() {
        Intent intent = new Intent(this, ConfigActivity.class);
        int notificationId = 1;
        NotificationUtils.displayNotification(this, notificationId, "Ticker", "GeoSwitch", "You've entered trigger area", intent);
        Log.i(TAG, "notification displayed");
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
//        super.onStartCommand(intent, flags, startId);

        double latitude = intent.getDoubleExtra(Preferences.latitudeKey, 0);
        double longitude = intent.getDoubleExtra(Preferences.longitudeKey, 0);
        double radius = intent.getDoubleExtra(Preferences.radiusKey, 0);

        GeoArea newArea = new GeoArea(latitude, longitude, radius);

        Log.i(TAG, "onStartCommand: new area "+newArea);
        Log.i(TAG, "onStartCommand: previous area "+((areaTrigger != null) ? areaTrigger.getArea() : "null"));

        areaTrigger = new AreaTrigger(newArea);

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