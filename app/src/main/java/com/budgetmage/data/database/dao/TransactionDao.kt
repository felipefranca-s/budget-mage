package com.budgetmage.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.budgetmage.data.database.entity.CategoryTotal
import com.budgetmage.data.database.entity.MonthSummary
import com.budgetmage.data.database.entity.TransactionEntity
import com.budgetmage.data.database.entity.TransactionType
import com.budgetmage.data.database.entity.TransactionWithDetails
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Transaction operations.
 */
@Dao
interface TransactionDao {

    @Query("""
        SELECT t.id, t.accountId, a.name as accountName, t.categoryId, c.name as categoryName,
               t.type, t.amountCents, t.date, t.description, t.createdAt, t.updatedAt
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        INNER JOIN accounts a ON t.accountId = a.id
        WHERE (:type IS NULL OR t.type = :type)
          AND (:categoryId IS NULL OR t.categoryId = :categoryId)
          AND (:accountId IS NULL OR t.accountId = :accountId)
          AND (:startDate IS NULL OR t.date >= :startDate)
          AND (:endDate IS NULL OR t.date <= :endDate)
        ORDER BY t.date DESC, t.createdAt DESC
    """)
    fun getFilteredTransactions(
        type: TransactionType? = null,
        categoryId: Long? = null,
        accountId: Long? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): Flow<List<TransactionWithDetails>>

    @Query("""
        SELECT t.id, t.accountId, a.name as accountName, t.categoryId, c.name as categoryName,
               t.type, t.amountCents, t.date, t.description, t.createdAt, t.updatedAt
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        INNER JOIN accounts a ON t.accountId = a.id
        WHERE (:type IS NULL OR t.type = :type)
          AND (:categoryId IS NULL OR t.categoryId = :categoryId)
          AND (:accountId IS NULL OR t.accountId = :accountId)
          AND (:startDate IS NULL OR t.date >= :startDate)
          AND (:endDate IS NULL OR t.date <= :endDate)
        ORDER BY t.date DESC, t.createdAt DESC
    """)
    fun getFilteredTransactionsPaged(
        type: TransactionType? = null,
        categoryId: Long? = null,
        accountId: Long? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): PagingSource<Int, TransactionWithDetails>

    @Query("""
        SELECT t.id, t.accountId, a.name as accountName, t.categoryId, c.name as categoryName,
               t.type, t.amountCents, t.date, t.description, t.createdAt, t.updatedAt
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        INNER JOIN accounts a ON t.accountId = a.id
        WHERE t.id = :id
    """)
    fun getTransactionById(id: Long): Flow<TransactionWithDetails?>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionEntityById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    // Dashboard aggregation queries

    @Query("""
        SELECT
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amountCents ELSE 0 END), 0) as totalIncomeCents,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amountCents ELSE 0 END), 0) as totalExpenseCents
        FROM transactions
        WHERE date >= :startDay AND date < :endDay
    """)
    fun getMonthSummary(startDay: Long, endDay: Long): Flow<MonthSummary>

    @Query("""
        SELECT c.id as categoryId, c.name as categoryName, SUM(t.amountCents) as totalCents
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE t.type = 'EXPENSE'
          AND t.date >= :startDay
          AND t.date < :endDay
        GROUP BY t.categoryId
        ORDER BY totalCents DESC
        LIMIT :limit
    """)
    fun getTopExpenseCategories(startDay: Long, endDay: Long, limit: Int = 5): Flow<List<CategoryTotal>>

    @Query("""
        SELECT c.id as categoryId, c.name as categoryName, SUM(t.amountCents) as totalCents
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE t.type = 'EXPENSE'
          AND t.date >= :startDay
          AND t.date < :endDay
        GROUP BY t.categoryId
        ORDER BY totalCents DESC
    """)
    fun getAllExpenseCategories(startDay: Long, endDay: Long): Flow<List<CategoryTotal>>
}
