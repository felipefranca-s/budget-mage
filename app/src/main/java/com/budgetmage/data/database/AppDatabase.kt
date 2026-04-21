package com.budgetmage.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.budgetmage.data.database.converter.Converters
import com.budgetmage.data.database.dao.AccountDao
import com.budgetmage.data.database.dao.CategoryDao
import com.budgetmage.data.database.dao.GoalDao
import com.budgetmage.data.database.dao.PaymentDao
import com.budgetmage.data.database.dao.TransactionDao
import com.budgetmage.data.database.entity.AccountEntity
import com.budgetmage.data.database.entity.CategoryEntity
import com.budgetmage.data.database.entity.GoalEntity
import com.budgetmage.data.database.entity.PaymentEntity
import com.budgetmage.data.database.entity.PaymentMonthStatusEntity
import com.budgetmage.data.database.entity.TransactionEntity
import com.budgetmage.data.database.entity.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        PaymentEntity::class,
        PaymentMonthStatusEntity::class,
        GoalEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun paymentDao(): PaymentDao
    abstract fun goalDao(): GoalDao

    companion object {
        private const val DATABASE_NAME = "budget_mage.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS payments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        amountCents INTEGER NOT NULL,
                        startDate INTEGER NOT NULL,
                        endDate INTEGER,
                        categoryId INTEGER NOT NULL,
                        accountId INTEGER NOT NULL,
                        notes TEXT,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(categoryId) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE RESTRICT,
                        FOREIGN KEY(accountId) REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_categoryId ON payments(categoryId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_accountId ON payments(accountId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_startDate ON payments(startDate)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_endDate ON payments(endDate)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS payment_month_status (
                        paymentId INTEGER NOT NULL,
                        yearMonth INTEGER NOT NULL,
                        transactionId INTEGER,
                        paidAt INTEGER NOT NULL,
                        PRIMARY KEY(paymentId, yearMonth),
                        FOREIGN KEY(paymentId) REFERENCES payments(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(transactionId) REFERENCES transactions(id) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payment_month_status_paymentId ON payment_month_status(paymentId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payment_month_status_yearMonth ON payment_month_status(yearMonth)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payment_month_status_transactionId ON payment_month_status(transactionId)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE payments ADD COLUMN notes TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS goals (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        amountCents INTEGER NOT NULL,
                        startDate INTEGER NOT NULL,
                        endDate INTEGER,
                        notes TEXT,
                        priority INTEGER NOT NULL DEFAULT 5,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_goals_startDate ON goals(startDate)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_goals_endDate ON goals(endDate)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_goals_priority ON goals(priority)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .addCallback(PrepopulateCallback())
                .build()
        }
    }

    private class PrepopulateCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            // Insert default account
            db.execSQL("""
                INSERT INTO accounts (code, name, createdAt)
                VALUES ('CASH', 'Dinheiro', ${System.currentTimeMillis()})
            """)

            // Insert default expense categories
            val expenseCategories = listOf(
                "Alimentação",
                "Transporte",
                "Moradia",
                "Utilidades",
                "Saúde",
                "Lazer",
                "Educação",
                "Outros"
            )

            expenseCategories.forEach { name ->
                db.execSQL("""
                    INSERT INTO categories (name, type, createdAt)
                    VALUES ('$name', '${TransactionType.EXPENSE.name}', ${System.currentTimeMillis()})
                """)
            }

            // Insert default income categories
            val incomeCategories = listOf(
                "Salário",
                "Investimentos",
                "Freelance",
                "Outros"
            )

            incomeCategories.forEach { name ->
                db.execSQL("""
                    INSERT INTO categories (name, type, createdAt)
                    VALUES ('$name', '${TransactionType.INCOME.name}', ${System.currentTimeMillis()})
                """)
            }
        }
    }
}
