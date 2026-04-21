package com.budgetmage.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "goals",
    indices = [
        Index("startDate"),
        Index("endDate"),
        Index("priority")
    ]
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amountCents: Long,
    val startDate: Long,
    val endDate: Long? = null,
    val notes: String? = null,
    val priority: Int = 5,
    val createdAt: Long = System.currentTimeMillis()
)

data class GoalWithProgress(
    val id: Long,
    val name: String,
    val amountCents: Long,
    val startDate: Long,
    val endDate: Long?,
    val notes: String?,
    val priority: Int,
    val createdAt: Long,
    val achievedCents: Long
)
