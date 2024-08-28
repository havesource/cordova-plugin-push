package com.adobe.phonegap.push

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.google.firebase.messaging.FirebaseMessagingService

object AndroidUtils {

    /**
     * Get the Application Name from Label
     */
    fun getAppName(context: Context): String {
        return context.packageManager.getApplicationLabel(context.applicationInfo) as String
    }

    fun intentForLaunchActivity(context: Context): Intent? {
        val pm = context.packageManager
        val packageName = context.packageName
        return pm?.getLaunchIntentForPackage(packageName)
    }

    fun getPushSharedPref(context: Context): SharedPreferences {
        return context.getSharedPreferences(
            PushConstants.COM_ADOBE_PHONEGAP_PUSH,
            FirebaseMessagingService.MODE_PRIVATE
        )
    }
}
