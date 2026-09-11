package com.kharcha.app.ui

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * First-run guided setup. Walks the tester through the permissions the
 * shake-from-anywhere feature needs, so they don't get stuck. Brand-specific
 * Auto-start guidance is shown on phones that need it (Oppo, Xiaomi, etc.).
 */
@Composable
fun OnboardingScreen(
    status: AppStatus,
    needsAutostart: Boolean,
    brandLabel: String,
    onGrantOverlay: () -> Unit,
    onGrantNotifications: () -> Unit,
    onOpenBattery: () -> Unit,
    onOpenAutostart: () -> Unit,
    onTestOverlay: () -> Unit,
    onFinish: () -> Unit
) {
    val needsNotificationStep = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            "Welcome to Kharcha 👋",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Log any expense in 2 seconds — just shake your phone from anywhere and a " +
                "button pops up. Let's enable that quickly:",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))

        StepCard(
            number = "1",
            title = "Allow \"appear on top\"",
            description = "Lets the little button show over other apps. This is the main one.",
            done = status.overlayGranted,
            buttonText = if (status.overlayGranted) "Granted" else "Allow",
            onClick = onGrantOverlay
        ) {
            TextButton(onClick = onTestOverlay) { Text("Test it") }
        }

        if (needsNotificationStep) {
            StepCard(
                number = "2",
                title = "Allow notifications",
                description = "Android requires a small \"Kharcha is running\" notification while " +
                    "the shake listener is on.",
                done = status.notificationsGranted,
                buttonText = if (status.notificationsGranted) "Granted" else "Allow",
                onClick = onGrantNotifications
            )
        }

        StepCard(
            number = if (needsNotificationStep) "3" else "2",
            title = "Keep it running (battery)",
            description = "Stops your phone from pausing Kharcha to save battery, which would " +
                "break shake detection.",
            done = null,
            buttonText = "Open battery settings",
            onClick = onOpenBattery
        )

        if (needsAutostart) {
            StepCard(
                number = if (needsNotificationStep) "4" else "3",
                title = "Enable Auto-start ($brandLabel)",
                description = "$brandLabel phones need Auto-start turned on or they stop the app " +
                    "in the background. Tap below and switch Kharcha ON in the list.",
                done = null,
                buttonText = "Open Auto-start settings",
                onClick = onOpenAutostart
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get started")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "You can change all of this later in Settings.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StepCard(
    number: String,
    title: String,
    description: String,
    done: Boolean?,
    buttonText: String,
    onClick: () -> Unit,
    extra: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            when (done) {
                true -> Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Done",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(28.dp)
                )
                false -> Icon(
                    Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(28.dp)
                )
                null -> Text(
                    number,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (done == true) {
                        OutlinedButton(onClick = onClick, enabled = false) { Text(buttonText) }
                    } else {
                        Button(onClick = onClick) { Text(buttonText) }
                    }
                    extra?.invoke()
                }
            }
        }
    }
}
