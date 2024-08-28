package com.adobe.phonegap.push

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.adobe.phonegap.push.AndroidUtils.getPushSharedPref
import com.adobe.phonegap.push.PushPlugin.Companion.isActive
import com.adobe.phonegap.push.PushPlugin.Companion.isInForeground
import com.adobe.phonegap.push.PushPlugin.Companion.sendExtras
import com.adobe.phonegap.push.PushPlugin.Companion.setApplicationIconBadgeNumber
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.security.SecureRandom
import java.util.*

/**
 * Firebase Cloud Messaging Service Class
 */
@Suppress("HardCodedStringLiteral")
@SuppressLint("NewApi", "LongLogTag", "LogConditional")
class FCMService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "${PushPlugin.PREFIX_TAG} (FCMService)"
    }

    private val context: Context
        get() = applicationContext

    private val pushSharedPref: SharedPreferences
        get() = getPushSharedPref(context)

    /**
     * Called when a new token is generated, after app install or token changes.
     *
     * @param token
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")

        // TODO: Implement this method to send any registration to your app's servers.
        //sendRegistrationToServer(token);
    }

    /**
     * On Message Received
     */
    override fun onMessageReceived(message: RemoteMessage) {
        val from = message.from
        Log.d(TAG, "onMessageReceived (from=$from)")

        var extras = Bundle()

        message.notification?.let {
            extras.putString(PushConstants.TITLE, it.title)
            extras.putString(PushConstants.MESSAGE, it.body)
            extras.putString(PushConstants.SOUND, it.sound)
            extras.putString(PushConstants.ICON, it.icon)
            extras.putString(PushConstants.COLOR, it.color)
        }

        for ((key, value) in message.data) {
            extras.putString(key, value)
        }

        if (PushUtils.isAvailableSender(pushSharedPref, from)) {
            val messageKey =
                pushSharedPref.getString(PushConstants.MESSAGE_KEY, PushConstants.MESSAGE)
            val titleKey = pushSharedPref.getString(PushConstants.TITLE_KEY, PushConstants.TITLE)

            extras = PushUtils.normalizeExtras(context, extras, messageKey, titleKey)

            // Clear Badge
            val clearBadge = pushSharedPref.getBoolean(PushConstants.CLEAR_BADGE, false)
            if (clearBadge) {
                setApplicationIconBadgeNumber(context, 0)
            }

            if ("true" == message.data["voip"]) {
                if ("true" == message.data["isCancelPush"]) {
                    IncomingCallHelper.dismissVOIPNotification(context)
                    IncomingCallActivity.dismissUnlockScreenNotification(this.applicationContext)
                } else {
                    showVOIPNotification(context, message.data)
                }
            } else {
                // Foreground
                extras.putBoolean(PushConstants.FOREGROUND, isInForeground)

                // if we are in the foreground and forceShow is `false` only send data
                val forceShow = pushSharedPref.getBoolean(PushConstants.FORCE_SHOW, false)
                if (!forceShow && isInForeground) {
                    Log.d(TAG, "Do Not Force & Is In Foreground")
                    extras.putBoolean(PushConstants.COLDSTART, false)
                    sendExtras(extras)
                } else if (forceShow && isInForeground) {
                    Log.d(TAG, "Force & Is In Foreground")
                    extras.putBoolean(PushConstants.COLDSTART, false)
                    showNotificationIfPossible(context, extras)
                } else {
                    Log.d(TAG, "In Background")
                    extras.putBoolean(PushConstants.COLDSTART, isActive)
                    showNotificationIfPossible(context, extras)
                }
            }
        }
    }

    private fun createNotification(context: Context, extras: Bundle?) {
        val mNotificationManager =
            context.getSystemService(FirebaseMessagingService.NOTIFICATION_SERVICE) as NotificationManager
        val appName = AndroidUtils.getAppName(context)
        val notId = PushUtils.parseNotificationIdToInt(extras)
        val notificationIntent = Intent(context, PushHandlerActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(PushConstants.PUSH_BUNDLE, extras)
            putExtra(PushConstants.NOT_ID, notId)
        }
        val random = SecureRandom()
        var requestCode = random.nextInt()

        val contentIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            TaskStackBuilder.create(context).run {
                addNextIntentWithParentStack(notificationIntent)
                PendingIntent.getActivity(
                    context,
                    requestCode,
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or NotificationUtils.FLAG_IMMUTABLE
                )
            }
        } else {
            PendingIntent.getActivity(
                context,
                requestCode,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or NotificationUtils.FLAG_IMMUTABLE
            )
        }

        val dismissedNotificationIntent = Intent(
            context,
            PushDismissedHandler::class.java
        ).apply {
            putExtra(PushConstants.PUSH_BUNDLE, extras)
            putExtra(PushConstants.NOT_ID, notId)
            putExtra(PushConstants.DISMISSED, true)

            action = PushConstants.PUSH_DISMISSED
        }

        requestCode = random.nextInt()

        val deleteIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            dismissedNotificationIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or NotificationUtils.FLAG_IMMUTABLE
        )

        val mBuilder: NotificationCompat.Builder =
            NotificationUtils.createNotificationBuilder(context, extras, mNotificationManager)

        mBuilder.setWhen(System.currentTimeMillis())
            .setContentTitle(extras?.getString(PushConstants.TITLE)?.fromHtml())
            .setTicker((extras?.getString(PushConstants.TITLE)?.fromHtml()))
            .setContentIntent(contentIntent)
            .setDeleteIntent(deleteIntent)
            .setAutoCancel(true)

        val localIcon = pushSharedPref.getString(PushConstants.ICON, null)
        val localIconColor = pushSharedPref.getString(PushConstants.ICON_COLOR, null)
        val soundOption = pushSharedPref.getBoolean(PushConstants.SOUND, true)
        val vibrateOption = pushSharedPref.getBoolean(PushConstants.VIBRATE, true)

        Log.d(TAG, "stored icon=$localIcon")
        Log.d(TAG, "stored iconColor=$localIconColor")
        Log.d(TAG, "stored sound=$soundOption")
        Log.d(TAG, "stored vibrate=$vibrateOption")

        /*
         * Notification Vibration
         */
        NotificationUtils.setNotificationVibration(extras, vibrateOption, mBuilder)

        /*
         * Notification Icon Color
         *
         * Sets the small-icon background color of the notification.
         * To use, add the `iconColor` key to plugin android options
         */
        PushUtils.setNotificationIconColor(
            extras?.getString(PushConstants.COLOR),
            mBuilder,
            localIconColor
        )

        /*
         * Notification Icon
         *
         * Sets the small-icon of the notification.
         *
         * - checks the plugin options for `icon` key
         * - if none, uses the application icon
         *
         * The icon value must be a string that maps to a drawable resource.
         * If no resource is found, falls
         */
        PushUtils.setNotificationSmallIcon(context, extras, mBuilder, localIcon)

        /*
         * Notification Large-Icon
         *
         * Sets the large-icon of the notification
         *
         * - checks the gcm data for the `image` key
         * - checks to see if remote image, loads it.
         * - checks to see if assets image, Loads It.
         * - checks to see if resource image, LOADS IT!
         * - if none, we don't set the large icon
         */
        PushUtils.setNotificationLargeIcon(context, extras, mBuilder)

        /*
         * Notification Sound
         */
        if (soundOption) {
            NotificationUtils.setNotificationSound(context, extras, mBuilder)
        }

        /*
         *  LED Notification
         */
        NotificationUtils.setNotificationLedColor(extras, mBuilder)

        /*
         *  Priority Notification
         */
        NotificationUtils.setNotificationPriority(extras, mBuilder)

        /*
         * Notification message
         */
        NotificationUtils.setNotificationMessage(notId, extras, mBuilder)

        /*
         * Notification count
         */
        NotificationUtils.setNotificationCount(extras, mBuilder)

        /*
         *  Notification ongoing
         */
        NotificationUtils.setNotificationOngoing(extras, mBuilder)

        /*
         * Notification count
         */
        NotificationUtils.setVisibility(extras, mBuilder)

        /*
         * Notification add actions
         */
        NotificationUtils.createActions(context, extras, mBuilder, notId)
        mNotificationManager.notify(appName, notId, mBuilder.build())
    }

    private fun showVOIPNotification(context: Context, messageData: Map<String, String>) {
        NotificationUtils.createNotificationChannel(context)

        // Prepare data from messageData
        var caller: String? = "Unknown caller"
        if (messageData.containsKey("caller")) {
            caller = messageData["caller"]
        }
        val callId = messageData["callId"]
        val callbackUrl = messageData["callbackUrl"]

        // Read the message title from messageData
        var title: String? = "Eingehender Anruf"
        if (messageData.containsKey("body")) {
            title = messageData["body"]
        }

        // Update Webhook status to CONNECTED
        IncomingCallHelper.updateWebhookVOIPStatus(
            callbackUrl,
            callId,
            IncomingCallActivity.VOIP_CONNECTED
        )

        // Intent for LockScreen or tapping on notification
        val fullScreenIntent = Intent(context, IncomingCallActivity::class.java)
        fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        fullScreenIntent.putExtra("caller", caller)
        fullScreenIntent.putExtra(IncomingCallHelper.EXTRA_CALLBACK_URL, callbackUrl)
        fullScreenIntent.putExtra(IncomingCallHelper.EXTRA_CALL_ID, callId)

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, 0, fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Intent for tapping on Answer
        val acceptIntent = Intent(context, IncomingCallActionHandlerActivity::class.java)
        acceptIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        acceptIntent.putExtra(IncomingCallHelper.EXTRA_BUTTON_ACTION, IncomingCallActivity.VOIP_ACCEPT)
        acceptIntent.putExtra(IncomingCallHelper.EXTRA_CALLBACK_URL, callbackUrl)
        acceptIntent.putExtra(IncomingCallHelper.EXTRA_CALL_ID, callId)

        val acceptPendingIntent = PendingIntent.getActivity(context, 10,
            acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent for tapping on Reject
        val declineIntent = Intent(context, IncomingCallActionHandlerActivity::class.java)
        declineIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        declineIntent.putExtra(IncomingCallHelper.EXTRA_BUTTON_ACTION, IncomingCallActivity.VOIP_DECLINE)
        declineIntent.putExtra(IncomingCallHelper.EXTRA_CALLBACK_URL, callbackUrl)
        declineIntent.putExtra(IncomingCallHelper.EXTRA_CALL_ID, callId)

        val declinePendingIntent = PendingIntent.getActivity(
            context, 20,
            declineIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val pushicon = resources.getIdentifier("pushicon", "drawable", packageName)
        val notificationBuilder =
            NotificationCompat.Builder(context, NotificationUtils.CHANNEL_VOIP)
                .setSmallIcon(pushicon)
                .setContentTitle(title)
                .setContentText(caller)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(fullScreenPendingIntent, true) // Show Accept button
                .addAction(
                    NotificationCompat.Action(
                        0,
                        "Annehmen",
                        acceptPendingIntent
                    )
                ) // Show decline action
                .addAction(
                    NotificationCompat.Action(
                        0,
                        "Ablehnen",
                        declinePendingIntent
                    )
                ) // Make notification dismiss on user input action
                .setAutoCancel(true) // Cannot be swiped by user
                .setOngoing(true) // Set ringtone to notification (< Android O)
                .setSound(NotificationUtils.defaultRingtoneUri())
        val incomingCallNotification: Notification = notificationBuilder.build()
        val notificationManager = NotificationManagerCompat.from(context)

        // Display notification
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(NotificationUtils.VOIP_NOTIFICATION_ID, incomingCallNotification)
    }

    // END of VoIP implementation

    private fun showNotificationIfPossible(context: Context, extras: Bundle?) {
        // Send a notification if there is a message or title, otherwise just send data
        extras?.let {
            val message = it.getString(PushConstants.MESSAGE)
            val title = it.getString(PushConstants.TITLE)
            val contentAvailable = it.getString(PushConstants.CONTENT_AVAILABLE)
            val forceStart = it.getString(PushConstants.FORCE_START)
            val badgeCount = PushUtils.extractBadgeCount(extras)

            if (badgeCount >= 0) {
                PushPlugin.setApplicationIconBadgeNumber(context, badgeCount)
            }

            if (badgeCount == 0) {
                val mNotificationManager =
                    context.getSystemService(FirebaseMessagingService.NOTIFICATION_SERVICE) as NotificationManager
                mNotificationManager.cancelAll()
            }

            Log.d(TAG, "message=$message")
            Log.d(TAG, "title=$title")
            Log.d(TAG, "contentAvailable=$contentAvailable")
            Log.d(TAG, "forceStart=$forceStart")
            Log.d(TAG, "badgeCount=$badgeCount")

            val hasMessage = !message.isNullOrEmpty()
            val hasTitle = !title.isNullOrEmpty()

            if (hasMessage || hasTitle) {
                Log.d(TAG, "Create Notification")

                if (!hasTitle) {
                    extras.putString(PushConstants.TITLE, AndroidUtils.getAppName(context))
                }

                createNotification(context, extras)
            }

            if (!isActive && forceStart == "1") {
                Log.d(TAG, "The app is not running, attempting to start in the background")

                val intent = Intent(context, PushHandlerActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(PushConstants.PUSH_BUNDLE, extras)
                    putExtra(PushConstants.START_IN_BACKGROUND, true)
                    putExtra(PushConstants.FOREGROUND, false)
                }

                context.startActivity(intent)
            } else if (contentAvailable == "1") {
                Log.d(
                    TAG,
                    "The app is not running and content available is true, sending notification event"
                )
                PushPlugin.sendExtras(extras)
            }
        }
    }
}
