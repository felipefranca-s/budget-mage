package com.budgetmage.data.repository

import com.budgetmage.data.database.dao.GoalDao
import com.budgetmage.data.database.entity.GoalEntity
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao
) {

    fun getActiveGoalsForMonth(yearMonth: YearMonth): Flow<List<GoalEntity>> {
        val firstDay = yearMonth.atDay(1).toEpochDay()
        val lastDay = yearMonth.atEndOfMonth().toEpochDay()
        return goalDao.getActiveGoalsForMonth(firstDay, lastDay)
    }

    fun getTotalTargetForMonth(yearMonth: YearMonth): Flow<Long> {
        val firstDay = yearMonth.atDay(1).toEpochDay()
        val lastDay = yearMonth.atEndOfMonth().toEpochDay()
        return goalDao.getTotalTargetForMonth(firstDay, lastDay)
    }

    suspend fun getGoalById(id: Long): GoalEntity? = goalDao.getGoalById(id)

    suspend fun insertGoal(goal: GoalEntity): Result<Long> = try {
        Result.success(goalDao.insertGoal(goal))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateGoal(goal: GoalEntity): Result<Unit> = try {
        goalDao.updateGoal(goal)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteGoal(id: Long): Result<Unit> = try {
        goalDao.deleteGoal(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
