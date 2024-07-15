package com.adobe.phonegap.push

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.system.exitProcess

object IncomingCallHelper {

    fun updateWebhookVOIPStatus(url: String?, callId: String?, status: String, callback: ((Boolean) -> Unit)? = null) {

        val client = OkHttpClient()
        val urlBuilder = HttpUrl.parse(url)?.newBuilder()
        urlBuilder?.addQueryParameter("id", callId)
        urlBuilder?.addQueryParameter("input", status)
        val urlBuilt: String = urlBuilder?.build().toString()
        val request = Request.Builder().url(urlBuilt).build()
        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("", "Update For CallId $callId and Status $status failed")
                    callback?.invoke(false)
                }
                override fun onResponse(call: Call, response: Response) {
                    Log.d("", "Update For CallId $callId and Status $status successful")
                    callback?.invoke(true)
                }
            })
    }

    fun finishApp() {
        IncomingCallActivity.instance?.get()?.finishAndRemoveTask()
    }

    fun finishCallScreen() {
        IncomingCallActivity.instance?.get()?.finish()
    }

    fun dismissVOIPNotification(context: Context, finishCallScreen: Boolean = false) {
        NotificationManagerCompat.from(context).cancel(FCMService.VOIP_NOTIFICATION_ID)
        if (finishCallScreen) {
            finishCallScreen()
        }
    }

    fun handleActionCall(context: Context, intent: Intent, voipStatus: String) {
        val callbackUrl = intent.getStringExtra(PushConstants.VOIP_EXTRA_CALLBACK_URL)
        val callId = intent.getStringExtra(PushConstants.VOIP_EXTRA_CALL_ID)

        // Handle actiontest
        dismissVOIPNotification(context)
        if (voipStatus == PushConstants.VOIP_ACCEPT_KEY) {
            finishCallScreen()
        }

        // Update Webhook status to CONNECTED
        updateWebhookVOIPStatus(callbackUrl, callId, voipStatus) { result ->
            if (result) { checkRedirectIfNext(context, voipStatus) }
        }
    }

    private fun checkRedirectIfNext(context: Context, voipStatus: String) {
        // Start cordova activity on answer
        if (voipStatus == PushConstants.VOIP_ACCEPT_KEY) {
          context.startActivity(intentForLaunchActivity(context))
        } else {
          finishApp()
        }
    }

    fun intentForLaunchActivity(context: Context): Intent? {
        val pm = context.packageManager
        val packageName = context.packageName
        return pm?.getLaunchIntentForPackage(packageName)
    }

    fun defaultRingtoneUri(): Uri {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    }
}
