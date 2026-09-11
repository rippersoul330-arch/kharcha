package com.kharcha.app.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Locale

/**
 * Helps open the "Auto-start" / "Auto launch" settings screen on phone brands
 * that aggressively kill background apps (Xiaomi, Oppo, Vivo, Realme, OnePlus,
 * Huawei). Without auto-start enabled on these brands, the shake listener can't
 * run in the background.
 */
object AutostartHelper {

    private val aggressiveBrands = setOf(
        "xiaomi", "redmi", "poco", "oppo", "vivo", "realme", "oneplus", "huawei", "honor", "iqoo"
    )

    fun brandLabel(): String =
        Build.MANUFACTURER.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }

    /** True if this phone brand needs the extra auto-start permission. */
    fun needsAutostart(): Boolean =
        Build.MANUFACTURER.lowercase(Locale.getDefault()) in aggressiveBrands

    /** Tries to open the brand's auto-start screen. Returns false if none matched. */
    fun openAutostartSettings(context: Context): Boolean {
        for (intent in candidateIntents()) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (_: Exception) {
                // Try the next candidate.
            }
        }
        return false
    }

    private fun component(pkg: String, cls: String): Intent =
        Intent().setComponent(ComponentName(pkg, cls))

    private fun candidateIntents(): List<Intent> = listOf(
        // Xiaomi / Redmi / POCO (MIUI)
        component("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
        // Oppo / Realme (ColorOS)
        component("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
        component("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"),
        component("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"),
        component("com.coloros.phonemanager", "com.coloros.phonemanager.MainActivity"),
        // Vivo / iQOO
        component("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"),
        component("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"),
        // OnePlus
        component("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"),
        // Huawei / Honor
        component("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
        component("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")
    )
}
