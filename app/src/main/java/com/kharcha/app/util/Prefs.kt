package com.kharcha.app.util

import android.content.Context

/** Thin wrapper over SharedPreferences for the handful of settings we keep. */
class Prefs(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("kharcha_prefs", Context.MODE_PRIVATE)

    /** Whether the user has turned the always-on shake listener on. */
    var shakeServiceEnabled: Boolean
        get() = prefs.getBoolean(KEY_SERVICE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SERVICE_ENABLED, value).apply()

    /** Whether we've shown the first-run onboarding/permissions screen. */
    var onboardingComplete: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_DONE, value).apply()

    companion object {
        private const val KEY_SERVICE_ENABLED = "shake_service_enabled"
        private const val KEY_ONBOARDING_DONE = "onboarding_complete"
    }
}
