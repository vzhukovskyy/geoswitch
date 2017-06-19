package ua.pp.rudiki.geoswitch.peripherals;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import ua.pp.rudiki.geoswitch.App;

import static android.net.wifi.WifiManager.*;

public class WifiBroadcastReceiver extends BroadcastReceiver {
    private final static String TAG = WifiBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WIFI_STATE_CHANGED_ACTION.equals(action)) {
            // Broadcast intent action indicating that Wi-Fi has been enabled, disabled, enabling, disabling, or unknown.

            int currentWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
            int previousWifiState = intent.getIntExtra(WifiManager.EXTRA_PREVIOUS_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);

            App.getLogger().logWifi(TAG, "WIFI_STATE_CHANGED_ACTION state: "+wifiStateToString(currentWifiState));
        }
        else if (NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(EXTRA_NETWORK_INFO);
            NetworkInfo.DetailedState state = (networkInfo != null) ? networkInfo.getDetailedState() : null;

            App.getLogger().logWifi(TAG, "NETWORK_STATE_CHANGED_ACTION state=" + String.valueOf(state));
        }
        else {
            App.getLogger().logWifi(TAG, action);
        }

    }

    private boolean isConnectedViaWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) App.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiInfo.isConnected();
    }

    private String wifiStateToString(int state) {
        switch(state) {
            case WIFI_STATE_DISABLED:
                return "Disabled";
            case WIFI_STATE_DISABLING:
                return "Disabling";
            case WIFI_STATE_ENABLED:
                return "Enabled";
            case WIFI_STATE_ENABLING:
                return "Enabling";
            case WIFI_STATE_UNKNOWN:
                return "Unknown";
        }

        return "Unknown";
    }
}
