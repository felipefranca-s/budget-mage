package com.budgetmage.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.budgetmage.data.database.dao.TransactionDao
import com.budgetmage.data.database.entity.CategoryTotal
import com.budgetmage.data.database.entity.MonthSummary
import com.budgetmage.data.database.entity.TransactionEntity
import com.budgetmage.data.database.entity.TransactionType
import com.budgetmage.data.database.entity.TransactionWithDetails
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Transaction operations.
 */
@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {

    fun getFilteredTransactions(
        type: TransactionType? = null,
        categoryIds: List<Long> = emptyList(),
        accountId: Long? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): Flow<List<TransactionWithDetails>> = transactionDao.getFilteredTransactions(
        type = type,
        hasCategoryFilter = if (categoryIds.isEmpty()) 0 else 1,
        categoryIds = categoryIds.ifEmpty { listOf(-1L) },
        accountId = accountId,
        startDate = startDate,
        endDate = endDate
    )

    fun getFilteredTransactionsPaged(
        type: TransactionType? = null,
        categoryIds: List<Long> = emptyList(),
        accountId: Long? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): Flow<PagingData<TransactionWithDetails>> = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false,
            prefetchDistance = 5
        ),
        pagingSourceFactory = {
            transactionDao.getFilteredTransactionsPaged(
                type = type,
                hasCategoryFilter = if (categoryIds.isEmpty()) 0 else 1,
                categoryIds = categoryIds.ifEmpty { listOf(-1L) },
                accountId = accountId,
                startDate = startDate,
                endDate = endDate
            )
        }
    ).flow

    fun getTransactionById(id: Long): Flow<TransactionWithDetails?> =
        transactionDao.getTransactionById(id)

    suspend fun getTransactionEntityById(id: Long): TransactionEntity? =
        transactionDao.getTransactionEntityById(id)

    suspend fun insert(transaction: TransactionEntity): Result<Long> {
        return try {
            val id = transactionDao.insert(transaction)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun update(transaction: TransactionEntity): Result<Unit> {
        return try {
            transactionDao.update(transaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun delete(transaction: TransactionEntity): Result<Unit> {
        return try {
            transactionDao.delete(transaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteById(id: Long): Result<Unit> {
        return try {
            transactionDao.deleteById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Dashboard methods

    fun getMonthSummary(yearMonth: YearMonth): Flow<MonthSummary> {
        val startDay = yearMonth.atDay(1).toEpochDay()
        val endDay = yearMonth.plusMonths(1).atDay(1).toEpochDay()
        return transactionDao.getMonthSummary(startDay, endDay)
    }

    fun getTopExpenseCategories(yearMonth: YearMonth, limit: Int = 5): Flow<List<CategoryTotal>> {
        val startDay = yearMonth.atDay(1).toEpochDay()
        val endDay = yearMonth.plusMonths(1).atDay(1).toEpochDay()
        return transactionDao.getTopExpenseCategories(startDay, endDay, limit)
    }

    fun getAllExpenseCategories(yearMonth: YearMonth): Flow<List<CategoryTotal>> {
        val startDay = yearMonth.atDay(1).toEpochDay()
        val endDay = yearMonth.plusMonths(1).atDay(1).toEpochDay()
        return transactionDao.getAllExpenseCategories(startDay, endDay)
    }
}
