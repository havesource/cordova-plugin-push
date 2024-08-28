package com.adobe.phonegap.push

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.ArrayList

object PushUtils {
    private const val TAG = "${PushPlugin.PREFIX_TAG} (PushUtils)"

    const val VISIBILITY_PUBLIC_STR = "PUBLIC"
    const val VISIBILITY_PRIVATE_STR = "PRIVATE"
    const val VISIBILITY_SECRET_STR = "SECRET"

  private fun replaceKey(context: Context, oldKey: String, newKey: String, extras: Bundle, newExtras: Bundle) {
    /*
     * Change a values key in the extras bundle
     */
    var value = extras[oldKey]
    if (value != null) {
      when (value) {
        is String -> {
          value = localizeKey(context, newKey, value)
          newExtras.putString(newKey, value as String?)
        }

        is Boolean -> newExtras.putBoolean(newKey, (value as Boolean?) ?: return)

        is Number -> {
          newExtras.putDouble(newKey, value.toDouble())
        }

        else -> {
          newExtras.putString(newKey, value.toString())
        }
      }
    }
  }

  private fun localizeKey(context: Context, key: String, value: String): String {
    /*
     * Normalize localization for key
     */
    return when (key) {
      PushConstants.TITLE,
      PushConstants.MESSAGE,
      PushConstants.SUMMARY_TEXT,
      -> {
        try {
          val localeObject = JSONObject(value)
          val localeKey = localeObject.getString(PushConstants.LOC_KEY)
          val localeFormatData = ArrayList<String>()

          if (!localeObject.isNull(PushConstants.LOC_DATA)) {
            val localeData = localeObject.getString(PushConstants.LOC_DATA)
            val localeDataArray = JSONArray(localeData)

            for (i in 0 until localeDataArray.length()) {
              localeFormatData.add(localeDataArray.getString(i))
            }
          }

          val resourceId = context.resources.getIdentifier(
            localeKey,
            "string",
            context.packageName
          )

          if (resourceId != 0) {
            context.resources.getString(resourceId, *localeFormatData.toTypedArray())
          } else {
            Log.d(TAG, "Can't Find Locale Resource (key=$localeKey)")
            value
          }
        } catch (e: JSONException) {
          Log.d(TAG, "No Locale Found (key= $key, error=${e.message})")
          value
        }
      }
      else -> value
    }
  }

  private fun normalizeKey(
    key: String,
    messageKey: String?,
    titleKey: String?,
    newExtras: Bundle,
  ): String {
    /*
     * Replace alternate keys with our canonical value
     */
    return when {
      key == PushConstants.BODY
        || key == PushConstants.ALERT
        || key == PushConstants.MP_MESSAGE
        || key == PushConstants.GCM_NOTIFICATION_BODY
        || key == PushConstants.TWILIO_BODY
        || key == messageKey
        || key == PushConstants.AWS_PINPOINT_BODY
      -> {
        PushConstants.MESSAGE
      }

      key == PushConstants.TWILIO_TITLE || key == PushConstants.SUBJECT || key == titleKey -> {
        PushConstants.TITLE
      }

      key == PushConstants.MSGCNT || key == PushConstants.BADGE -> {
        PushConstants.COUNT
      }

      key == PushConstants.SOUNDNAME || key == PushConstants.TWILIO_SOUND -> {
        PushConstants.SOUND
      }

      key == PushConstants.AWS_PINPOINT_PICTURE -> {
        newExtras.putString(PushConstants.STYLE, PushConstants.STYLE_PICTURE)
        PushConstants.PICTURE
      }

      key.startsWith(PushConstants.GCM_NOTIFICATION) -> {
        key.substring(PushConstants.GCM_NOTIFICATION.length + 1, key.length)
      }

      key.startsWith(PushConstants.GCM_N) -> {
        key.substring(PushConstants.GCM_N.length + 1, key.length)
      }

      key.startsWith(PushConstants.UA_PREFIX) -> {
        key.substring(PushConstants.UA_PREFIX.length + 1, key.length).lowercase()
      }

      key.startsWith(PushConstants.AWS_PINPOINT_PREFIX) -> {
        key.substring(PushConstants.AWS_PINPOINT_PREFIX.length + 1, key.length)
      }

      else -> key
    }
  }

  fun normalizeExtras(
    context: Context,
    extras: Bundle,
    messageKey: String?,
    titleKey: String?,
  ): Bundle {
    /*
     * Parse bundle into normalized keys.
     */
    Log.d(TAG, "normalize extras")

    val it: Iterator<String> = extras.keySet().iterator()
    val newExtras = Bundle()

    while (it.hasNext()) {
      val key = it.next()
      Log.d(TAG, "key = $key")

      // If normalizeKey, the key is "data" or "message" and the value is a json object extract
      // This is to support parse.com and other services. Issue #147 and pull #218
      if (
        key == PushConstants.PARSE_COM_DATA ||
        key == PushConstants.MESSAGE ||
        key == messageKey
      ) {
        val json = extras[key]

        // Make sure data is in json object string format
        if (json is String && json.startsWith("{")) {
          Log.d(TAG, "extracting nested message data from key = $key")

          try {
            // If object contains message keys promote each value to the root of the bundle
            val data = JSONObject(json)
            if (
              data.has(PushConstants.ALERT)
              || data.has(PushConstants.MESSAGE)
              || data.has(PushConstants.BODY)
              || data.has(PushConstants.TITLE)
              || data.has(messageKey)
              || data.has(titleKey)
            ) {
              val jsonKeys = data.keys()

              while (jsonKeys.hasNext()) {
                var jsonKey = jsonKeys.next()
                Log.d(TAG, "key = data/$jsonKey")

                var value = data.getString(jsonKey)
                jsonKey = normalizeKey(jsonKey, messageKey, titleKey, newExtras)
                value = localizeKey(context, jsonKey, value)
                newExtras.putString(jsonKey, value)
              }
            } else if (data.has(PushConstants.LOC_KEY) || data.has(PushConstants.LOC_DATA)) {
              val newKey = normalizeKey(key, messageKey, titleKey, newExtras)
              Log.d(TAG, "replace key $key with $newKey")
              replaceKey(context, key, newKey, extras, newExtras)
            }
          } catch (e: JSONException) {
            Log.e(TAG, "normalizeExtras: JSON exception")
          }
        } else {
          val newKey = normalizeKey(key, messageKey, titleKey, newExtras)
          Log.d(TAG, "replace key $key with $newKey")
          replaceKey(context, key, newKey, extras, newExtras)
        }
      } else if (key == "notification") {
        val value = extras.getBundle(key)
        val iterator: Iterator<String> = value!!.keySet().iterator()

        while (iterator.hasNext()) {
          val notificationKey = iterator.next()
          Log.d(TAG, "notificationKey = $notificationKey")

          val newKey = normalizeKey(notificationKey, messageKey, titleKey, newExtras)
          Log.d(TAG, "Replace key $notificationKey with $newKey")

          var valueData = value.getString(notificationKey)
          valueData = localizeKey(context, newKey, valueData!!)
          newExtras.putString(newKey, valueData)
        }
        continue
        // In case we weren't working on the payload data node or the notification node,
        // normalize the key.
        // This allows to have "message" as the payload data key without colliding
        // with the other "message" key (holding the body of the payload)
        // See issue #1663
      } else {
        val newKey = normalizeKey(key, messageKey, titleKey, newExtras)
        Log.d(TAG, "replace key $key with $newKey")
        replaceKey(context, key, newKey, extras, newExtras)
      }
    } // while
    return newExtras
  }

  fun extractBadgeCount(extras: Bundle?): Int {
    var count = -1

    try {
      extras?.getString(PushConstants.COUNT)?.let {
        count = it.toInt()
      }
    } catch (e: NumberFormatException) {
      Log.e(TAG, e.localizedMessage, e)
    }

    return count
  }

    fun updateIntent(
        intent: Intent,
        callback: String,
        extras: Bundle?,
        foreground: Boolean,
        notId: Int,
    ) {
        intent.apply {
            putExtra(PushConstants.CALLBACK, callback)
            putExtra(PushConstants.PUSH_BUNDLE, extras)
            putExtra(PushConstants.FOREGROUND, foreground)
            putExtra(PushConstants.NOT_ID, notId)
        }
    }

    private fun getCircleBitmap(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null) {
            return null
        }

        val output = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height,
            Bitmap.Config.ARGB_8888
        )

        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.RED
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        }

        Canvas(output).apply {
            drawARGB(0, 0, 0, 0)

            val cx = (bitmap.width / 2).toFloat()
            val cy = (bitmap.height / 2).toFloat()
            val radius = if (cx < cy) cx else cy
            val rect = Rect(0, 0, bitmap.width, bitmap.height)

            drawCircle(cx, cy, radius, paint)
            drawBitmap(bitmap, rect, rect, paint)
        }

        bitmap.recycle()
        return output
    }

    fun setNotificationLargeIcon(context: Context,
                                 extras: Bundle?,
                                 mBuilder: NotificationCompat.Builder,
    ) {
        extras?.let {
            val gcmLargeIcon = it.getString(PushConstants.IMAGE)
            val imageType = it.getString(PushConstants.IMAGE_TYPE, PushConstants.IMAGE_TYPE_SQUARE)

            if (gcmLargeIcon != null && gcmLargeIcon != "") {
                if (
                    gcmLargeIcon.startsWith("http://")
                    || gcmLargeIcon.startsWith("https://")
                ) {
                    val bitmap = getBitmapFromURL(gcmLargeIcon)

                    if (PushConstants.IMAGE_TYPE_SQUARE.equals(imageType, ignoreCase = true)) {
                        mBuilder.setLargeIcon(bitmap)
                    } else {
                        val bm = getCircleBitmap(bitmap)
                        mBuilder.setLargeIcon(bm)
                    }

                    Log.d(TAG, "Using remote large-icon from GCM")
                } else {
                    try {
                        val inputStream: InputStream = context.assets.open(gcmLargeIcon)

                        val bitmap = BitmapFactory.decodeStream(inputStream)

                        if (PushConstants.IMAGE_TYPE_SQUARE.equals(imageType, ignoreCase = true)) {
                            mBuilder.setLargeIcon(bitmap)
                        } else {
                            val bm = getCircleBitmap(bitmap)
                            mBuilder.setLargeIcon(bm)
                        }

                        Log.d(TAG, "Using assets large-icon from GCM")
                    } catch (e: IOException) {
                        val largeIconId: Int = getImageId(context, gcmLargeIcon)

                        if (largeIconId != 0) {
                            val largeIconBitmap = BitmapFactory.decodeResource(context.resources, largeIconId)
                            mBuilder.setLargeIcon(largeIconBitmap)
                            Log.d(TAG, "Using resources large-icon from GCM")
                        } else {
                            Log.d(TAG, "Not large icon settings")
                        }
                    }
                }
            }
        }
    }

    fun setNotificationCount(extras: Bundle?, mBuilder: NotificationCompat.Builder) {
        val count = extractBadgeCount(extras)
        if (count >= 0) {
            Log.d(TAG, "count =[$count]")
            mBuilder.setNumber(count)
        }
    }

    fun getImageId(context: Context, icon: String): Int {
        var iconId = context.resources.getIdentifier(icon, PushConstants.DRAWABLE, context.packageName)
        if (iconId == 0) {
            iconId = context.resources.getIdentifier(icon, "mipmap", context.packageName)
        }
        return iconId
    }

    fun setNotificationSmallIcon(
        context: Context,
        extras: Bundle?,
        mBuilder: NotificationCompat.Builder,
        localIcon: String?,
    ) {
        extras?.let {
            val icon = it.getString(PushConstants.ICON)

            val iconId = when {
                !icon.isNullOrEmpty() -> {
                    getImageId(context, icon)
                }

                !localIcon.isNullOrEmpty() -> {
                    getImageId(context, localIcon)
                }

                else -> {
                    Log.d(TAG, "No icon resource found from settings, using application icon")
                    context.applicationInfo.icon
                }
            }

            mBuilder.setSmallIcon(iconId)
        }
    }

    fun setNotificationIconColor(
        color: String?,
        mBuilder: NotificationCompat.Builder,
        localIconColor: String?,
    ) {
        val iconColor = when {
            color != null && color != "" -> {
                try {
                    Color.parseColor(color)
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "Couldn't parse color from Android options")
                }
            }

            localIconColor != null && localIconColor != "" -> {
                try {
                    Color.parseColor(localIconColor)
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "Couldn't parse color from android options")
                }
            }

            else -> {
                Log.d(TAG, "No icon color settings found")
                0
            }
        }

        if (iconColor != 0) {
            mBuilder.color = iconColor
        }
    }

    fun getBitmapFromURL(strURL: String?): Bitmap? {
        return try {
            val url = URL(strURL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                doInput = true
                connect()
            }
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun parseNotificationIdToInt(extras: Bundle?): Int {
        var returnVal = 0

        try {
            returnVal = extras?.getString(PushConstants.NOT_ID)?.toInt() ?: 0
        } catch (e: NumberFormatException) {
            Log.e(TAG, "NumberFormatException occurred: ${PushConstants.NOT_ID}: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Exception occurred when parsing ${PushConstants.NOT_ID}: ${e.message}")
        }

        return returnVal
    }

    fun isAvailableSender(pushSharedPref: SharedPreferences, from: String?): Boolean {
        val savedSenderID = pushSharedPref.getString(PushConstants.SENDER_ID, "")
        Log.d(TAG, "sender id = $savedSenderID")
        return from == savedSenderID || from!!.startsWith("/topics/")
    }

    fun getNotificationVisibility(value: String): Int {
        return when (value) {
            VISIBILITY_PUBLIC_STR -> NotificationCompat.VISIBILITY_PUBLIC
            VISIBILITY_PRIVATE_STR -> NotificationCompat.VISIBILITY_PRIVATE
            VISIBILITY_SECRET_STR -> NotificationCompat.VISIBILITY_SECRET
            else -> { NotificationCompat.VISIBILITY_PRIVATE }
        }
    }


}
