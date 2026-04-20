package com.budgetmage.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.budgetmage.data.database.entity.PaymentEntity
import com.budgetmage.data.database.entity.PaymentMonthStatusEntity
import com.budgetmage.data.database.entity.PaymentWithStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    @Query("""
        SELECT p.id, p.name, p.amountCents, p.startDate, p.endDate,
               p.categoryId, c.name AS categoryName,
               p.accountId, a.name AS accountName,
               p.notes,
               p.createdAt,
               CASE WHEN s.paymentId IS NOT NULL THEN 1 ELSE 0 END AS isPaid,
               s.transactionId AS transactionId,
               s.paidAt AS paidAt
        FROM payments p
        INNER JOIN categories c ON p.categoryId = c.id
        INNER JOIN accounts a ON p.accountId = a.id
        LEFT JOIN payment_month_status s
            ON s.paymentId = p.id AND s.yearMonth = :yearMonth
        WHERE p.startDate <= :lastDay
          AND (p.endDate IS NULL OR p.endDate >= :firstDay)
        ORDER BY p.name COLLATE NOCASE ASC, p.createdAt ASC
    """)
    fun getActivePaymentsForMonth(
        yearMonth: Int,
        firstDay: Long,
        lastDay: Long
    ): Flow<List<PaymentWithStatus>>

    @Query("SELECT * FROM payments WHERE id = :id")
    suspend fun getPaymentById(id: Long): PaymentEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deletePayment(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthStatus(status: PaymentMonthStatusEntity)

    @Query("DELETE FROM payment_month_status WHERE paymentId = :paymentId AND yearMonth = :yearMonth")
    suspend fun deleteMonthStatus(paymentId: Long, yearMonth: Int)
}
