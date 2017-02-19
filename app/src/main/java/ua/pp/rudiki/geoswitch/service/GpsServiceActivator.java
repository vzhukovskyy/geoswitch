package ua.pp.rudiki.geoswitch.service;

import android.content.Context;
import android.content.Intent;

import ua.pp.rudiki.geoswitch.App;
import ua.pp.rudiki.geoswitch.peripherals.PowerReceiver;

public class GpsServiceActivator {
    private final static String TAG = GpsServiceActivator.class.getSimpleName();

    private GpsServiceActivationListener listener;

    public GpsServiceActivator() {
    }

    public void registerListener(GpsServiceActivationListener listener) {
        this.listener = listener;
    }

    public void connectedToCharger() {
        App.getLogger().info(TAG, "connectedToCharger");
        if(App.getPreferences().getActivateOnCharger())
            startService(true, true, GeoSwitchGpsService.START_REASON_START_OR_STOP);
    }

    public void disconnectedFromCharger() {
        App.getLogger().info(TAG, "disconnectedFromCharger");
        if(App.getPreferences().getActivateOnCharger())
            startService(false, true, GeoSwitchGpsService.START_REASON_START_OR_STOP);
    }

    public void switchedOnManually() {
        App.getLogger().info(TAG, "switchedOnManually");
        if(!App.getPreferences().getActivateOnCharger())
            startService(true, false, GeoSwitchGpsService.START_REASON_START_OR_STOP);
    }

    public void switchedOffManually() {
        App.getLogger().info(TAG, "switchedOffManually");
        if(!App.getPreferences().getActivateOnCharger())
            startService(false, false, GeoSwitchGpsService.START_REASON_START_OR_STOP);
    }

    public void activationModeChanged() {
        boolean activateOnCharger = App.getPreferences().getActivateOnCharger();
        App.getLogger().info(TAG, "activationModeChanged, activateOnCharger="+activateOnCharger);

        // if mode changed to manual, deactivate
        // if mode changed to onCharger, de/activate depending on whether connected to charger or not
        // in any case, turn off switch for manual mode
        if(activateOnCharger)
            startService(PowerReceiver.isCharging(), true, GeoSwitchGpsService.START_REASON_USER_CHANGED_ACTIVATION);
        else
            startService(false, false, GeoSwitchGpsService.START_REASON_USER_CHANGED_ACTIVATION);

        App.getPreferences().storeGpsManuallyActivated(false);
    }

    private void startService(boolean on, boolean byCharger, String reason) {
        Context appContext = App.getAppContext();

        Intent serviceIntent = new Intent(appContext, GeoSwitchGpsService.class);
        serviceIntent.putExtra(GeoSwitchGpsService.START_REASON_KEY, reason);
        appContext.startService(serviceIntent);

        if(listener != null) {
            if (on)
                listener.onActivated();
            else
                listener.onDeactivated();
        }

    }

    public boolean isOn() {
        if(App.getPreferences().getActivateOnCharger())
            return PowerReceiver.isCharging();
        else
            return App.getPreferences().getGpsManuallyActivated();
    }
}
