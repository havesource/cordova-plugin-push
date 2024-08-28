package com.adobe.phonegap.push

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.core.app.TaskStackBuilder
import org.json.JSONArray
import org.json.JSONException
import java.security.SecureRandom
import java.util.ArrayList
import java.util.HashMap

object NotificationUtils {
    private const val TAG = "${PushPlugin.PREFIX_TAG} (NotificationUtils)"

    private val messageMap = HashMap<Int, ArrayList<String?>>()

    private val FLAG_MUTABLE = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_MUTABLE
    } else {
        0
    }

    val FLAG_IMMUTABLE = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE
    } else {
        0
    }

    // VoIP
    const val CHANNEL_VOIP = "Voip"
    private const val CHANNEL_NAME = "TCVoip"
    const val VOIP_NOTIFICATION_ID = 168697

    /**
     * Set Notification
     * If message is empty or null, the message list is cleared.
     *
     * @param notId
     * @param message
     */
    fun setNotification(notId: Int, message: String?) {
        var messageList = messageMap[notId]

        if (messageList == null) {
            messageList = ArrayList()
            messageMap[notId] = messageList
        }

        if (message.isNullOrEmpty()) {
            messageList.clear()
        } else {
            messageList.add(message)
        }
    }

    fun defaultRingtoneUri(): Uri {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    }

    fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance: Int = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_VOIP, CHANNEL_NAME, importance)
            channel.description = "Channel For VOIP Calls"
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            // Set ringtone to notification (>= Android O)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .build()
            channel.setSound(defaultRingtoneUri(), audioAttributes)

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager: NotificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createNotificationBuilder(
        context: Context,
        extras: Bundle?,
        notificationManager: NotificationManager
    ): NotificationCompat.Builder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channelID: String? = null

            if (extras != null) {
                channelID = extras.getString(PushConstants.ANDROID_CHANNEL_ID)
            }

            // if the push payload specifies a channel use it
            return if (channelID != null) {
                NotificationCompat.Builder(context, channelID)
            } else {
                val channels = notificationManager.notificationChannels

                channelID = if (channels.size == 1) {
                    channels[0].id.toString()
                } else {
                    PushConstants.DEFAULT_CHANNEL_ID
                }

                Log.d(TAG, "Using channel ID = $channelID")
                NotificationCompat.Builder(context, channelID)
            }
        } else {
            return NotificationCompat.Builder(context)
        }
    }

    fun createActions(
        context: Context,
        extras: Bundle?,
        mBuilder: NotificationCompat.Builder,
        notId: Int,
    ) {
        Log.d(TAG, "create actions: with in-line")

        if (extras == null) {
            Log.d(TAG, "create actions: extras is null, skipping")
            return
        }

        val actions = extras.getString(PushConstants.ACTIONS)
        if (actions != null) {
            try {
                val actionsArray = JSONArray(actions)
                val wActions = ArrayList<NotificationCompat.Action>()

                for (i in 0 until actionsArray.length()) {
                    val min = 1
                    val max = 2000000000
                    val random = SecureRandom()
                    val uniquePendingIntentRequestCode = random.nextInt(max - min + 1) + min

                    Log.d(TAG, "adding action")

                    val action = actionsArray.getJSONObject(i)

                    Log.d(TAG, "adding callback = " + action.getString(PushConstants.CALLBACK))

                    val foreground = action.optBoolean(PushConstants.FOREGROUND, true)
                    val inline = action.optBoolean("inline", false)
                    var intent: Intent
                    var pIntent: PendingIntent?
                    val callback = action.getString(PushConstants.CALLBACK)

                    when {
                        inline -> {
                            Log.d(TAG, "Version: ${Build.VERSION.SDK_INT} = ${Build.VERSION_CODES.M}")

                            intent = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                                Log.d(TAG, "Push Activity")
                                Intent(context, PushHandlerActivity::class.java)
                            } else {
                                Log.d(TAG, "Push Receiver")
                                Intent(context, BackgroundActionButtonHandler::class.java)
                            }

                            PushUtils.updateIntent(intent, callback, extras, foreground, notId)

                            pIntent = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                                Log.d(TAG, "push activity for notId $notId")

                                PendingIntent.getActivity(
                                    context,
                                    uniquePendingIntentRequestCode,
                                    intent,
                                    PendingIntent.FLAG_ONE_SHOT or NotificationUtils.FLAG_MUTABLE
                                )

                            } else if (foreground) {
                                Log.d(TAG, "push receiver for notId $notId")
                                PendingIntent.getBroadcast(
                                    context,
                                    uniquePendingIntentRequestCode,
                                    intent,
                                    PendingIntent.FLAG_ONE_SHOT or NotificationUtils.FLAG_MUTABLE
                                )
                            } else {
                                // Only add on platform levels that support FLAG_MUTABLE
                                val flag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
                                if (context.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.S &&
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    intent = Intent(context, OnNotificationReceiverActivity::class.java)
                                    PushUtils.updateIntent(intent, action.getString(PushConstants.CALLBACK), extras, foreground, notId)

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        TaskStackBuilder.create(context).run {
                                            addNextIntentWithParentStack(intent)
                                            PendingIntent.getActivity(context, uniquePendingIntentRequestCode, intent, flag)
                                        }
                                    } else {
                                        PendingIntent.getActivity(context, uniquePendingIntentRequestCode, intent, flag)
                                    }

                                } else {
                                    intent = Intent(context, BackgroundActionButtonHandler::class.java)
                                    PushUtils.updateIntent(intent, action.getString(PushConstants.CALLBACK), extras, foreground, notId)
                                    PendingIntent.getBroadcast(context, uniquePendingIntentRequestCode, intent, flag)
                                }
                            }
                        }

                        foreground -> {
                            intent = Intent(context, PushHandlerActivity::class.java)
                            PushUtils.updateIntent(intent, callback, extras, foreground, notId)

                            pIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                TaskStackBuilder.create(context).run {
                                    addNextIntentWithParentStack(intent)
                                    PendingIntent.getActivity(
                                        context, uniquePendingIntentRequestCode,
                                        intent, PendingIntent.FLAG_UPDATE_CURRENT or NotificationUtils.FLAG_IMMUTABLE
                                    )
                                }
                            } else {
                                PendingIntent.getActivity(
                                    context, uniquePendingIntentRequestCode,
                                    intent, PendingIntent.FLAG_UPDATE_CURRENT or NotificationUtils.FLAG_IMMUTABLE
                                )
                            }
                        }
                        else -> {
                            intent = Intent(context, BackgroundActionButtonHandler::class.java)
                            PushUtils.updateIntent(intent, callback, extras, foreground, notId)
                            pIntent = PendingIntent.getBroadcast(
                                context, uniquePendingIntentRequestCode,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT or NotificationUtils.FLAG_IMMUTABLE
                            )
                        }
                    }
                    val actionBuilder = NotificationCompat.Action.Builder(
                        PushUtils.getImageId(context, action.optString(PushConstants.ICON, "")),
                        action.getString(PushConstants.TITLE),
                        pIntent
                    )

                    var remoteInput: RemoteInput?

                    if (inline) {
                        Log.d(TAG, "Create Remote Input")

                        val replyLabel = action.optString(
                            PushConstants.INLINE_REPLY_LABEL,
                            "Enter your reply here"
                        )

                        remoteInput = RemoteInput.Builder(PushConstants.INLINE_REPLY)
                            .setLabel(replyLabel)
                            .build()

                        actionBuilder.addRemoteInput(remoteInput)
                    }

                    val wAction: NotificationCompat.Action = actionBuilder.build()
                    wActions.add(actionBuilder.build())

                    if (inline) {
                        mBuilder.addAction(wAction)
                    } else {
                        mBuilder.addAction(
                            PushUtils.getImageId(context, action.optString(PushConstants.ICON, "")),
                            action.getString(PushConstants.TITLE),
                            pIntent
                        )
                    }
                }

                mBuilder.extend(NotificationCompat.WearableExtender().addActions(wActions))
                wActions.clear()
            } catch (e: JSONException) {
                // nope
            }
        }
    }

    fun setNotificationCount(extras: Bundle?, mBuilder: NotificationCompat.Builder) {
        val count = PushUtils.extractBadgeCount(extras)
        if (count >= 0) {
            Log.d(TAG, "count =[$count]")
            mBuilder.setNumber(count)
        }
    }

    fun setVisibility(extras: Bundle?, mBuilder: NotificationCompat.Builder) {
        extras?.getString(PushConstants.VISIBILITY)?.let { visibilityStr ->
            try {
                val visibilityInt = PushUtils.getNotificationVisibility(visibilityStr)
                if (
                    visibilityInt >= NotificationCompat.VISIBILITY_SECRET
                    && visibilityInt <= NotificationCompat.VISIBILITY_PUBLIC
                ) {
                    mBuilder.setVisibility(visibilityInt)
                } else {
                    Log.e(TAG, "Visibility parameter must be between -1 and 1")
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
        }
    }

    fun setNotificationVibration(
        extras: Bundle?,
        vibrateOption: Boolean,
        mBuilder: NotificationCompat.Builder,
    ) {
        if (extras == null) {
            Log.d(TAG, "setNotificationVibration: extras is null, skipping")
            return
        }

        val vibrationPattern = extras.getString(PushConstants.VIBRATION_PATTERN)
        if (vibrationPattern != null) {
            val items = vibrationPattern.convertToTypedArray()
            val results = LongArray(items.size)
            for (i in items.indices) {
                try {
                    results[i] = items[i].trim { it <= ' ' }.toLong()
                } catch (nfe: NumberFormatException) {
                    Log.e(TAG, "", nfe)
                }
            }
            mBuilder.setVibrate(results)
        } else {
            if (vibrateOption) {
                mBuilder.setDefaults(Notification.DEFAULT_VIBRATE)
            }
        }
    }

    fun setNotificationOngoing(extras: Bundle?, mBuilder: NotificationCompat.Builder) {
        extras?.getString(PushConstants.ONGOING, "false")?.let {
            mBuilder.setOngoing(it.toBoolean())
        }
    }

    fun setNotificationMessage(
        notId: Int,
        extras: Bundle?,
        mBuilder: NotificationCompat.Builder,
    ) {
        extras?.let {
            val message = it.getString(PushConstants.MESSAGE)

            when (it.getString(PushConstants.STYLE, PushConstants.STYLE_TEXT)) {
                PushConstants.STYLE_INBOX -> {
                    NotificationUtils.setNotification(notId, message)
                    mBuilder.setContentText(message?.fromHtml())

                    NotificationUtils.messageMap[notId]?.let { messageList ->
                        val sizeList = messageList.size

                        if (sizeList > 1) {
                            val sizeListMessage = sizeList.toString()
                            var stacking: String? = "$sizeList more"

                            it.getString(PushConstants.SUMMARY_TEXT)?.let { summaryText ->
                                stacking = summaryText.replace("%n%", sizeListMessage)
                            }

                            val notificationInbox = NotificationCompat.InboxStyle().run {
                                setBigContentTitle(it.getString(PushConstants.TITLE)?.fromHtml())
                                setSummaryText(stacking?.fromHtml())
                            }.also { inbox ->
                                for (i in messageList.indices.reversed()) {
                                    inbox.addLine(messageList[i]?.fromHtml())
                                }
                            }

                            mBuilder.setStyle(notificationInbox)
                        } else {
                            message?.let { message ->
                                val bigText = NotificationCompat.BigTextStyle().run {
                                    bigText(message.fromHtml())
                                    setBigContentTitle(it.getString(PushConstants.TITLE)?.fromHtml())
                                }

                                mBuilder.setStyle(bigText)
                            }
                        }
                    }
                }

                PushConstants.STYLE_PICTURE -> {
                    NotificationUtils.setNotification(notId, "")
                    val bigPicture = NotificationCompat.BigPictureStyle().run {
                        bigPicture(PushUtils.getBitmapFromURL(it.getString(PushConstants.PICTURE)))
                        setBigContentTitle(it.getString(PushConstants.TITLE)?.fromHtml())
                        setSummaryText(it.getString(PushConstants.SUMMARY_TEXT)?.fromHtml())
                    }

                    mBuilder.apply {
                        setContentTitle(it.getString(PushConstants.TITLE)?.fromHtml())
                        setContentText(message?.fromHtml())
                        setStyle(bigPicture)
                    }
                }

                else -> {
                    NotificationUtils.setNotification(notId, "")

                    message?.let { messageStr ->
                        val bigText = NotificationCompat.BigTextStyle().run {
                            bigText(messageStr.fromHtml())
                            setBigContentTitle(it.getString(PushConstants.TITLE)?.fromHtml())

                            it.getString(PushConstants.SUMMARY_TEXT)?.let { summaryText ->
                                setSummaryText(summaryText.fromHtml())
                            }
                        }

                        mBuilder.setContentText(messageStr.fromHtml())
                        mBuilder.setStyle(bigText)
                    }
                }
            }
        }
    }

    fun setNotificationSound(context: Context, extras: Bundle?, mBuilder: NotificationCompat.Builder) {
        extras?.let {
            val soundName = it.getString(PushConstants.SOUNDNAME) ?: it.getString(PushConstants.SOUND)

            when {
                soundName == PushConstants.SOUND_RINGTONE -> {
                    mBuilder.setSound(Settings.System.DEFAULT_RINGTONE_URI)
                }

                soundName != null && !soundName.contentEquals(PushConstants.SOUND_DEFAULT) -> {
                    val sound = Uri.parse(
                        "${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/raw/$soundName"
                    )

                    Log.d(TAG, "Sound URL: $sound")

                    mBuilder.setSound(sound)
                }

                else -> {
                    mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                }
            }
        }
    }

    fun setNotificationLedColor(extras: Bundle?, mBuilder: NotificationCompat.Builder) {
        extras?.let { it ->
            it.getString(PushConstants.LED_COLOR)?.let { ledColor ->
                // Convert ledColor to Int Typed Array
                val items = ledColor.convertToTypedArray()
                val results = IntArray(items.size)

                for (i in items.indices) {
                    try {
                        results[i] = items[i].trim { it <= ' ' }.toInt()
                    } catch (nfe: NumberFormatException) {
                        Log.e(TAG, "Number Format Exception: $nfe")
                    }
                }

                if (results.size == 4) {
                    val (alpha, red, green, blue) = results
                    mBuilder.setLights(Color.argb(alpha, red, green, blue), 500, 500)
                } else {
                    Log.e(TAG, "ledColor parameter must be an array of length == 4 (ARGB)")
                }
            }
        }
    }

    fun setNotificationPriority(extras: Bundle?, mBuilder: NotificationCompat.Builder) {
        extras?.let { it ->
            it.getString(PushConstants.PRIORITY)?.let { priorityStr ->
                try {
                    val priority = priorityStr.toInt()

                    if (
                        priority >= NotificationCompat.PRIORITY_MIN
                        && priority <= NotificationCompat.PRIORITY_MAX
                    ) {
                        mBuilder.priority = priority
                    } else {
                        Log.e(TAG, "Priority parameter must be between -2 and 2")
                    }
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
