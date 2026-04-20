package com.budgetmage.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "payment_month_status",
    primaryKeys = ["paymentId", "yearMonth"],
    foreignKeys = [
        ForeignKey(
            entity = PaymentEntity::class,
            parentColumns = ["id"],
            childColumns = ["paymentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("paymentId"),
        Index("yearMonth"),
        Index("transactionId")
    ]
)
data class PaymentMonthStatusEntity(
    val paymentId: Long,
    val yearMonth: Int,
    val transactionId: Long? = null,
    val paidAt: Long
)
