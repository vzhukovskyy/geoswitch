package ua.pp.rudiki.geoswitch.peripherals;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;

import ua.pp.rudiki.geoswitch.ActivityMain;
import ua.pp.rudiki.geoswitch.App;
import ua.pp.rudiki.geoswitch.R;

public class NotificationUtils {
    private static final String TAG = NotificationUtils.class.getSimpleName();

    final static int NOTIFICATION_ID = 0;
    final static int STICKY_NOTIFICATION_ID = 1;


    public Notification displayStickyNotification(String message) {
        Context context = App.getAppContext();

        Intent notificationIntent = new Intent(context, ActivityMain.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, STICKY_NOTIFICATION_ID, notificationIntent, 0);

        Notification notification = new Notification.Builder(context)
                .setContentTitle(context.getString(R.string.service_sticky_title))
                .setContentText(message)
                .setSmallIcon(R.mipmap.geoswitch_bold_inverse)
                .setShowWhen(false)
                .setContentIntent(pendingIntent)
                .build();
        return notification;
    }

    public void displayNotification(String text, boolean playSound) {
        Context context = App.getAppContext();

        Intent intent = new Intent(context, ActivityMain.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.geoswitch_bold_idea_inverse)
                .setColor(Color.rgb(0,100,0))
                .setContentTitle(context.getString(R.string.service_notification_title))
                .setContentText(text)
                .setContentIntent(resultPendingIntent)
                .setSound(playSound ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) : null)
                .setAutoCancel(true)
                .build();

        NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifyMgr.notify(NOTIFICATION_ID, notification);
    }
}
