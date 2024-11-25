package com.adobe.phonegap.push

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

class OnNotificationReceiverActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(LOG_TAG, "OnNotificationReceiverActivity.onCreate()")
        handleNotification(this, intent)
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(LOG_TAG, "OnNotificationReceiverActivity.onNewIntent()")
        handleNotification(this, intent)
        finish()
    }

    companion object {
        private const val LOG_TAG = "Push_OnNotificationReceiverActivity"
        private fun handleNotification(context: Context, intent: Intent) {
            try {
                val pm = context.packageManager
                val launchIntent = pm.getLaunchIntentForPackage(context.packageName)
                launchIntent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                val data = intent.extras
                if (data?.containsKey(PushConstants.NOTIFY_MESSAGE_TYPE_KEY) == false) {
                    data.putString(PushConstants.NOTIFY_MESSAGE_TYPE_KEY, PushConstants.NOTIFY_NOTIFICATION_VALUE)
                }
                data?.putString(
                    PushConstants.NOTIFY_TAP_KEY,
                    if (PushPlugin.isInBackground) PushConstants.NOTIFY_BACKGROUND_VALUE else PushConstants.NOTIFY_FOREGROUND_VALUE
                )
                Log.d(
                    LOG_TAG,
                    "OnNotificationReceiverActivity.handleNotification(): " + data.toString()
                )
                PushPlugin.sendExtras(data)
                data?.apply {
                    launchIntent?.putExtras(data)
                }
                context.startActivity(launchIntent)
            } catch (e: Exception) {
                Log.e(LOG_TAG, e.localizedMessage, e)
            }
        }
    }
}
