package com.budgetmage.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.budgetmage.data.database.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Query("""
        SELECT * FROM goals
        WHERE startDate <= :lastDay
          AND (endDate IS NULL OR endDate >= :firstDay)
        ORDER BY priority DESC, createdAt ASC, id ASC
    """)
    fun getActiveGoalsForMonth(firstDay: Long, lastDay: Long): Flow<List<GoalEntity>>

    @Query("""
        SELECT COALESCE(SUM(amountCents), 0) FROM goals
        WHERE startDate <= :lastDay
          AND (endDate IS NULL OR endDate >= :firstDay)
    """)
    fun getTotalTargetForMonth(firstDay: Long, lastDay: Long): Flow<Long>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Long): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoal(id: Long)
}
