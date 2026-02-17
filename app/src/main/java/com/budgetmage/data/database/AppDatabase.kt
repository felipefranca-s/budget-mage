package com.budgetmage.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.budgetmage.data.database.converter.Converters
import com.budgetmage.data.database.dao.AccountDao
import com.budgetmage.data.database.dao.CategoryDao
import com.budgetmage.data.database.dao.TransactionDao
import com.budgetmage.data.database.entity.AccountEntity
import com.budgetmage.data.database.entity.CategoryEntity
import com.budgetmage.data.database.entity.TransactionEntity
import com.budgetmage.data.database.entity.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        private const val DATABASE_NAME = "budget_mage.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

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
