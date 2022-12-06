package com.adobe.phonegap.push

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources.NotFoundException
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import me.leolin.shortcutbadger.ShortcutBadger
import org.apache.cordova.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutionException

/**
 * Cordova Plugin Push
 */
@Suppress("HardCodedStringLiteral")
@SuppressLint("LongLogTag", "LogConditional")
class PushPlugin : CordovaPlugin() {
  companion object {
    const val PREFIX_TAG: String = "cordova-plugin-push"
    private const val TAG: String = "$PREFIX_TAG (PushPlugin)"

    /**
     * Is the WebView in the foreground?
     */
    var isInForeground: Boolean = false

    private var pushContext: CallbackContext? = null
    private var gWebView: CordovaWebView? = null
    private val gCachedExtras = Collections.synchronizedList(ArrayList<Bundle>())

    /**
     *
     */
    fun sendEvent(json: JSONObject?) {
      val pluginResult = PluginResult(PluginResult.Status.OK, json)
        .apply { keepCallback = true }
      pushContext?.sendPluginResult(pluginResult)
    }

    /**
     * Sends the push bundle extras to the client application. If the client
     * application isn't currently active and the no-cache flag is not set, it is
     * cached for later processing.
     *
     * @param extras
     */
    @JvmStatic
    fun sendExtras(extras: Bundle?) {
      /**
       * Serializes a bundle to JSON.
       *
       * @param extras
       *
       * @return JSONObject|null
       */
      fun convertBundleToJson(extras: Bundle): JSONObject? {
        Log.d(TAG, "Convert Extras to JSON")

        try {
          val json = JSONObject()
          val additionalData = JSONObject()

          // Add any keys that need to be in top level json to this set
          val jsonKeySet: HashSet<String?> = HashSet<String?>()

          Collections.addAll(
            jsonKeySet,
            PushConstants.TITLE,
            PushConstants.MESSAGE,
            PushConstants.COUNT,
            PushConstants.SOUND,
            PushConstants.IMAGE
          )

          val it: Iterator<String> = extras.keySet().iterator()

          while (it.hasNext()) {
            val key = it.next()
            val value = extras[key]

            Log.d(TAG, "Extras Iteration: key=$key")

            when {
              jsonKeySet.contains(key) -> {
                json.put(key, value)
              }

              key == PushConstants.COLDSTART -> {
                additionalData.put(key, extras.getBoolean(PushConstants.COLDSTART))
              }

              key == PushConstants.FOREGROUND -> {
                additionalData.put(key, extras.getBoolean(PushConstants.FOREGROUND))
              }

              key == PushConstants.DISMISSED -> {
                additionalData.put(key, extras.getBoolean(PushConstants.DISMISSED))
              }

              value is String -> {
                try {
                  // Try to figure out if the value is another JSON object
                  when {
                    value.startsWith("{") -> {
                      additionalData.put(key, JSONObject(value))
                    }

                    value.startsWith("[") -> {
                      additionalData.put(key, JSONArray(value))
                    }

                    else -> {
                      additionalData.put(key, value)
                    }
                  }
                } catch (e: Exception) {
                  additionalData.put(key, value)
                }
              }
            }
          }

          json.put(PushConstants.ADDITIONAL_DATA, additionalData)

          Log.v(TAG, "Extras To JSON Result: $json")
          return json
        } catch (e: JSONException) {
          Log.e(TAG, "convertBundleToJson had a JSON Exception")
        }

        return null
      }

      extras?.let {
        val noCache = it.getString(PushConstants.NO_CACHE)

        if (gWebView != null) {
          sendEvent(convertBundleToJson(extras))
        } else if (noCache != "1") {
          Log.v(TAG, "sendExtras: Caching extras to send at a later time.")
          gCachedExtras.add(extras)
        }
      }
    }

    /**
     * Retrieves the badge count from SharedPreferences
     *
     * @param context
     *
     * @return Int
     */
    fun getApplicationIconBadgeNumber(context: Context): Int {
      val settings = context.getSharedPreferences(PushConstants.BADGE, Context.MODE_PRIVATE)
      return settings.getInt(PushConstants.BADGE, 0)
    }

    /**
     * Sets badge count on application icon and in SharedPreferences
     *
     * @param context
     * @param badgeCount
     */
    @JvmStatic
    fun setApplicationIconBadgeNumber(context: Context, badgeCount: Int) {
      if (badgeCount > 0) {
        ShortcutBadger.applyCount(context, badgeCount)
      } else {
        ShortcutBadger.removeCount(context)
      }

      context.getSharedPreferences(PushConstants.BADGE, Context.MODE_PRIVATE)
        .edit()?.apply {
          putInt(PushConstants.BADGE, badgeCount.coerceAtLeast(0))
          apply()
        }
    }

    /**
     * @return Boolean Active is true when the Cordova WebView is present.
     */
    val isActive: Boolean
      get() = gWebView != null
  }

  private val activity: Activity
    get() = cordova.activity

  private val applicationContext: Context
    get() = activity.applicationContext

  private val notificationManager: NotificationManager
    get() = (activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)

  private val appName: String
    get() = activity.packageManager.getApplicationLabel(activity.applicationInfo) as String

  @TargetApi(26)
  @Throws(JSONException::class)
  private fun listChannels(): JSONArray {
    val channels = JSONArray()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationChannels = notificationManager.notificationChannels

      for (notificationChannel in notificationChannels) {
        val channel = JSONObject().apply {
          put(PushConstants.CHANNEL_ID, notificationChannel.id)
          put(PushConstants.CHANNEL_DESCRIPTION, notificationChannel.description)
        }

        channels.put(channel)
      }
    }

    return channels
  }

  @TargetApi(26)
  private fun deleteChannel(channelId: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      notificationManager.deleteNotificationChannel(channelId)
    }
  }

  @TargetApi(26)
  @Throws(JSONException::class)
  private fun createChannel(channel: JSONObject?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      channel?.let {
        NotificationChannel(
          it.getString(PushConstants.CHANNEL_ID),
          it.optString(PushConstants.CHANNEL_DESCRIPTION, appName),
          it.optInt(PushConstants.CHANNEL_IMPORTANCE, NotificationManager.IMPORTANCE_DEFAULT)
        ).apply {
          /**
           * Enable Lights when Light Color is set.
           */
          val mLightColor = it.optInt(PushConstants.CHANNEL_LIGHT_COLOR, -1)
          if (mLightColor != -1) {
            enableLights(true)
            lightColor = mLightColor
          }

          /**
           * Set Lock Screen Visibility.
           */
          lockscreenVisibility = channel.optInt(
            PushConstants.VISIBILITY,
            NotificationCompat.VISIBILITY_PUBLIC
          )

          /**
           * Set if badge should be shown
           */
          setShowBadge(it.optBoolean(PushConstants.BADGE, true))

          /**
           * Sound Settings
           */
          val (soundUri, audioAttributes) = getNotificationChannelSound(it)
          setSound(soundUri, audioAttributes)

          /**
           * Set vibration settings.
           * Data can be either JSONArray or Boolean value.
           */
          val (hasVibration, vibrationPatternArray) = getNotificationChannelVibration(it)
          if (vibrationPatternArray != null) {
            vibrationPattern = vibrationPatternArray
          } else {
            enableVibration(hasVibration)
          }

          notificationManager.createNotificationChannel(this)
        }
      }
    }
  }

  private fun getNotificationChannelSound(channelData: JSONObject): Pair<Uri?, AudioAttributes?> {
    val audioAttributes = AudioAttributes.Builder()
      .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
      .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
      .build()

    val sound = channelData.optString(PushConstants.SOUND, PushConstants.SOUND_DEFAULT)

    return when {
      sound == PushConstants.SOUND_RINGTONE -> Pair(
        Settings.System.DEFAULT_RINGTONE_URI,
        audioAttributes
      )

      // Disable sound for this notification channel if an empty string is passed.
      // https://stackoverflow.com/a/47144981/6194193
      sound.isEmpty() -> Pair(null, null)

      // E.g. android.resource://org.apache.cordova/raw/<SOUND>
      sound != PushConstants.SOUND_DEFAULT -> {
        val scheme = ContentResolver.SCHEME_ANDROID_RESOURCE
        val packageName = applicationContext.packageName

        Pair(
          Uri.parse("${scheme}://$packageName/raw/$sound"),
          audioAttributes
        )
      }

      else -> Pair(Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes)
    }
  }

  private fun getNotificationChannelVibration(channelData: JSONObject): Pair<Boolean, LongArray?> {
    var patternArray: LongArray? = null
    val mVibrationPattern = channelData.optJSONArray(PushConstants.CHANNEL_VIBRATION)

    if (mVibrationPattern != null) {
      val patternLength = mVibrationPattern.length()
      patternArray = LongArray(patternLength)

      for (i in 0 until patternLength) {
        patternArray[i] = mVibrationPattern.optLong(i)
      }
    }

    return Pair(
      channelData.optBoolean(PushConstants.CHANNEL_VIBRATION, true),
      patternArray
    )
  }

  @TargetApi(26)
  private fun createDefaultNotificationChannelIfNeeded(options: JSONObject?) {
    // only call on Android O and above
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channels = notificationManager.notificationChannels

      for (i in channels.indices) {
        if (PushConstants.DEFAULT_CHANNEL_ID == channels[i].id) {
          return
        }
      }

      try {
        options?.apply {
          put(PushConstants.CHANNEL_ID, PushConstants.DEFAULT_CHANNEL_ID)
          putOpt(PushConstants.CHANNEL_DESCRIPTION, appName)
        }

        createChannel(options)
      } catch (e: JSONException) {
        Log.e(TAG, "Execute: JSON Exception ${e.message}")
      }
    }
  }

  /**
   * Performs various push plugin related tasks:
   *
   *  - Initialize
   *  - Unregister
   *  - Has Notification Permission Check
   *  - Set Icon Badge Number
   *  - Get Icon Badge Number
   *  - Clear All Notifications
   *  - Clear Notification
   *  - Subscribe
   *  - Unsubscribe
   *  - Create Channel
   *  - Delete Channel
   *  - List Channels
   *
   *  @param action
   *  @param data
   *  @param callbackContext
   */
  override fun execute(
    action: String,
    data: JSONArray,
    callbackContext: CallbackContext
  ): Boolean {
    Log.v(TAG, "Execute: Action = $action")

    gWebView = webView

    when (action) {
      PushConstants.INITIALIZE -> executeActionInitialize(data, callbackContext)
      PushConstants.UNREGISTER -> executeActionUnregister(data, callbackContext)
      PushConstants.FINISH -> callbackContext.success()
      PushConstants.HAS_PERMISSION -> executeActionHasPermission(callbackContext)
      PushConstants.SET_APPLICATION_ICON_BADGE_NUMBER -> executeActionSetIconBadgeNumber(
        data, callbackContext
      )
      PushConstants.GET_APPLICATION_ICON_BADGE_NUMBER -> executeActionGetIconBadgeNumber(
        callbackContext
      )
      PushConstants.CLEAR_ALL_NOTIFICATIONS -> executeActionClearAllNotifications(callbackContext)
      PushConstants.SUBSCRIBE -> executeActionSubscribe(data, callbackContext)
      PushConstants.UNSUBSCRIBE -> executeActionUnsubscribe(data, callbackContext)
      PushConstants.CREATE_CHANNEL -> executeActionCreateChannel(data, callbackContext)
      PushConstants.DELETE_CHANNEL -> executeActionDeleteChannel(data, callbackContext)
      PushConstants.LIST_CHANNELS -> executeActionListChannels(callbackContext)
      PushConstants.CLEAR_NOTIFICATION -> executeActionClearNotification(data, callbackContext)
      else -> {
        Log.e(TAG, "Execute: Invalid Action $action")
        callbackContext.sendPluginResult(PluginResult(PluginResult.Status.INVALID_ACTION))
        return false
      }
    }
    return true
  }

  private fun executeActionInitialize(data: JSONArray, callbackContext: CallbackContext) {
    // Better Logging
    fun formatLogMessage(msg: String): String = "Execute::Initialize: ($msg)"

    cordova.threadPool.execute(Runnable {
      Log.v(TAG, formatLogMessage("Data=$data"))

      pushContext = callbackContext

      val sharedPref = applicationContext.getSharedPreferences(
        PushConstants.COM_ADOBE_PHONEGAP_PUSH,
        Context.MODE_PRIVATE
      )
      var jo: JSONObject? = null
      var senderID: String? = null

      try {
        jo = data.getJSONObject(0).getJSONObject(PushConstants.ANDROID)

        val senderIdResId = activity.resources.getIdentifier(
          PushConstants.GCM_DEFAULT_SENDER_ID,
          "string",
          activity.packageName
        )
        senderID = activity.getString(senderIdResId)

        // If no NotificationChannels exist create the default one
        createDefaultNotificationChannelIfNeeded(jo)

        Log.v(TAG, formatLogMessage("JSONObject=$jo"))
        Log.v(TAG, formatLogMessage("senderID=$senderID"))

        val token = try {
          try {
            Tasks.await(FirebaseMessaging.getInstance().token)
          } catch (e: ExecutionException) {
            throw e.cause ?: e
          }
        } catch (e: IllegalStateException) {
          Log.e(TAG, formatLogMessage("Firebase Token Exception ${e.message}"))
          null
        } catch (e: ExecutionException) {
          Log.e(TAG, formatLogMessage("Firebase Token Exception ${e.message}"))
          null
        } catch (e: InterruptedException) {
          Log.e(TAG, formatLogMessage("Firebase Token Exception ${e.message}"))
          null
        }

        if (token != "") {
          val registration = JSONObject().put(PushConstants.REGISTRATION_ID, token).apply {
            put(PushConstants.REGISTRATION_TYPE, PushConstants.FCM)
          }

          Log.v(TAG, formatLogMessage("onRegistered=$registration"))

          val topics = jo.optJSONArray(PushConstants.TOPICS)
          subscribeToTopics(topics)

          sendEvent(registration)
        } else {
          callbackContext.error("Empty registration ID received from FCM")
          return@Runnable
        }
      } catch (e: JSONException) {
        Log.e(TAG, formatLogMessage("JSON Exception ${e.message}"))
        callbackContext.error(e.message)
      } catch (e: IOException) {
        Log.e(TAG, formatLogMessage("IO Exception ${e.message}"))
        callbackContext.error(e.message)
      } catch (e: NotFoundException) {
        Log.e(TAG, formatLogMessage("Resources NotFoundException Exception ${e.message}"))
        callbackContext.error(e.message)
      }

      jo?.let {
        /**
         * Add Shared Preferences
         *
         * Make sure to remove the preferences in the Remove step.
         */
        sharedPref.edit()?.apply {
          /**
           * Set Icon
           */
          try {
            putString(PushConstants.ICON, it.getString(PushConstants.ICON))
          } catch (e: JSONException) {
            Log.d(TAG, formatLogMessage("No Icon Options"))
          }

          /**
           * Set Icon Color
           */
          try {
            putString(PushConstants.ICON_COLOR, it.getString(PushConstants.ICON_COLOR))
          } catch (e: JSONException) {
            Log.d(TAG, formatLogMessage("No Icon Color Options"))
          }

          /**
           * Clear badge count when true
           */
          val clearBadge = it.optBoolean(PushConstants.CLEAR_BADGE, false)
          putBoolean(PushConstants.CLEAR_BADGE, clearBadge)

          if (clearBadge) {
            setApplicationIconBadgeNumber(applicationContext, 0)
          }

          /**
           * Set Sound
           */
          putBoolean(PushConstants.SOUND, it.optBoolean(PushConstants.SOUND, true))

          /**
           * Set Vibrate
           */
          putBoolean(PushConstants.VIBRATE, it.optBoolean(PushConstants.VIBRATE, true))

          /**
           * Set Clear Notifications
           */
          putBoolean(
            PushConstants.CLEAR_NOTIFICATIONS,
            it.optBoolean(PushConstants.CLEAR_NOTIFICATIONS, true)
          )

          /**
           * Set Force Show
           */
          putBoolean(
            PushConstants.FORCE_SHOW,
            it.optBoolean(PushConstants.FORCE_SHOW, false)
          )

          /**
           * Set SenderID
           */
          putString(PushConstants.SENDER_ID, senderID)

          /**
           * Set Message Key
           */
          putString(PushConstants.MESSAGE_KEY, it.optString(PushConstants.MESSAGE_KEY))

          /**
           * Set Title Key
           */
          putString(PushConstants.TITLE_KEY, it.optString(PushConstants.TITLE_KEY))

          commit()
        }
      }

      if (gCachedExtras.isNotEmpty()) {
        Log.v(TAG, formatLogMessage("Sending Cached Extras"))

        synchronized(gCachedExtras) {
          val gCachedExtrasIterator: Iterator<Bundle> = gCachedExtras.iterator()

          while (gCachedExtrasIterator.hasNext()) {
            sendExtras(gCachedExtrasIterator.next())
          }
        }

        gCachedExtras.clear()
      }
    })
  }

  private fun executeActionUnregister(data: JSONArray, callbackContext: CallbackContext) {
    // Better Logging
    fun formatLogMessage(msg: String): String = "Execute::Unregister: ($msg)"

    cordova.threadPool.execute {
      try {
        val sharedPref = applicationContext.getSharedPreferences(
          PushConstants.COM_ADOBE_PHONEGAP_PUSH,
          Context.MODE_PRIVATE
        )
        val topics = data.optJSONArray(0)

        if (topics != null) {
          unsubscribeFromTopics(topics)
        } else {
          try {
            Tasks.await(FirebaseMessaging.getInstance().deleteToken())
          } catch (e: ExecutionException) {
            throw e.cause ?: e
          }
          Log.v(TAG, formatLogMessage("UNREGISTER"))

          /**
           * Remove Shared Preferences
           *
           * Make sure to remove what was in the Initialize step.
           */
          sharedPref.edit()?.apply {
            remove(PushConstants.ICON)
            remove(PushConstants.ICON_COLOR)
            remove(PushConstants.CLEAR_BADGE)
            remove(PushConstants.SOUND)
            remove(PushConstants.VIBRATE)
            remove(PushConstants.CLEAR_NOTIFICATIONS)
            remove(PushConstants.FORCE_SHOW)
            remove(PushConstants.SENDER_ID)
            remove(PushConstants.MESSAGE_KEY)
            remove(PushConstants.TITLE_KEY)

            commit()
          }
        }

        callbackContext.success()
      } catch (e: IOException) {
        Log.e(TAG, formatLogMessage("IO Exception ${e.message}"))
        callbackContext.error(e.message)
      } catch (e: InterruptedException) {
        Log.e(TAG, formatLogMessage("Interrupted ${e.message}"))
        callbackContext.error(e.message)
      }
    }
  }

  private fun executeActionHasPermission(callbackContext: CallbackContext) {
    // Better Logging
    fun formatLogMessage(msg: String): String = "Execute::HasPermission: ($msg)"

    cordova.threadPool.execute {
      try {
        val isNotificationEnabled = NotificationManagerCompat.from(applicationContext)
          .areNotificationsEnabled()

        Log.d(TAG, formatLogMessage("Has Notification Permission: $isNotificationEnabled"))

        val jo = JSONObject().apply {
          put(PushConstants.IS_ENABLED, isNotificationEnabled)
        }

        val pluginResult = PluginResult(PluginResult.Status.OK, jo).apply {
          keepCallback = true
        }

        callbackContext.sendPluginResult(pluginResult)
      } catch (e: UnknownError) {
        callbackContext.error(e.message)
      } catch (e: JSONException) {
        callbackContext.error(e.message)
      }
    }
  }

  private fun executeActionSetIconBadgeNumber(data: JSONArray, callbackContext: CallbackContext) {
    fun formatLogMessage(msg: String): String = "Execute::SetIconBadgeNumber: ($msg)"

    cordova.threadPool.execute {
      Log.v(TAG, formatLogMessage("data=$data"))

      try {
        val badgeCount = data.getJSONObject(0).getInt(PushConstants.BADGE)
        setApplicationIconBadgeNumber(applicationContext, badgeCount)
      } catch (e: JSONException) {
        callbackContext.error(e.message)
      }

      callbackContext.success()
    }
  }

  private fun executeActionGetIconBadgeNumber(callbackContext: CallbackContext) {
    cordova.threadPool.execute {
      Log.v(TAG, "Execute::GetIconBadgeNumber")
      callbackContext.success(getApplicationIconBadgeNumber(applicationContext))
    }
  }

  private fun executeActionClearAllNotifications(callbackContext: CallbackContext) {
    cordova.threadPool.execute {
      Log.v(TAG, "Execute Clear All Notifications")
      clearAllNotifications()
      callbackContext.success()
    }
  }

  private fun executeActionSubscribe(data: JSONArray, callbackContext: CallbackContext) {
    cordova.threadPool.execute {
      try {
        Log.v(TAG, "Execute::Subscribe")
        val topic = data.getString(0)
        subscribeToTopic(topic)
        callbackContext.success()
      } catch (e: JSONException) {
        callbackContext.error(e.message)
      }
    }
  }

  private fun executeActionUnsubscribe(data: JSONArray, callbackContext: CallbackContext) {
    cordova.threadPool.execute {
      try {
        Log.v(TAG, "Execute::Unsubscribe")
        val topic = data.getString(0)
        unsubscribeFromTopic(topic)
        callbackContext.success()
      } catch (e: JSONException) {
        callbackContext.error(e.message)
      }
    }
  }

  private fun executeActionCreateChannel(data: JSONArray, callbackContext: CallbackContext) {
    cordova.threadPool.execute {
      try {
        Log.v(TAG, "Execute::CreateChannel")
        createChannel(data.getJSONObject(0))
        callbackContext.success()
      } catch (e: JSONException) {
        callbackContext.error(e.message)
      }
    }
  }

  private fun executeActionDeleteChannel(data: JSONArray, callbackContext: CallbackContext) {
    cordova.threadPool.execute {
      try {
        val channelId = data.getString(0)
        Log.v(TAG, "Execute::DeleteChannel channelId=$channelId")
        deleteChannel(channelId)
        callbackContext.success()
      } catch (e: JSONException) {
        callbackContext.error(e.message)
      }
    }
  }

  private fun executeActionListChannels(callbackContext: CallbackContext) {
    cordova.threadPool.execute {
      try {
        Log.v(TAG, "Execute::ListChannels")
        callbackContext.success(listChannels())
      } catch (e: JSONException) {
        callbackContext.error(e.message)
      }
    }
  }

  private fun executeActionClearNotification(data: JSONArray, callbackContext: CallbackContext) {
    cordova.threadPool.execute {
      try {
        val notificationId = data.getInt(0)
        Log.v(TAG, "Execute::ClearNotification notificationId=$notificationId")
        clearNotification(notificationId)
        callbackContext.success()
      } catch (e: JSONException) {
        callbackContext.error(e.message)
      }
    }
  }

  /**
   * Initialize
   */
  override fun initialize(cordova: CordovaInterface, webView: CordovaWebView) {
    super.initialize(cordova, webView)
    isInForeground = true
  }

  /**
   * Handle when the view is being paused
   */
  override fun onPause(multitasking: Boolean) {
    isInForeground = false
    super.onPause(multitasking)
  }

  /**
   * Handle when the view is resuming
   */
  override fun onResume(multitasking: Boolean) {
    super.onResume(multitasking)
    isInForeground = true
  }

  /**
   * Handle when the view is being destroyed
   */
  override fun onDestroy() {
    isInForeground = false
    gWebView = null

    // Clear Notification
    applicationContext.getSharedPreferences(
      PushConstants.COM_ADOBE_PHONEGAP_PUSH,
      Context.MODE_PRIVATE
    ).apply {
      if (getBoolean(PushConstants.CLEAR_NOTIFICATIONS, true)) {
        clearAllNotifications()
      }
    }

    super.onDestroy()
  }

  private fun clearAllNotifications() {
    notificationManager.cancelAll()
  }

  private fun clearNotification(id: Int) {
    notificationManager.cancel(appName, id)
  }

  private fun subscribeToTopics(topics: JSONArray?) {
    topics?.let {
      for (i in 0 until it.length()) {
        val topicKey = it.optString(i, null)
        subscribeToTopic(topicKey)
      }
    }
  }

  private fun unsubscribeFromTopics(topics: JSONArray?) {
    topics?.let {
      for (i in 0 until it.length()) {
        val topic = it.optString(i, null)
        unsubscribeFromTopic(topic)
      }
    }
  }

  private fun subscribeToTopic(topic: String?) {
    topic?.let {
      Log.d(TAG, "Subscribing to Topic: $it")
      FirebaseMessaging.getInstance().subscribeToTopic(it)
    }
  }

  private fun unsubscribeFromTopic(topic: String?) {
    topic?.let {
      Log.d(TAG, "Unsubscribing to topic: $it")
      FirebaseMessaging.getInstance().unsubscribeFromTopic(it)
    }
  }
}
