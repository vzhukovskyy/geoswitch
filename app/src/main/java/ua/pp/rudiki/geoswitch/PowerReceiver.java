package ua.pp.rudiki.geoswitch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class PowerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String logMessage;
        if(intent.getAction() == Intent.ACTION_POWER_CONNECTED) {
            logMessage = "Power Connected";
        } else if(intent.getAction() == Intent.ACTION_POWER_DISCONNECTED){
            logMessage = "Power Disconnected";
        } else {
            logMessage = "Not recognized event";
        }

        GeoSwitchApp.getLogger().log(logMessage);

        Intent serviceIntent = new Intent(context, GeoSwitchGpsService.class);
        context.startService(serviceIntent);
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
