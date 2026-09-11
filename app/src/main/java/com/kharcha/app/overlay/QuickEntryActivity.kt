package com.kharcha.app.overlay

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.kharcha.app.data.Category
import com.kharcha.app.data.Expense
import com.kharcha.app.kharchaRepository
import com.kharcha.app.ui.QuickEntryScreen
import com.kharcha.app.ui.theme.KharchaTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * A small, transparent floating screen that captures amount + category as fast
 * as possible. Opened either by tapping the floating button (after a shake) or
 * by the in-app "+" button. Also reused to EDIT an existing expense when
 * [EXTRA_EXPENSE_ID] is provided.
 */
class QuickEntryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = kharchaRepository
        val editId = intent.getLongExtra(EXTRA_EXPENSE_ID, -1L).takeIf { it > 0 }

        setContent {
            KharchaTheme {
                var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
                var existing by remember { mutableStateOf<Expense?>(null) }
                var loaded by remember { mutableStateOf(editId == null) }

                LaunchedEffect(Unit) {
                    categories = repository.observeCategories().first()
                    if (editId != null) {
                        existing = repository.getExpense(editId)
                        loaded = true
                    }
                }

                if (loaded) {
                    QuickEntryScreen(
                        categories = categories,
                        initialAmountPaise = existing?.amountPaise,
                        initialCategoryId = existing?.categoryId,
                        initialNote = existing?.note,
                        isEdit = editId != null,
                        onSave = { amountPaise, categoryId, note ->
                            saveExpense(editId, existing, amountPaise, categoryId, note)
                        },
                        onDelete = if (editId != null) {
                            { deleteExpense(existing) }
                        } else null,
                        onCancel = { finish() }
                    )
                }
            }
        }
    }

    private fun saveExpense(
        editId: Long?,
        existing: Expense?,
        amountPaise: Long,
        categoryId: Long?,
        note: String?
    ) {
        val repository = kharchaRepository
        lifecycleScope.launch {
            if (editId != null && existing != null) {
                repository.updateExpense(
                    existing.copy(amountPaise = amountPaise, categoryId = categoryId, note = note)
                )
                toast(getString(com.kharcha.app.R.string.expense_updated))
            } else {
                repository.addExpense(amountPaise, categoryId, note)
                toast(getString(com.kharcha.app.R.string.expense_saved))
            }
            finish()
        }
    }

    private fun deleteExpense(existing: Expense?) {
        val repository = kharchaRepository
        existing ?: run { finish(); return }
        lifecycleScope.launch {
            repository.deleteExpense(existing)
            toast(getString(com.kharcha.app.R.string.expense_deleted))
            finish()
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_EXPENSE_ID = "extra_expense_id"
    }
}
