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
          AND (:hasCategoryFilter = 0 OR t.categoryId IN (:categoryIds))
          AND (:accountId IS NULL OR t.accountId = :accountId)
          AND (:startDate IS NULL OR t.date >= :startDate)
          AND (:endDate IS NULL OR t.date <= :endDate)
          AND (:hasDescriptionFilter = 0 OR t.description LIKE '%' || :descriptionQuery || '%' COLLATE NOCASE)
        ORDER BY t.date DESC, t.createdAt DESC
    """)
    fun getFilteredTransactions(
        type: TransactionType? = null,
        hasCategoryFilter: Int = 0,
        categoryIds: List<Long> = listOf(-1L),
        accountId: Long? = null,
        startDate: Long? = null,
        endDate: Long? = null,
        hasDescriptionFilter: Int = 0,
        descriptionQuery: String = ""
    ): Flow<List<TransactionWithDetails>>

    @Query("""
        SELECT
            COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amountCents ELSE 0 END), 0) as totalIncomeCents,
            COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amountCents ELSE 0 END), 0) as totalExpenseCents
        FROM transactions t
        WHERE (:type IS NULL OR t.type = :type)
          AND (:hasCategoryFilter = 0 OR t.categoryId IN (:categoryIds))
          AND (:accountId IS NULL OR t.accountId = :accountId)
          AND (:startDate IS NULL OR t.date >= :startDate)
          AND (:endDate IS NULL OR t.date <= :endDate)
          AND (:hasDescriptionFilter = 0 OR t.description LIKE '%' || :descriptionQuery || '%' COLLATE NOCASE)
    """)
    fun getFilteredSummary(
        type: TransactionType? = null,
        hasCategoryFilter: Int = 0,
        categoryIds: List<Long> = listOf(-1L),
        accountId: Long? = null,
        startDate: Long? = null,
        endDate: Long? = null,
        hasDescriptionFilter: Int = 0,
        descriptionQuery: String = ""
    ): Flow<MonthSummary>

    @Query("""
        SELECT
            COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amountCents ELSE 0 END), 0) as totalIncomeCents,
            COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amountCents ELSE 0 END), 0) as totalExpenseCents
        FROM transactions t
        WHERE (:type IS NULL OR t.type = :type)
          AND (:hasCategoryFilter = 0 OR t.categoryId IN (:categoryIds))
          AND (:accountId IS NULL OR t.accountId = :accountId)
          AND (:startDate IS NULL OR t.date >= :startDate)
          AND (:endDate IS NULL OR t.date <= :endDate)
          AND (:hasDescriptionFilter = 0 OR t.description LIKE '%' || :descriptionQuery || '%' COLLATE NOCASE)
    """)
    suspend fun getFilteredSummaryOnce(
        type: TransactionType? = null,
        hasCategoryFilter: Int = 0,
        categoryIds: List<Long> = listOf(-1L),
        accountId: Long? = null,
        startDate: Long? = null,
        endDate: Long? = null,
        hasDescriptionFilter: Int = 0,
        descriptionQuery: String = ""
    ): MonthSummary

    @Query("""
        SELECT t.id, t.accountId, a.name as accountName, t.categoryId, c.name as categoryName,
               t.type, t.amountCents, t.date, t.description, t.createdAt, t.updatedAt
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        INNER JOIN accounts a ON t.accountId = a.id
        WHERE (:type IS NULL OR t.type = :type)
          AND (:hasCategoryFilter = 0 OR t.categoryId IN (:categoryIds))
          AND (:accountId IS NULL OR t.accountId = :accountId)
          AND (:startDate IS NULL OR t.date >= :startDate)
          AND (:endDate IS NULL OR t.date <= :endDate)
          AND (:hasDescriptionFilter = 0 OR t.description LIKE '%' || :descriptionQuery || '%' COLLATE NOCASE)
        ORDER BY t.date DESC, t.createdAt DESC
    """)
    fun getFilteredTransactionsPaged(
        type: TransactionType? = null,
        hasCategoryFilter: Int = 0,
        categoryIds: List<Long> = listOf(-1L),
        accountId: Long? = null,
        startDate: Long? = null,
        endDate: Long? = null,
        hasDescriptionFilter: Int = 0,
        descriptionQuery: String = ""
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
