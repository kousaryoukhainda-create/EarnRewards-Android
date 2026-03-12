// EarnRewards/src/com/ykapps/earnrewards/services/EarnRewardsMessagingService.java

package com.ykapps.earnrewards.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ykapps.earnrewards.MainActivity;
import com.ykapps.earnrewards.R;

/**
 * EarnRewardsMessagingService - Handles Firebase Cloud Messaging (FCM) notifications
 */
public class EarnRewardsMessagingService extends FirebaseMessagingService {
    
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "earnrewards_notifications";
    
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());
        
        // Handle data message
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleDataMessage(remoteMessage.getData());
        }
        
        // Handle notification message
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message notification body: " + remoteMessage.getNotification().getBody());
            handleNotificationMessage(remoteMessage.getNotification());
        }
    }
    
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        
        // Send token to your server
        sendRegistrationToServer(token);
    }
    
    @Override
    public void onMessageSent(@NonNull String msgId) {
        Log.d(TAG, "Message sent with ID: " + msgId);
    }
    
    @Override
    public void onSendError(@NonNull String msgId, @NonNull Exception exception) {
        Log.e(TAG, "Message failed to send with ID: " + msgId, exception);
    }
    
    private void handleDataMessage(java.util.Map<String, String> data) {
        String messageType = data.get("type");
        String title = data.get("title");
        String body = data.get("body");
        String deepLink = data.get("deepLink");
        
        Log.d(TAG, "Handling data message: type=" + messageType);
        
        switch (messageType) {
            case "reward_notification":
                handleRewardNotification(title, body, data);
                break;
            
            case "promotion":
                handlePromotionNotification(title, body, deepLink);
                break;
            
            case "referral":
                handleReferralNotification(title, body, data);
                break;
            
            case "maintenance":
                handleMaintenanceNotification(title, body);
                break;
            
            default:
                handleGenericNotification(title, body);
                break;
        }
    }
    
    private void handleNotificationMessage(RemoteMessage.Notification notification) {
        String title = notification.getTitle();
        String body = notification.getBody();
        
        sendNotification(title, body, null);
    }
    
    private void handleRewardNotification(String title, String body, java.util.Map<String, String> data) {
        String rewardAmount = data.get("reward_amount");
        String rewardType = data.get("reward_type");
        
        String fullTitle = title != null ? title : "Reward Alert!";
        String fullBody = body != null ? body : "You earned " + rewardAmount + " coins!";
        
        sendNotification(fullTitle, fullBody, data);
    }
    
    private void handlePromotionNotification(String title, String body, String deepLink) {
        sendNotificationWithDeepLink(
            title != null ? title : "Special Offer",
            body != null ? body : "Check out our latest offers!",
            deepLink
        );
    }
    
    private void handleReferralNotification(String title, String body, java.util.Map<String, String> data) {
        String referralBonus = data.get("bonus_amount");
        
        sendNotification(
            title != null ? title : "Referral Bonus!",
            body != null ? body : "You earned " + referralBonus + " coins from a referral!",
            data
        );
    }
    
    private void handleMaintenanceNotification(String title, String body) {
        sendNotification(
            title != null ? title : "Maintenance Notice",
            body != null ? body : "App maintenance scheduled. Please try again later.",
            null
        );
    }
    
    private void handleGenericNotification(String title, String body) {
        sendNotification(
            title != null ? title : "EarnRewards",
            body != null ? body : "You have a new notification",
            null
        );
    }
    
    private void sendNotification(String title, String body, java.util.Map<String, String> data) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        if (data != null && !data.isEmpty()) {
            for (String key : data.keySet()) {
                intent.putExtra(key, data.get(key));
            }
        }
        
        int notificationId = (int) System.currentTimeMillis();
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        
        NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentBody(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        
        NotificationManager notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        createNotificationChannel(notificationManager);
        notificationManager.notify(notificationId, notificationBuilder.build());
        
        Log.d(TAG, "Notification sent: " + title);
    }
    
    private void sendNotificationWithDeepLink(String title, String body, String deepLink) {
        Intent intent;
        
        if (deepLink != null && !deepLink.isEmpty()) {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(deepLink));
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        int notificationId = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        
        NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentBody(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        
        NotificationManager notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        createNotificationChannel(notificationManager);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
    
    private void createNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "EarnRewards Notifications";
            String channelDescription = "Notifications from EarnRewards";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, channelName, importance
            );
            channel.setDescription(channelDescription);
            channel.enableVibration(true);
            channel.enableLights(true);
            
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    private void sendRegistrationToServer(String token) {
        // Save token to Firebase or your backend server
        // This would typically save to Firebase Realtime Database under /users/{userId}/fcmToken
        Log.d(TAG, "Token saved to server: " + token);
    }
}
