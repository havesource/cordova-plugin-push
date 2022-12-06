package com.adobe.phonegap.push

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 *
 */
@Suppress("HardCodedStringLiteral")
@SuppressLint("LongLogTag")
class PushDismissedHandler : BroadcastReceiver() {
  companion object {
    private const val TAG: String = "${PushPlugin.PREFIX_TAG} (PushDismissedHandler)"
  }

  /**
   * @param context
   * @param intent
   */
  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == PushConstants.PUSH_DISMISSED) {
      val notID = intent.getIntExtra(PushConstants.NOT_ID, 0)
      Log.d(TAG, "not id = $notID")
      FCMService().setNotification(notID, "")
    }
  }
}
