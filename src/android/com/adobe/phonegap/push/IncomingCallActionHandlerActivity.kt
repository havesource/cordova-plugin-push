package com.adobe.phonegap.push

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

class IncomingCallActionHandlerActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Log.d(LOG_TAG, "onCreate()")
    handleNotification(this, intent)
    finish()
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    Log.d(LOG_TAG, "onNewIntent()")
    handleNotification(this, intent)
    finish()
  }

  companion object {
    private const val LOG_TAG = "Push_IncomingCallActionHandlerActivity"

    private fun handleNotification(context: Context, intent: Intent) {
        val voipStatus = intent.getStringExtra(PushConstants.VOIP_EXTRA_BUTTON_ACTION) ?: return
        IncomingCallHelper.handleActionCall(context, intent, voipStatus)
    }
  }
}
