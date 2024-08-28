package com.adobe.phonegap.push

import android.content.Context
import android.content.Intent
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

    const val EXTRA_BUTTON_ACTION = "extra_button_action"
    const val EXTRA_CALLBACK_URL = "extra_callback_url"
    const val EXTRA_CALL_ID = "extra_call_id"

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

    fun dismissVOIPNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(NotificationUtils.VOIP_NOTIFICATION_ID)
        IncomingCallActivity.instance?.finish()
    }

    fun handleActionCall(context: Context, intent: Intent, voipStatus: String) {
        val callbackUrl = intent.getStringExtra(EXTRA_CALLBACK_URL)
        val callId = intent.getStringExtra(EXTRA_CALL_ID)

        // Handle actiontest
        dismissVOIPNotification(context)

        // Update Webhook status to CONNECTED
        updateWebhookVOIPStatus(callbackUrl, callId, voipStatus) { result ->
            if (result) { checkRedirectIfNext(context, voipStatus) }
        }
    }

    private fun checkRedirectIfNext(context: Context, voipStatus: String) {
        // Start cordova activity on answer
        if (voipStatus == IncomingCallActivity.VOIP_ACCEPT) {
            context.startActivity(AndroidUtils.intentForLaunchActivity(context))
        } else {
            exitProcess(0)
        }
    }
}
