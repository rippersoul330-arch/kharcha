package com.kharcha.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsContent(status: AppStatus, actions: AppActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Shake to log
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Shake to log", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Shake your phone from anywhere to pop up the quick-add button.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = status.shakeEnabled,
                        onCheckedChange = { actions.onToggleShake(it) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Overlay permission prompt (only when missing)
        if (!status.overlayGranted) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Permission needed",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "To show the floating button over other apps, Kharcha needs the " +
                            "\"appear on top\" permission.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Button(onClick = actions.onGrantOverlay) {
                        Text("Allow appear on top")
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Battery optimisation
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Keep it reliable", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Some phones stop background apps to save battery, which can break " +
                        "shake detection. Allow Kharcha to keep running for best results.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                OutlinedButton(onClick = actions.onIgnoreBattery) {
                    Text("Battery settings")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Data + feedback
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Data & feedback", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = actions.onExport,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Upload, contentDescription = null)
                    Spacer(modifier = Modifier.height(0.dp))
                    Text("  Export / backup (CSV)")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = actions.onSendFeedback,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Email, contentDescription = null)
                    Text("  Send feedback")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Kharcha • v1.0 — your data stays on this phone.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
