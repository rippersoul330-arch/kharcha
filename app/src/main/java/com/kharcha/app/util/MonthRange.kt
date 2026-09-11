package com.kharcha.app.util

import java.util.Calendar
import java.util.Locale

/**
 * Represents one calendar month (1st 00:00:00 to last day 23:59:59.999) as an
 * epoch-millis range, so Room can filter expenses that belong to that month.
 */
data class MonthRange(
    val year: Int,
    val month: Int, // 0 = January, 11 = December (Calendar convention)
    val startMillis: Long,
    val endMillis: Long
) {
    val label: String
        get() {
            val c = Calendar.getInstance().apply { set(Calendar.YEAR, year); set(Calendar.MONTH, month) }
            val monthName = c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
            return "$monthName $year"
        }

    fun previous(): MonthRange = of(year, month - 1)
    fun next(): MonthRange = of(year, month + 1)

    /** True if this range contains "now" — used to disable the "next month" arrow. */
    fun isCurrentMonth(): Boolean {
        val now = Calendar.getInstance()
        return now.get(Calendar.YEAR) == year && now.get(Calendar.MONTH) == month
    }

    companion object {
        fun current(): MonthRange {
            val now = Calendar.getInstance()
            return of(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
        }

        /** Handles month overflow/underflow (e.g. month = -1 rolls to December of the prior year). */
        fun of(year: Int, month: Int): MonthRange {
            val start = Calendar.getInstance().apply {
                clear()
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val end = (start.clone() as Calendar).apply {
                add(Calendar.MONTH, 1)
                add(Calendar.MILLISECOND, -1)
            }
            return MonthRange(
                year = start.get(Calendar.YEAR),
                month = start.get(Calendar.MONTH),
                startMillis = start.timeInMillis,
                endMillis = end.timeInMillis
            )
        }
    }
}
