package com.kharcha.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single logged expense.
 *
 * The amount is stored in *paise* (integer) to avoid floating-point rounding
 * errors — e.g. ₹49.50 is stored as 4950. Display code divides by 100.
 *
 * If a category is deleted, its expenses are kept but their [categoryId] is set
 * to null ("Uncategorised") so history is never lost.
 */
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId"), Index("timestamp")]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountPaise: Long,
    val categoryId: Long?,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null
)
