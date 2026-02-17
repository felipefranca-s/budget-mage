package com.budgetmage.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a transaction category.
 * Categories are separated by type (income vs expense).
 */
@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["type"]),
        Index(value = ["name", "type"], unique = true)
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val createdAt: Long = System.currentTimeMillis()
)
