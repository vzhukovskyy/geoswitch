package ua.pp.rudiki.geoswitch.service;

import android.content.Context;
import android.content.Intent;

import ua.pp.rudiki.geoswitch.ActivityGpsOptions;
import ua.pp.rudiki.geoswitch.App;
import ua.pp.rudiki.geoswitch.peripherals.BluetoothBroadcastReceiver;
import ua.pp.rudiki.geoswitch.peripherals.PowerBroadcastReceiver;

import static ua.pp.rudiki.geoswitch.ActivityGpsOptions.GpsActivationType.Bluetooth;
import static ua.pp.rudiki.geoswitch.ActivityGpsOptions.GpsActivationType.Charger;
import static ua.pp.rudiki.geoswitch.ActivityGpsOptions.GpsActivationType.Manual;

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
        if(App.getPreferences().getGpsActivationOption() == Charger)
            startService(true, true, GeoSwitchGpsService.START_REASON_START_OR_STOP);
    }

    public void disconnectedFromCharger() {
        App.getLogger().info(TAG, "disconnectedFromCharger");
        if(App.getPreferences().getGpsActivationOption() == Charger)
            startService(false, true, GeoSwitchGpsService.START_REASON_START_OR_STOP);
    }

    public void connectedViaBluetooth() {
        App.getLogger().info(TAG, "connectedToBluetooth");
        if(App.getPreferences().getGpsActivationOption() == Bluetooth)
            startService(true, true, GeoSwitchGpsService.START_REASON_START_OR_STOP);
    }

    public void disconnectedViaBluetooth() {
        App.getLogger().info(TAG, "disconnectedFromBluetooth");
        if(App.getPreferences().getGpsActivationOption() == Bluetooth)
            startService(false, true, GeoSwitchGpsService.START_REASON_START_OR_STOP);
    }

    public void switchedOnManually() {
        App.getLogger().info(TAG, "switchedOnManually");
        if(App.getPreferences().getGpsActivationOption() == Manual)
            startService(true, false, GeoSwitchGpsService.START_REASON_START_OR_STOP);
    }

    public void switchedOffManually() {
        App.getLogger().info(TAG, "switchedOffManually");
        if(App.getPreferences().getGpsActivationOption() == Manual)
            startService(false, false, GeoSwitchGpsService.START_REASON_START_OR_STOP);
    }

    public void activationModeChanged() {
        ActivityGpsOptions.GpsActivationType activationType = App.getPreferences().getGpsActivationOption();
        App.getLogger().info(TAG, "activationModeChanged, type="+activationType);

        // if mode changed to manual, deactivate
        // if mode changed to onCharger, de/activate depending on whether connected to charger or not
        // in any case, turn off switch for manual mode
        switch(activationType) {
            case Manual:
                startService(false, false, GeoSwitchGpsService.START_REASON_USER_CHANGED_ACTIVATION);
                App.getPreferences().storeGpsManuallyActivated(false);
                break;
            case Charger:
                startService(PowerBroadcastReceiver.isCharging(), true, GeoSwitchGpsService.START_REASON_USER_CHANGED_ACTIVATION);
                break;
            case Bluetooth:
                startService(BluetoothBroadcastReceiver.isConnected(), true, GeoSwitchGpsService.START_REASON_USER_CHANGED_ACTIVATION);
                break;
        }
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
        switch(App.getPreferences().getGpsActivationOption()) {
            case Charger:
                return PowerBroadcastReceiver.isCharging();
            case Bluetooth:
                return BluetoothBroadcastReceiver.isConnected();
            case Manual:
                return App.getPreferences().getGpsManuallyActivated();
        }

        return false;
    }

}
