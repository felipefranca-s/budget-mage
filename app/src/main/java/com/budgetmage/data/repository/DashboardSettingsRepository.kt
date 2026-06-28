package com.budgetmage.data.repository

import com.budgetmage.data.database.dao.DashboardSettingsDao
import com.budgetmage.data.database.entity.DashboardSettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardSettingsRepository @Inject constructor(
    private val dao: DashboardSettingsDao
) {

    fun getExcludedAccountIds(): Flow<Set<Long>> =
        dao.getSettings().map { it?.excludedAccountIds ?: emptySet() }

    suspend fun setExcludedAccountIds(excludedIds: Set<Long>) {
        dao.upsert(DashboardSettingsEntity(excludedAccountIds = excludedIds))
    }
}
