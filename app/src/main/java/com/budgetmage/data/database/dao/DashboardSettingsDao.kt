package com.budgetmage.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.budgetmage.data.database.entity.DashboardSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardSettingsDao {

    @Query("SELECT * FROM dashboard_settings WHERE id = 1")
    fun getSettings(): Flow<DashboardSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: DashboardSettingsEntity)
}
