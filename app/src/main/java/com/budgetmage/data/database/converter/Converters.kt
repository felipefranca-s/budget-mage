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

    @TypeConverter
    fun fromLongSet(set: Set<Long>): String = set.joinToString(",")

    @TypeConverter
    fun toLongSet(value: String): Set<Long> =
        if (value.isBlank()) emptySet()
        else value.split(",").mapNotNull { it.toLongOrNull() }.toSet()
}
