package com.playtube.protube.video.music.ads;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import com.playtube.protube.video.music.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String FCM_PARAM = "picture";
    private static final String CHANNEL_NAME = "FCM";
    private static final String CHANNEL_DESC = "Firebase Cloud Messaging";
    private int numMessages = 0;
    String TAG = "MyFirebaseMess";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Map<String, String> data = remoteMessage.getData();
        Log.e("FROM", remoteMessage.getFrom());
        assert notification != null;
        try {
            sendNotification(notification, data);
        } catch (Exception e) {
            Log.e(TAG, "KLKLKLKLKL onMessageReceived: " + e.getMessage());
        }
    }

    private void sendNotification(RemoteMessage.Notification notification, Map<String, String> data) {
        Bundle bundle = new Bundle();
        bundle.putString(FCM_PARAM, data.get(FCM_PARAM));

        Intent intent = null;
        intent = new Intent(this, SplashActivity.class);

        intent.putExtras(bundle);
        PendingIntent pendingIntent = null;
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setContentInfo("Hello")
                .setColor(Color.BLUE)
                .setLights(Color.RED, 1000, 300)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setNumber(++numMessages)
                .setSmallIcon(R.drawable.ic_notifications);

        try {
            String picture = data.get(FCM_PARAM);
            if (picture != null && !"".equals(picture)) {
                URL url = new URL(picture);
                Bitmap bigPicture = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                notificationBuilder.setStyle(
                        new NotificationCompat.BigPictureStyle().bigPicture(bigPicture).setSummaryText(notification.getBody())
                );
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        getString(R.string.notification_channel_id), CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
                );
                channel.setDescription(CHANNEL_DESC);
                channel.setShowBadge(true);
                channel.canShowBadge();
                channel.enableLights(true);
                channel.setLightColor(Color.RED);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});

                assert notificationManager != null;
                notificationManager.createNotificationChannel(channel);
            }

            assert notificationManager != null;
            notificationManager.notify(0, notificationBuilder.build());
        } catch (
                Exception e) {
            e.printStackTrace();
            Log.e(TAG, "sendNotification: " + e.getMessage());

        }
    }
}