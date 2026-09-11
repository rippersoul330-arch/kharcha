package com.kharcha.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): Expense?

    /** Expenses in a time range (a calendar month), newest first, with category info. */
    @Query(
        """
        SELECT e.id AS id, e.amountPaise AS amountPaise, e.categoryId AS categoryId,
               c.name AS categoryName, c.colorHex AS colorHex,
               e.timestamp AS timestamp, e.note AS note
        FROM expenses e
        LEFT JOIN categories c ON e.categoryId = c.id
        WHERE e.timestamp BETWEEN :start AND :end
        ORDER BY e.timestamp DESC
        """
    )
    fun observeBetween(start: Long, end: Long): Flow<List<ExpenseWithCategory>>

    /** Spending grouped by category over a time range — feeds the pie chart. */
    @Query(
        """
        SELECT e.categoryId AS categoryId, c.name AS categoryName, c.colorHex AS colorHex,
               SUM(e.amountPaise) AS totalPaise
        FROM expenses e
        LEFT JOIN categories c ON e.categoryId = c.id
        WHERE e.timestamp BETWEEN :start AND :end
        GROUP BY e.categoryId
        ORDER BY totalPaise DESC
        """
    )
    fun observeCategoryTotalsBetween(start: Long, end: Long): Flow<List<CategoryTotal>>

    /** Grand total spent over a time range (null when there are no expenses). */
    @Query("SELECT SUM(amountPaise) FROM expenses WHERE timestamp BETWEEN :start AND :end")
    fun observeTotalBetween(start: Long, end: Long): Flow<Long?>

    /** Every expense, oldest first — used to build the backup/export file. */
    @Query(
        """
        SELECT e.id AS id, e.amountPaise AS amountPaise, e.categoryId AS categoryId,
               c.name AS categoryName, c.colorHex AS colorHex,
               e.timestamp AS timestamp, e.note AS note
        FROM expenses e
        LEFT JOIN categories c ON e.categoryId = c.id
        ORDER BY e.timestamp ASC
        """
    )
    suspend fun getAllForExport(): List<ExpenseWithCategory>
}
