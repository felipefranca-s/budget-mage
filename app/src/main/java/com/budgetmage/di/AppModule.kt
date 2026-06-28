package com.budgetmage.di

import android.content.Context
import com.budgetmage.data.database.AppDatabase
import com.budgetmage.data.database.dao.AccountDao
import com.budgetmage.data.database.dao.CategoryDao
import com.budgetmage.data.database.dao.DashboardSettingsDao
import com.budgetmage.data.database.dao.GoalDao
import com.budgetmage.data.database.dao.PaymentDao
import com.budgetmage.data.database.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideAccountDao(database: AppDatabase): AccountDao {
        return database.accountDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun providePaymentDao(database: AppDatabase): PaymentDao {
        return database.paymentDao()
    }

    @Provides
    @Singleton
    fun provideGoalDao(database: AppDatabase): GoalDao {
        return database.goalDao()
    }

    @Provides
    @Singleton
    fun provideDashboardSettingsDao(database: AppDatabase): DashboardSettingsDao {
        return database.dashboardSettingsDao()
    }
}
