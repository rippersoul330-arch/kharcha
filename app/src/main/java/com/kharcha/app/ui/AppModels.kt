package com.kharcha.app.ui

/** Actions the UI can trigger; implemented by MainActivity (permissions, intents). */
data class AppActions(
    val onAddExpense: () -> Unit,
    val onEditExpense: (Long) -> Unit,
    val onToggleShake: (Boolean) -> Unit,
    val onGrantOverlay: () -> Unit,
    val onIgnoreBattery: () -> Unit,
    val onExport: () -> Unit,
    val onSendFeedback: () -> Unit
)

/** Live permission / feature status, refreshed whenever the app resumes. */
data class AppStatus(
    val overlayGranted: Boolean,
    val shakeEnabled: Boolean,
    val notificationsGranted: Boolean
)
