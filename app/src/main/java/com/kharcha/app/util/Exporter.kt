package com.kharcha.app.util

import com.kharcha.app.data.ExpenseWithCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Builds the backup/export file contents from the stored expenses. */
object Exporter {

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val timeFmt = SimpleDateFormat("HH:mm", Locale.US)

    /** A spreadsheet-friendly CSV of every expense, oldest first. */
    fun buildCsv(expenses: List<ExpenseWithCategory>): String {
        val sb = StringBuilder()
        sb.append("Date,Time,Amount (INR),Category,Note\n")
        expenses.forEach { e ->
            val date = Date(e.timestamp)
            val amount = Money.paiseToPlainString(e.amountPaise)
            val category = escape(e.categoryName ?: "Uncategorised")
            val note = escape(e.note ?: "")
            sb.append("${dateFmt.format(date)},${timeFmt.format(date)},$amount,$category,$note\n")
        }
        return sb.toString()
    }

    /** Quote fields that contain commas, quotes or newlines (RFC-4180 style). */
    private fun escape(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    fun suggestedFileName(): String {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        return "kharcha_backup_$stamp.csv"
    }
}
