package com.adobe.phonegap.push

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager

class FullScreenActivity : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(LOG_TAG, "onCreate")
        super.onCreate(savedInstanceState)
        turnScreenOnAndKeyguardOff()
        forceMainActivityReload()
        finish()
    }

    private fun forceMainActivityReload() {
        val pm: PackageManager = getPackageManager()
        val launchIntent: Intent? =
            pm.getLaunchIntentForPackage(getApplicationContext().getPackageName())
        launchIntent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        launchIntent?.addFlags(Intent.FLAG_FROM_BACKGROUND)
        startActivity(launchIntent)
    }

    protected override fun onDestroy() {
        super.onDestroy()
        Log.d(LOG_TAG, "onDestroy")
        turnScreenOffAndKeyguardOn()
    }

    private fun turnScreenOnAndKeyguardOff() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            Log.d(LOG_TAG, "setShowWhenLocked")
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            Log.d(LOG_TAG, "addFlags")
            getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }
        val keyguardManager: KeyguardManager? = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && keyguardManager != null) {
            keyguardManager?.requestDismissKeyguard(this, object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissCancelled() {
                    super.onDismissCancelled()
                    Log.d(LOG_TAG, "canceled")
                }

                override fun onDismissError() {
                    super.onDismissError()
                    Log.d(LOG_TAG, "onDismissError")
                }

                override fun onDismissSucceeded() {
                    super.onDismissSucceeded()
                    Log.d(LOG_TAG, "onDismissSucceeded")
                }
            })
        }
    }

    private fun turnScreenOffAndKeyguardOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(false)
            setTurnScreenOn(false)
        } else {
            getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }
    }

    companion object {
        private const val LOG_TAG = "FullScreenActivity"
    }
}
