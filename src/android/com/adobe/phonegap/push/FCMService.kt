package com.adobe.phonegap.push

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.adobe.phonegap.push.PushPlugin.Companion.isActive
import com.adobe.phonegap.push.PushPlugin.Companion.isInForeground
import com.adobe.phonegap.push.PushPlugin.Companion.sendExtras
import com.adobe.phonegap.push.PushPlugin.Companion.setApplicationIconBadgeNumber
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.security.SecureRandom

private const val TAG = "${PushPlugin.PREFIX_TAG} (FCMService)"

/**
 * Firebase Cloud Messaging Service Class
 */
@Suppress("HardCodedStringLiteral")
@SuppressLint("NewApi", "LongLogTag", "LogConditional")
class FCMService : FirebaseMessagingService() {

    private val context: Context
        get() = applicationContext

    private val pushSharedPref: SharedPreferences
        get() = AndroidUtils.getPushSharedPref(context)

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
                showNotificationIfPossible(extras)
            } else {
                Log.d(TAG, "In Background")
                extras.putBoolean(PushConstants.COLDSTART, isActive)
                showNotificationIfPossible(extras)
            }
        }
    }

    private fun showNotificationIfPossible(extras: Bundle?) {
        // Send a notification if there is a message or title, otherwise just send data
        extras?.let {
            val message = it.getString(PushConstants.MESSAGE)
            val title = it.getString(PushConstants.TITLE)
            val contentAvailable = it.getString(PushConstants.CONTENT_AVAILABLE)
            val forceStart = it.getString(PushConstants.FORCE_START)
            val badgeCount = PushUtils.extractBadgeCount(extras)

            if (badgeCount >= 0) {
                setApplicationIconBadgeNumber(context, badgeCount)
            }

            if (badgeCount == 0) {
                val mNotificationManager =
                    getSystemService(NOTIFICATION_SERVICE) as NotificationManager
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
                    extras.putString(PushConstants.TITLE, AndroidUtils.getAppName(this))
                }

                createNotification(extras)
            }

            if (!isActive && forceStart == "1") {
                Log.d(TAG, "The app is not running, attempting to start in the background")

                val intent = Intent(this, PushHandlerActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(PushConstants.PUSH_BUNDLE, extras)
                    putExtra(PushConstants.START_IN_BACKGROUND, true)
                    putExtra(PushConstants.FOREGROUND, false)
                }

                startActivity(intent)
            } else if (contentAvailable == "1") {
                Log.d(
                    TAG,
                    "The app is not running and content available is true, sending notification event"
                )

                sendExtras(extras)
            }
        }
    }

    private fun createNotification(extras: Bundle?) {
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val appName = AndroidUtils.getAppName(this)
        val notId = PushUtils.parseNotificationIdToInt(extras)
        val notificationIntent = Intent(this, PushHandlerActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(PushConstants.PUSH_BUNDLE, extras)
            putExtra(PushConstants.NOT_ID, notId)
        }
        val random = SecureRandom()
        var requestCode = random.nextInt()
        val contentIntent = PendingIntent.getActivity(
            this,
            requestCode,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or NotificationUtils.FLAG_IMMUTABLE
        )
        val dismissedNotificationIntent = Intent(
            this,
            PushDismissedHandler::class.java
        ).apply {
            putExtra(PushConstants.PUSH_BUNDLE, extras)
            putExtra(PushConstants.NOT_ID, notId)
            putExtra(PushConstants.DISMISSED, true)

            action = PushConstants.PUSH_DISMISSED
        }

        requestCode = random.nextInt()

        val deleteIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            dismissedNotificationIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or NotificationUtils.FLAG_IMMUTABLE
        )

        val mBuilder: NotificationCompat.Builder =
            NotificationUtils.createNotificationBuilder(context, extras, mNotificationManager)

        mBuilder.setWhen(System.currentTimeMillis())
            .setContentTitle(extras?.getString(PushConstants.TITLE)?.fromHtml())
            .setTicker(extras?.getString(PushConstants.TITLE)?.fromHtml())
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
        PushUtils.setNotificationCount(extras, mBuilder)

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
}
