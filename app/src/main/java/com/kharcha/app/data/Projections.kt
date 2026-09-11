package com.kharcha.app.data

/**
 * An expense joined with its category name/colour, for showing in lists.
 * categoryName/colorHex are null when the category was deleted.
 */
data class ExpenseWithCategory(
    val id: Long,
    val amountPaise: Long,
    val categoryId: Long?,
    val categoryName: String?,
    val colorHex: String?,
    val timestamp: Long,
    val note: String?
)

/**
 * The summed spending for one category over a time range — powers the pie chart.
 */
data class CategoryTotal(
    val categoryId: Long?,
    val categoryName: String?,
    val colorHex: String?,
    val totalPaise: Long
)
