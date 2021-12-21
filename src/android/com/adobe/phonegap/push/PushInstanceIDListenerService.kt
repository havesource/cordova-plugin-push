package com.adobe.phonegap.push

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import java.lang.NullPointerException


/**
 * Push InstanceID Listener Service
 */
@Suppress("HardCodedStringLiteral")
@SuppressLint("LongLogTag", "LogConditional")
class PushInstanceIDListenerService : FirebaseMessagingService() {
  companion object {
    private const val TAG: String = "${PushPlugin.PREFIX_TAG} (PushInstanceIDListenerService)"
  }

  /**
   * Called when a new token is generated, after app install or token changes.
   *
   * @param token
   */
  override fun onNewToken(token: String) {
  try {
    super.onNewToken(token)
    FirebaseInstanceId.getInstance().instanceId
      .addOnSuccessListener { instanceIdResult ->
        // Get updated InstanceID token.
        val refreshedToken = instanceIdResult.token
        Log.d(TAG, "Refreshed token: $refreshedToken")

        // TODO: Implement this method to send any registration to your app's servers.
        //sendRegistrationToServer(refreshedToken);
      }
      } catch (e: NullPointerException) {
      Log.e(TAG, "execute: Null Pointer Exception in pushplugin initialize");
      }
  }
}
