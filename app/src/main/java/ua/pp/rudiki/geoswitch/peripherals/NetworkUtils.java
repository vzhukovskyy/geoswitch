package ua.pp.rudiki.geoswitch.peripherals;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import ua.pp.rudiki.geoswitch.App;

public class NetworkUtils {
    public static boolean isConnectedToInternet() {
        Context context = App.getAppContext();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnected();
    }
}
