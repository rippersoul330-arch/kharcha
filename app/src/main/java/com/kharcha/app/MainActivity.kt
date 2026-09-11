package com.kharcha.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kharcha.app.overlay.QuickEntryActivity
import com.kharcha.app.service.ShakeDetectionService
import com.kharcha.app.ui.AppActions
import com.kharcha.app.ui.AppStatus
import com.kharcha.app.ui.HomeViewModel
import com.kharcha.app.ui.MainScreen
import com.kharcha.app.ui.theme.KharchaTheme
import com.kharcha.app.util.Exporter
import com.kharcha.app.util.Prefs
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var prefs: Prefs

    // Live status the UI observes; refreshed in onResume.
    private val statusState = mutableStateOf(AppStatus(false, false, false))

    // Set when the user toggled "shake on" but still needs to grant overlay permission.
    private var pendingEnableAfterOverlay = false

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            refreshStatus()
        }

    private val overlaySettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Return handled in onResume (which always re-checks).
        }

    private val batterySettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    private val exportDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
            if (uri != null) writeExport(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        refreshStatus()

        setContent {
            KharchaTheme {
                val vm: HomeViewModel = viewModel()
                val status by statusState
                MainScreen(
                    viewModel = vm,
                    status = status,
                    actions = AppActions(
                        onAddExpense = ::openAddExpense,
                        onEditExpense = ::openEditExpense,
                        onToggleShake = ::toggleShake,
                        onGrantOverlay = ::requestOverlayPermission,
                        onIgnoreBattery = ::openBatterySettings,
                        onExport = ::startExport,
                        onSendFeedback = ::sendFeedback
                    )
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // If the user just granted overlay permission after toggling on, start now.
        if (pendingEnableAfterOverlay && Settings.canDrawOverlays(this)) {
            pendingEnableAfterOverlay = false
            enableShakeService()
        }
        refreshStatus()
    }

    // ---- Status ----

    private fun refreshStatus() {
        statusState.value = AppStatus(
            overlayGranted = Settings.canDrawOverlays(this),
            shakeEnabled = prefs.shakeServiceEnabled,
            notificationsGranted = notificationsGranted()
        )
    }

    private fun notificationsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true
    }

    // ---- Shake service ----

    private fun toggleShake(enable: Boolean) {
        if (!enable) {
            ShakeDetectionService.stop(this)
            prefs.shakeServiceEnabled = false
            refreshStatus()
            return
        }

        // Ask for notification permission (Android 13+) so the required notification can show.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationsGranted()) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        if (!Settings.canDrawOverlays(this)) {
            pendingEnableAfterOverlay = true
            requestOverlayPermission()
            return
        }

        enableShakeService()
    }

    private fun enableShakeService() {
        ShakeDetectionService.start(this)
        prefs.shakeServiceEnabled = true
        refreshStatus()
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        overlaySettingsLauncher.launch(intent)
    }

    @Suppress("BatteryLife")
    private fun openBatterySettings() {
        val intent = try {
            Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:$packageName")
            )
        } catch (e: Exception) {
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        }
        try {
            batterySettingsLauncher.launch(intent)
        } catch (e: Exception) {
            batterySettingsLauncher.launch(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
    }

    // ---- Expense entry ----

    private fun openAddExpense() {
        startActivity(Intent(this, QuickEntryActivity::class.java))
    }

    private fun openEditExpense(id: Long) {
        startActivity(
            Intent(this, QuickEntryActivity::class.java)
                .putExtra(QuickEntryActivity.EXTRA_EXPENSE_ID, id)
        )
    }

    // ---- Export ----

    private fun startExport() {
        exportDocumentLauncher.launch(Exporter.suggestedFileName())
    }

    private fun writeExport(uri: Uri) {
        lifecycleScope.launch {
            val csv = withContext(Dispatchers.IO) {
                val data = kharchaRepository.getAllExpensesForExport()
                Exporter.buildCsv(data)
            }
            val ok = withContext(Dispatchers.IO) {
                try {
                    contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) }
                    true
                } catch (e: Exception) {
                    false
                }
            }
            Toast.makeText(
                this@MainActivity,
                if (ok) "Backup saved" else "Couldn't save backup",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // ---- Feedback ----

    private fun sendFeedback() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.feedback_email)))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject))
        }
        try {
            startActivity(Intent.createChooser(intent, "Send feedback"))
        } catch (e: Exception) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }
}
