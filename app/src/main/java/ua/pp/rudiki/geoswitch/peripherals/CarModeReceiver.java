package ua.pp.rudiki.geoswitch.peripherals;

import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;

import ua.pp.rudiki.geoswitch.App;

public class CarModeReceiver extends BroadcastReceiver {
    private static final String TAG = CarModeReceiver.class.getSimpleName();

    public static boolean isCarMode() {
        UiModeManager uiModeManager = (UiModeManager) App.getAppContext().getSystemService(Context.UI_MODE_SERVICE);
        return uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_CAR;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(UiModeManager.ACTION_ENTER_CAR_MODE)) {
                App.getLogger().logUiMode(TAG, "Entering car mode");
                App.getGpsServiceActivator().carModeEntered();
            } else if (action.equals(UiModeManager.ACTION_EXIT_CAR_MODE)) {
                App.getLogger().logUiMode(TAG, "Leaving car mode");
                App.getGpsServiceActivator().carModeExited();
            }
        }
    }
}
