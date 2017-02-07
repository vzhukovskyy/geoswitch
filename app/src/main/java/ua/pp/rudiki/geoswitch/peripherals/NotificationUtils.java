package ua.pp.rudiki.geoswitch.peripherals;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import ua.pp.rudiki.geoswitch.ActivityTrigger;
import ua.pp.rudiki.geoswitch.GeoSwitchApp;
import ua.pp.rudiki.geoswitch.R;

public class NotificationUtils {
    final String TAG = getClass().getSimpleName();

    public void displayNotification(String text, boolean playRingtone) {
        Context context = GeoSwitchApp.getAppContext();

        Intent intent = new Intent(context, ActivityTrigger.class);
        int notificationId = 1;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("GeoSwitch")
                .setContentText(text);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        mBuilder.setOnlyAlertOnce(true);

        NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notifyMgr.notify(notificationId, mBuilder.build());

        if(playRingtone) {
            RingtoneUtils.playRingtone();
        }
    }
}
