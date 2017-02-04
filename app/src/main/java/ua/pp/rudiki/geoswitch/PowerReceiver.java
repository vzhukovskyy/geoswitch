package ua.pp.rudiki.geoswitch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.widget.Toast;

public class PowerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String message;
        if(intent.getAction() == Intent.ACTION_POWER_CONNECTED) {
            message = "Power Connected";
        } else if(intent.getAction() == Intent.ACTION_POWER_DISCONNECTED){
            message = "Power Disconnected";
        } else {
            message = "Not recognized event";
        }

        GeoSwitchApp.getGpsLog().log(message);

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
