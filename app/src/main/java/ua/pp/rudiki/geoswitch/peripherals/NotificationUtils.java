package ua.pp.rudiki.geoswitch.peripherals;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;

import ua.pp.rudiki.geoswitch.ActivityMain;
import ua.pp.rudiki.geoswitch.GeoSwitchApp;
import ua.pp.rudiki.geoswitch.R;

public class NotificationUtils {
    final String TAG = getClass().getSimpleName();
    final static int NOTIFICATION_ID = 0;
    final static int STICKY_NOTIFICATION_ID = 1;


    public Notification displayStickyNotification(String message) {
        Context context = GeoSwitchApp.getAppContext();

        Intent notificationIntent = new Intent(context, ActivityMain.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, STICKY_NOTIFICATION_ID, notificationIntent, 0);

        Notification notification = new Notification.Builder(context)
                .setContentTitle(context.getString(R.string.sticky_title))
                .setContentText(message)
                .setSmallIcon(R.mipmap.geoswitch_bold_inverse)
                .setShowWhen(false)
                .setContentIntent(pendingIntent)
                .build();
        return notification;
    }

    public void displayNotification(String text) {
        Context context = GeoSwitchApp.getAppContext();

        Intent intent = new Intent(context, ActivityMain.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.geoswitch_bold_inverse)
                .setColor(Color.rgb(0,100,0))
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(text)
                .setContentIntent(resultPendingIntent)
                //.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true)
                .build();

        NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifyMgr.notify(NOTIFICATION_ID, notification);
    }
}
