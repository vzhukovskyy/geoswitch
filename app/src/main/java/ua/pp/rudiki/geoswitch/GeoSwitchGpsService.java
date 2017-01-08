package ua.pp.rudiki.geoswitch;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;

import ua.pp.rudiki.geoswitch.trigger.AreaTrigger;
import ua.pp.rudiki.geoswitch.trigger.GeoArea;
import ua.pp.rudiki.geoswitch.trigger.HttpUtils;

public class GeoSwitchGpsService extends Service implements android.location.LocationListener
{
    final String TAG = getClass().getSimpleName();

    private LocationManager locationManager;
    Location lastLocation;

    AreaTrigger areaTrigger;

    private GpsLog gpsLog;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        gpsLog = new GpsLog(this);
        gpsLog.log("Service started");

        registerLocationManagerListener();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        gpsLog.log("Service destroyed");

        super.onDestroy();
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
                sendNotification("You've entered the trigger area");
                executeAction();
                gpsLog.log("Area entered.");
            }
            else if(areaTrigger.exited()){
                sendNotification("You've left the trigger area");
                gpsLog.log("Area exited.");
            }
        }
    }

    void sendNotification(String message) {
        Intent intent = new Intent(this, ConfigActivity.class);
        int notificationId = 1;
        NotificationUtils.displayNotification(this, notificationId, "Ticker", "GeoSwitch", message, intent);
        //Log.i(TAG, "notification displayed");
    }

    void executeAction() {
        String url = GeoSwitchApp.getPreferences().getUrl();
        GeoSwitchApp.getHttpUtils().sendPostAsync(url);
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
        Log.d(TAG, "onStartCommand, intent=" + intent);
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

        GeoSwitchApp.getGoogleSignIn().silentLogin();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}