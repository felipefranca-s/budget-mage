package com.budgetmage.util

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

/**
 * Utility for formatting currency values.
 * Uses Brazilian Real (BRL) format by default.
 */
object CurrencyFormatter {

    private val brazilLocale = Locale("pt", "BR")
    private val currencyFormat = NumberFormat.getCurrencyInstance(brazilLocale)

    /**
     * Format cents to currency string.
     * Example: 12345 -> "R$ 123,45"
     */
    fun formatCents(cents: Long): String {
        val value = cents.toBigDecimal().divide(BigDecimal(100))
        return currencyFormat.format(value)
    }

    /**
     * Format BigDecimal to currency string.
     */
    fun format(value: BigDecimal): String {
        return currencyFormat.format(value)
    }

    /**
     * Parse currency string to cents.
     * Example: "123,45" -> 12345
     * Returns null if parsing fails.
     */
    fun parseToCents(input: String): Long? {
        return try {
            val cleanInput = input
                .replace("R$", "")
                .replace(".", "")
                .replace(",", ".")
                .trim()

            val value = BigDecimal(cleanInput)
            value.multiply(BigDecimal(100)).toLong()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parse decimal input string to cents.
     * Handles both comma and dot as decimal separator.
     * Example: "123.45" or "123,45" -> 12345
     */
    fun parseDecimalToCents(input: String): Long? {
        return try {
            val cleanInput = input
                .replace(",", ".")
                .trim()

            if (cleanInput.isEmpty()) return null

            val value = BigDecimal(cleanInput)
            if (value <= BigDecimal.ZERO) return null

            value.multiply(BigDecimal(100)).toLong()
        } catch (e: Exception) {
            null
        }
    }
}
