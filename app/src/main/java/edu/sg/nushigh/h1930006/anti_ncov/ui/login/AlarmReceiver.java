package edu.sg.nushigh.h1930006.anti_ncov.ui.login;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.BigTextStyle;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.Objects;

import edu.sg.nushigh.h1930006.anti_ncov.R;
import edu.sg.nushigh.h1930006.anti_ncov.SettingsUtil;

public class AlarmReceiver extends BroadcastReceiver {
    public static final int NOTIFICATION_ID = 888;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!SettingsUtil.getInstance(context).isNotificationsEnabled()) {
            return;
        }

        String notificationChannelId = NotificationUtil.createNotificationChannel(context);

        BigTextStyle bigTextStyle = new BigTextStyle()
                .bigText(context.getString(R.string.notification_desc))
                .setBigContentTitle(context.getString(R.string.notification_title));

        Intent notifyIntent = new Intent(context, LoginActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Builder notificationCompatBuilder =
                new NotificationCompat.Builder(context, notificationChannelId);

        Notification notification = notificationCompatBuilder
                .setStyle(bigTextStyle)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(
                        context.getResources(),
                        R.drawable.ic_notification))
                .setContentIntent(notifyPendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setCategory(Notification.CATEGORY_REMINDER)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        Objects.requireNonNull(context.getSystemService(NotificationManager.class))
                .notify(NOTIFICATION_ID, notification);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
