package com.budgetmage.data.repository

import com.budgetmage.data.database.dao.AccountDao
import com.budgetmage.data.database.entity.AccountEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Account operations.
 */
@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao
) {

    fun getAllAccounts(): Flow<List<AccountEntity>> = accountDao.getAllAccounts()

    fun getAccountById(id: Long): Flow<AccountEntity?> = accountDao.getAccountById(id)

    suspend fun getAccountByCode(code: String): AccountEntity? = accountDao.getAccountByCode(code)

    suspend fun insert(account: AccountEntity): Result<Long> {
        return try {
            val id = accountDao.insert(account)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun update(account: AccountEntity): Result<Unit> {
        return try {
            accountDao.update(account)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun delete(account: AccountEntity): Result<Unit> {
        return try {
            accountDao.delete(account)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getTransactionCount(accountId: Long): Flow<Int> = accountDao.getTransactionCount(accountId)
}
