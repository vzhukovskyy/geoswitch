package ua.pp.rudiki.geoswitch.peripherals;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import ua.pp.rudiki.geoswitch.App;

public class RingtoneUtils {
    private static final String TAG = RingtoneUtils.class.getSimpleName();

    public static void playRingtone() {
        App.getLogger().info(TAG, "Playing ringtone");
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(App.getAppContext(), notification);
            r.play();
        } catch (Exception e) {
            App.getLogger().exception(TAG, e);
        }

    }
}
