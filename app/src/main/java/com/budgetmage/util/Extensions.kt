package com.budgetmage.util

import java.math.BigDecimal

/**
 * Extension functions for common conversions.
 */

/**
 * Convert cents (Long) to BigDecimal representing the full amount.
 * Example: 12345L -> BigDecimal("123.45")
 */
fun Long.centsToBigDecimal(): BigDecimal {
    return BigDecimal(this).divide(BigDecimal(100))
}

/**
 * Convert BigDecimal to cents (Long).
 * Example: BigDecimal("123.45") -> 12345L
 */
fun BigDecimal.toCents(): Long {
    return this.multiply(BigDecimal(100)).toLong()
}

/**
 * Convert cents to formatted display string.
 * Example: 12345L -> "123,45"
 */
fun Long.centsToDisplayString(): String {
    val whole = this / 100
    val fraction = this % 100
    return "$whole,${fraction.toString().padStart(2, '0')}"
}

/**
 * Check if the amount in cents is valid (positive and within max limit).
 */
fun Long.isValidAmount(): Boolean {
    return this > 0 && this <= 99999999999L // Max R$ 999,999,999.99
}
