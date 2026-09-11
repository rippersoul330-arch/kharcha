package com.kharcha.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A spending category (e.g. Snacks, Travel). Users can add their own.
 *
 * @param colorHex the slice colour used in the monthly pie chart, e.g. "#FF7043".
 * @param isDefault true for the built-in starter categories that ship with the app.
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String,
    val isDefault: Boolean = false
)
