package com.budgetmage.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dashboard_settings")
data class DashboardSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val excludedAccountIds: Set<Long> = emptySet()
)
