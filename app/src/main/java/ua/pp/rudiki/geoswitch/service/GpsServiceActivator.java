package ua.pp.rudiki.geoswitch.service;

import android.content.Context;
import android.content.Intent;

import ua.pp.rudiki.geoswitch.GeoSwitchApp;
import ua.pp.rudiki.geoswitch.peripherals.PowerReceiver;

public class GpsServiceActivator {
    private Context context;
    private GpsServiceActivationListener listener;

    public GpsServiceActivator(Context context) {
        this.context = context;
    }

    public void registerListener(GpsServiceActivationListener listener) {
        this.listener = listener;
    }

    public void connectedToCharger() {
        if(GeoSwitchApp.getPreferences().getActivateOnCharger())
            startService(true, true);
    }

    public void disconnectedFromCharger() {
        if(GeoSwitchApp.getPreferences().getActivateOnCharger())
            startService(false, true);
    }

    public void switchedOnManually() {
        if(!GeoSwitchApp.getPreferences().getActivateOnCharger())
            startService(true, false);
    }

    public void switchedOffManually() {
        if(!GeoSwitchApp.getPreferences().getActivateOnCharger())
            startService(false, false);
    }

    public void activationModeChanged() {
        // if mode changed to manual, deactivate
        // if mode changed to onCharger, de/activate depending on whether connected to charger or not
        // in any case, turn off switch for manual mode
        if(GeoSwitchApp.getPreferences().getActivateOnCharger())
            startService(PowerReceiver.isCharging(context), true);
        else
            startService(false, false);

        GeoSwitchApp.getPreferences().storeGpsManuallyActivated(false);
    }

    private void startService(boolean on, boolean byCharger) {
        Intent serviceIntent = new Intent(context, GeoSwitchGpsService.class);
        context.startService(serviceIntent);

        if(listener != null) {
            if (on)
                listener.onActivated();
            else
                listener.onDeactivated();
        }

    }

    public boolean isOn() {
        if(GeoSwitchApp.getPreferences().getActivateOnCharger())
            return PowerReceiver.isCharging(context);
        else
            return GeoSwitchApp.getPreferences().getGpsManuallyActivated();
    }
}
