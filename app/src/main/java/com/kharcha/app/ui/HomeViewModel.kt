package com.kharcha.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.app.KharchaApplication
import com.kharcha.app.data.CategoryTotal
import com.kharcha.app.data.ExpenseWithCategory
import com.kharcha.app.util.MonthRange
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Holds the currently viewed month and exposes the expenses, per-category totals
 * and grand total for that month.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = (app as KharchaApplication).repository

    private val _month = MutableStateFlow(MonthRange.current())
    val month: StateFlow<MonthRange> = _month

    val expenses: StateFlow<List<ExpenseWithCategory>> =
        _month.flatMapLatest { m -> repository.observeExpensesBetween(m.startMillis, m.endMillis) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoryTotals: StateFlow<List<CategoryTotal>> =
        _month.flatMapLatest { m -> repository.observeCategoryTotalsBetween(m.startMillis, m.endMillis) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthTotal: StateFlow<Long> =
        _month.flatMapLatest { m -> repository.observeTotalBetween(m.startMillis, m.endMillis) }
            .map { it ?: 0L }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun showPreviousMonth() {
        _month.value = _month.value.previous()
    }

    fun showNextMonth() {
        if (!_month.value.isCurrentMonth()) {
            _month.value = _month.value.next()
        }
    }
}
