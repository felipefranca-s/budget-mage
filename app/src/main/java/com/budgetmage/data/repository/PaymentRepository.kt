package com.budgetmage.data.repository

import com.budgetmage.data.database.dao.PaymentDao
import com.budgetmage.data.database.entity.PaymentEntity
import com.budgetmage.data.database.entity.PaymentMonthStatusEntity
import com.budgetmage.data.database.entity.PaymentWithStatus
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val paymentDao: PaymentDao
) {

    fun getActivePaymentsForMonth(yearMonth: YearMonth): Flow<List<PaymentWithStatus>> {
        val encoded = encodeYearMonth(yearMonth)
        val firstDay = yearMonth.atDay(1).toEpochDay()
        val lastDay = yearMonth.atEndOfMonth().toEpochDay()
        return paymentDao.getActivePaymentsForMonth(encoded, firstDay, lastDay)
    }

    suspend fun getPaymentById(id: Long): PaymentEntity? = paymentDao.getPaymentById(id)

    suspend fun insertPayment(payment: PaymentEntity): Result<Long> = try {
        Result.success(paymentDao.insertPayment(payment))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updatePayment(payment: PaymentEntity): Result<Unit> = try {
        paymentDao.updatePayment(payment)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deletePayment(id: Long): Result<Unit> = try {
        paymentDao.deletePayment(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun markPaid(
        paymentId: Long,
        yearMonth: Int,
        transactionId: Long?,
        paidAt: Long
    ): Result<Unit> = try {
        paymentDao.insertMonthStatus(
            PaymentMonthStatusEntity(
                paymentId = paymentId,
                yearMonth = yearMonth,
                transactionId = transactionId,
                paidAt = paidAt
            )
        )
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun unmarkPaid(paymentId: Long, yearMonth: Int): Result<Unit> = try {
        paymentDao.deleteMonthStatus(paymentId, yearMonth)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    companion object {
        fun encodeYearMonth(yearMonth: YearMonth): Int =
            yearMonth.year * 100 + yearMonth.monthValue
    }
}
