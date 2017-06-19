package ua.pp.rudiki.geoswitch.peripherals;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import ua.pp.rudiki.geoswitch.App;

public class PowerReceiver extends BroadcastReceiver {
    private final static String TAG = PowerReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();

        if(intentAction.equals(Intent.ACTION_POWER_CONNECTED)) {
            App.getLogger().info(TAG, "Power Connected");
            App.getGpsServiceActivator().connectedToCharger();
        } else if(intentAction.equals(Intent.ACTION_POWER_DISCONNECTED)){
            App.getLogger().info(TAG, "Power Disconnected");
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
}
