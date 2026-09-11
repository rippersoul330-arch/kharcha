package com.kharcha.app.data

import kotlinx.coroutines.flow.Flow

/**
 * Single point of access to the app's data. All UI, the overlay entry screen,
 * and the export feature go through here.
 */
class KharchaRepository(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao
) {

    // ---- Categories ----

    fun observeCategories(): Flow<List<Category>> = categoryDao.observeAll()

    suspend fun getCategories(): List<Category> = categoryDao.getAll()

    suspend fun addCategory(name: String, colorHex: String): Long =
        categoryDao.insert(Category(name = name.trim(), colorHex = colorHex))

    suspend fun updateCategory(category: Category) = categoryDao.update(category)

    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)

    /**
     * Seeds the built-in starter categories the first time the app runs.
     * Safe to call on every launch — it only inserts when the table is empty.
     */
    suspend fun ensureDefaultCategories() {
        if (categoryDao.count() > 0) return
        categoryDao.insertAll(DEFAULT_CATEGORIES)
    }

    // ---- Expenses ----

    fun observeExpensesBetween(start: Long, end: Long): Flow<List<ExpenseWithCategory>> =
        expenseDao.observeBetween(start, end)

    fun observeCategoryTotalsBetween(start: Long, end: Long): Flow<List<CategoryTotal>> =
        expenseDao.observeCategoryTotalsBetween(start, end)

    fun observeTotalBetween(start: Long, end: Long): Flow<Long?> =
        expenseDao.observeTotalBetween(start, end)

    suspend fun getExpense(id: Long): Expense? = expenseDao.getById(id)

    suspend fun addExpense(amountPaise: Long, categoryId: Long?, note: String?, timestamp: Long = System.currentTimeMillis()): Long =
        expenseDao.insert(
            Expense(
                amountPaise = amountPaise,
                categoryId = categoryId,
                note = note?.trim()?.ifBlank { null },
                timestamp = timestamp
            )
        )

    suspend fun updateExpense(expense: Expense) = expenseDao.update(expense)

    suspend fun deleteExpense(expense: Expense) = expenseDao.delete(expense)

    suspend fun getAllExpensesForExport(): List<ExpenseWithCategory> = expenseDao.getAllForExport()

    companion object {
        /** Built-in starter categories with distinct pie-chart colours. */
        val DEFAULT_CATEGORIES = listOf(
            Category(name = "Snacks", colorHex = "#FF7043", isDefault = true),
            Category(name = "Food", colorHex = "#66BB6A", isDefault = true),
            Category(name = "Travel", colorHex = "#42A5F5", isDefault = true),
            Category(name = "Transport", colorHex = "#FFA726", isDefault = true),
            Category(name = "Clothes", colorHex = "#AB47BC", isDefault = true),
            Category(name = "Groceries", colorHex = "#26A69A", isDefault = true),
            Category(name = "Bills", colorHex = "#EC407A", isDefault = true),
            Category(name = "Other", colorHex = "#78909C", isDefault = true)
        )
    }
}
