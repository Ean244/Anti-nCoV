package edu.sg.nushigh.h1930006.anti_ncov.ui.login;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import java.util.Objects;

public class NotificationUtil {
    private static final String CHANNEL_ID = "channel_reminder_1";
    private static final String CHANNEL_NAME = "Temperature Reminder";
    private static final String CHANNEL_DESC = "Temperature Reminder Channel";

    public static String createNotificationChannel(Context context) {
        // Initializes NotificationChannel.
        NotificationChannel notificationChannel =
                new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription(CHANNEL_DESC);
        notificationChannel.enableVibration(true);
        notificationChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        // Adds NotificationChannel to system. Attempting to create an existing notification
        // channel with its original values performs no operation, so it's safe to perform the
        // below sequence.
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Objects.requireNonNull(notificationManager).createNotificationChannel(notificationChannel);
        return CHANNEL_ID;
    }
}