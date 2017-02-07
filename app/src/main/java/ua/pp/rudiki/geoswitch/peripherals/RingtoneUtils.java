package ua.pp.rudiki.geoswitch.peripherals;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import ua.pp.rudiki.geoswitch.GeoSwitchApp;

public class RingtoneUtils {
    private static final String TAG = RingtoneUtils.class.getSimpleName();

    public static void playRingtone() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(GeoSwitchApp.getAppContext(), notification);
            r.play();
        } catch (Exception e) {
            Log.e(TAG, "Exception playing ringtone", e);
        }

    }
}
