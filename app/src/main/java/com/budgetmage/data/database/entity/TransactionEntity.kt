package com.budgetmage.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a financial transaction.
 * Amount is stored in cents to avoid floating-point precision issues.
 * Date is stored as epoch day (days since 1970-01-01).
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("date"),
        Index("type"),
        Index("categoryId"),
        Index("accountId")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val categoryId: Long,
    val type: TransactionType,
    val amountCents: Long,
    val date: Long, // Epoch day
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Data class for transaction with joined category and account names.
 */
data class TransactionWithDetails(
    val id: Long,
    val accountId: Long,
    val accountName: String,
    val categoryId: Long,
    val categoryName: String,
    val type: TransactionType,
    val amountCents: Long,
    val date: Long,
    val description: String?,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * Data class for monthly summary aggregation.
 */
data class MonthSummary(
    val totalIncomeCents: Long,
    val totalExpenseCents: Long
) {
    val balanceCents: Long get() = totalIncomeCents - totalExpenseCents
}

/**
 * Data class for category total aggregation.
 */
data class CategoryTotal(
    val categoryId: Long,
    val categoryName: String,
    val totalCents: Long
)
