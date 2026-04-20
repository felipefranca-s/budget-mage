package com.budgetmage.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("categoryId"),
        Index("accountId"),
        Index("startDate"),
        Index("endDate")
    ]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amountCents: Long,
    val startDate: Long,
    val endDate: Long? = null,
    val categoryId: Long,
    val accountId: Long,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class PaymentWithStatus(
    val id: Long,
    val name: String,
    val amountCents: Long,
    val startDate: Long,
    val endDate: Long?,
    val categoryId: Long,
    val categoryName: String,
    val accountId: Long,
    val accountName: String,
    val notes: String?,
    val createdAt: Long,
    val isPaid: Int,
    val transactionId: Long?,
    val paidAt: Long?
) {
    val paid: Boolean get() = isPaid == 1
}
