package ua.pp.rudiki.geoswitch.peripherals;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import ua.pp.rudiki.geoswitch.App;

public class PowerBroadcastReceiver extends BroadcastReceiver {
    private final static String TAG = PowerBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();

        if(intentAction.equals(Intent.ACTION_POWER_CONNECTED)) {
            //int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1); - does not work
            App.getLogger().logPower(TAG, "Power Connected via "+getChargingSource());
            App.getGpsServiceActivator().connectedToCharger();
        } else if(intentAction.equals(Intent.ACTION_POWER_DISCONNECTED)){
            App.getLogger().logPower(TAG, "Power Disconnected");
            App.getGpsServiceActivator().disconnectedFromCharger();
        }
    }

    public static boolean isCharging() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = App.getAppContext().registerReceiver(null, ifilter);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        return isCharging;
    }

    private String getChargingSource() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = App.getAppContext().registerReceiver(null, ifilter);

        // accordingly to my experiments difference between USB and AC is in charging power:
        // 2A is treated as AC, 500mA is treated as USB. Complete correspondence to
        // "Charging" and "Charging slowly via USB" notifications.
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        String source = "";
        if(chargePlug == BatteryManager.BATTERY_PLUGGED_USB)
            source = "USB";
        else if (chargePlug == BatteryManager.BATTERY_PLUGGED_AC)
            source = "AC";
        else if (chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS)
            source = "Wireless";
        else
            source = "Unknown";

        return source;
    }
}
