package com.budgetmage.data.repository

import com.budgetmage.data.database.dao.CategoryDao
import com.budgetmage.data.database.entity.CategoryEntity
import com.budgetmage.data.database.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Category operations.
 */
@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {

    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    fun getCategoriesByType(type: TransactionType): Flow<List<CategoryEntity>> =
        categoryDao.getCategoriesByType(type)

    fun getCategoryById(id: Long): Flow<CategoryEntity?> = categoryDao.getCategoryById(id)

    suspend fun getCategoryByNameAndType(name: String, type: TransactionType): CategoryEntity? =
        categoryDao.getCategoryByNameAndType(name, type)

    suspend fun insert(category: CategoryEntity): Result<Long> {
        return try {
            val id = categoryDao.insert(category)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun update(category: CategoryEntity): Result<Unit> {
        return try {
            categoryDao.update(category)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun delete(category: CategoryEntity): Result<Unit> {
        return try {
            categoryDao.delete(category)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getTransactionCount(categoryId: Long): Flow<Int> = categoryDao.getTransactionCount(categoryId)
}
