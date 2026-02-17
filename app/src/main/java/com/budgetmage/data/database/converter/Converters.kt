package com.budgetmage.data.database.converter

import androidx.room.TypeConverter
import com.budgetmage.data.database.entity.TransactionType

/**
 * Type converters for Room database.
 */
class Converters {

    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)
}
