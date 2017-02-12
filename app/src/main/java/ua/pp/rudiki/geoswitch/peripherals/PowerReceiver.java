package ua.pp.rudiki.geoswitch.peripherals;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import ua.pp.rudiki.geoswitch.GeoSwitchApp;

public class PowerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() == Intent.ACTION_POWER_CONNECTED) {
            GeoSwitchApp.getLogger().log("Power Connected");
            GeoSwitchApp.getGpsServiceActivator().connectedToCharger();
        } else if(intent.getAction() == Intent.ACTION_POWER_DISCONNECTED){
            GeoSwitchApp.getLogger().log("Power Disconnected");
            GeoSwitchApp.getGpsServiceActivator().disconnectedFromCharger();
        }
    }

    public static boolean isCharging(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        return isCharging;
    }
}
