package com.kharcha.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kharcha.app.data.ExpenseWithCategory
import com.kharcha.app.util.Money
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class Tab { HOME, CATEGORIES, SETTINGS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: HomeViewModel,
    status: AppStatus,
    actions: AppActions
) {
    var tab by remember { mutableStateOf(Tab.HOME) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Kharcha") })
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == Tab.HOME,
                    onClick = { tab = Tab.HOME },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = tab == Tab.CATEGORIES,
                    onClick = { tab = Tab.CATEGORIES },
                    icon = { Icon(Icons.Filled.Category, contentDescription = "Categories") },
                    label = { Text("Categories") }
                )
                NavigationBarItem(
                    selected = tab == Tab.SETTINGS,
                    onClick = { tab = Tab.SETTINGS },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        },
        floatingActionButton = {
            if (tab == Tab.HOME) {
                FloatingActionButton(onClick = actions.onAddExpense) {
                    Icon(Icons.Filled.Add, contentDescription = "Add expense")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (tab) {
                Tab.HOME -> HomeContent(viewModel, actions)
                Tab.CATEGORIES -> CategoriesContent(viewModel)
                Tab.SETTINGS -> SettingsContent(status, actions)
            }
        }
    }
}

@Composable
private fun HomeContent(viewModel: HomeViewModel, actions: AppActions) {
    val month by viewModel.month.collectAsState()
    val totals by viewModel.categoryTotals.collectAsState()
    val grandTotal by viewModel.monthTotal.collectAsState()
    val expenses by viewModel.expenses.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.showPreviousMonth() }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
                }
                Text(text = month.label, style = MaterialTheme.typography.titleMedium)
                IconButton(
                    onClick = { viewModel.showNextMonth() },
                    enabled = !month.isCurrentMonth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month")
                }
            }
        }

        item {
            SpendingPieChart(
                totals = totals,
                grandTotalPaise = grandTotal,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            Text(
                text = "Expenses",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
        }

        if (expenses.isEmpty()) {
            item {
                Text(
                    text = "Nothing logged this month. Shake your phone or tap + to add one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(expenses, key = { it.id }) { expense ->
                ExpenseRow(expense = expense, onClick = { actions.onEditExpense(expense.id) })
            }
        }
    }
}

@Composable
private fun ExpenseRow(expense: ExpenseWithCategory, onClick: () -> Unit) {
    val timeFmt = remember { SimpleDateFormat("d MMM, h:mm a", Locale.getDefault()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableNoRipple(onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(colorFromHex(expense.colorHex))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = expense.categoryName ?: "Uncategorised",
                style = MaterialTheme.typography.bodyLarge
            )
            val subtitle = buildString {
                append(timeFmt.format(Date(expense.timestamp)))
                if (!expense.note.isNullOrBlank()) append("  •  ${expense.note}")
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = Money.formatRupees(expense.amountPaise),
            style = MaterialTheme.typography.titleMedium
        )
    }
}
