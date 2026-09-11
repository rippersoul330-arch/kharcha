package com.kharcha.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Settings

/**
 * Restarts the shake listener after the phone reboots — but only if the user
 * had it switched on and still has the "draw over other apps" permission.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val prefs: SharedPreferences =
            context.getSharedPreferences("kharcha_prefs", Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("shake_service_enabled", false)

        if (enabled && Settings.canDrawOverlays(context)) {
            ShakeDetectionService.start(context)
        }
    }
}
