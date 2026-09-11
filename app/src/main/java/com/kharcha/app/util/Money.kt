package com.kharcha.app.util

import java.text.NumberFormat
import java.util.Locale

/** Helpers for converting between rupees (what the user types) and paise (what we store). */
object Money {

    private val indiaLocale = Locale("en", "IN")

    /** "49.50" or "49" -> 4950 paise. Returns null if the text isn't a valid amount. */
    fun rupeesToPaise(text: String): Long? {
        val cleaned = text.trim().replace(",", "").removePrefix("₹").trim()
        if (cleaned.isEmpty()) return null
        val value = cleaned.toDoubleOrNull() ?: return null
        if (value < 0) return null
        return Math.round(value * 100.0)
    }

    /** 4950 -> "49.50" (no symbol) for editing fields. */
    fun paiseToPlainString(paise: Long): String {
        val rupees = paise / 100.0
        return if (paise % 100 == 0L) rupees.toLong().toString()
        else String.format(Locale.US, "%.2f", rupees)
    }

    /** 4950 -> "₹49.50" grouped in the Indian numbering style for display. */
    fun formatRupees(paise: Long): String {
        val format = NumberFormat.getCurrencyInstance(indiaLocale)
        format.maximumFractionDigits = 2
        format.minimumFractionDigits = if (paise % 100 == 0L) 0 else 2
        return format.format(paise / 100.0)
    }
}
