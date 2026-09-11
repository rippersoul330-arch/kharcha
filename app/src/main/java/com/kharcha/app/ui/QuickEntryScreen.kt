package com.kharcha.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kharcha.app.data.Category
import com.kharcha.app.util.Money
import kotlinx.coroutines.launch

/**
 * The fast amount + category capture card. Designed to be filled in ~2 seconds:
 * the amount field is auto-focused with the number pad already up. Includes a
 * "+ New" chip so categories can be created right here (no separate screen).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickEntryScreen(
    categories: List<Category>,
    palette: List<String>,
    initialAmountPaise: Long?,
    initialCategoryId: Long?,
    initialNote: String?,
    isEdit: Boolean,
    onSave: (amountPaise: Long, categoryId: Long?, note: String?) -> Unit,
    onAddCategory: suspend (name: String, colorHex: String) -> Long,
    onDelete: (() -> Unit)?,
    onCancel: () -> Unit
) {
    var amountText by remember {
        mutableStateOf(initialAmountPaise?.let { Money.paiseToPlainString(it) } ?: "")
    }
    var selectedCategoryId by remember { mutableStateOf(initialCategoryId) }
    var note by remember { mutableStateOf(initialNote ?: "") }
    var showAddCategory by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }

    // Auto-select the first category once the list has loaded (for new entries).
    LaunchedEffect(categories) {
        if (selectedCategoryId == null && categories.isNotEmpty()) {
            selectedCategoryId = categories.first().id
        }
    }

    val amountPaise = Money.rupeesToPaise(amountText)
    val canSave = amountPaise != null && amountPaise > 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .clickableNoRipple(onCancel),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clickableNoRipple {}
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (isEdit) "Edit expense" else "Add expense",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { new -> amountText = new.filter { it.isDigit() || it == '.' } },
                    label = { Text("Amount") },
                    prefix = { Text("₹ ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .focusRequester(focusRequester)
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategoryId == category.id,
                            onClick = { selectedCategoryId = category.id },
                            label = { Text(category.name) }
                        )
                    }
                    AssistChip(
                        onClick = { showAddCategory = true },
                        label = { Text("New") },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(AssistChipDefaults.IconSize)
                            )
                        }
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        Box(modifier = Modifier.weight(1f))
                    }
                    TextButton(onClick = onCancel) { Text("Cancel") }
                    Button(
                        onClick = {
                            val paise = amountPaise ?: return@Button
                            onSave(paise, selectedCategoryId, note.ifBlank { null })
                        },
                        enabled = canSave,
                        modifier = Modifier.padding(start = 8.dp)
                    ) { Text(if (isEdit) "Update" else "Save") }
                }
            }
        }
    }

    if (showAddCategory) {
        AddCategoryDialog(
            palette = palette,
            onDismiss = { showAddCategory = false },
            onConfirm = { name, color ->
                scope.launch {
                    val newId = onAddCategory(name, color)
                    selectedCategoryId = newId
                }
                showAddCategory = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddCategoryDialog(
    palette: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(palette.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New category") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Colour",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    palette.forEach { hex ->
                        val isSelected = hex == selectedColor
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colorFromHex(hex))
                                .then(
                                    if (isSelected) Modifier.border(3.dp, Color.Black, CircleShape)
                                    else Modifier
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, selectedColor) },
                enabled = name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
