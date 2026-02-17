package com.budgetmage.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Utility for formatting dates.
 * Uses Brazilian Portuguese locale by default.
 */
object DateFormatter {

    private val brazilLocale = Locale("pt", "BR")

    private val shortDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        .withLocale(brazilLocale)

    private val mediumDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(brazilLocale)

    private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", brazilLocale)

    private val dayMonthFormatter = DateTimeFormatter.ofPattern("dd/MM", brazilLocale)

    /**
     * Format LocalDate to short date string.
     * Example: "20/01/25"
     */
    fun formatShort(date: LocalDate): String {
        return shortDateFormatter.format(date)
    }

    /**
     * Format LocalDate to medium date string.
     * Example: "20 de jan. de 2025"
     */
    fun formatMedium(date: LocalDate): String {
        return mediumDateFormatter.format(date)
    }

    /**
     * Format LocalDate to day/month string.
     * Example: "20/01"
     */
    fun formatDayMonth(date: LocalDate): String {
        return dayMonthFormatter.format(date)
    }

    /**
     * Format YearMonth to month and year string.
     * Example: "janeiro 2025"
     */
    fun formatMonthYear(yearMonth: YearMonth): String {
        return monthYearFormatter.format(yearMonth).replaceFirstChar { it.uppercase() }
    }

    /**
     * Format epoch day to short date string.
     */
    fun formatEpochDay(epochDay: Long): String {
        return formatShort(LocalDate.ofEpochDay(epochDay))
    }

    /**
     * Format epoch day to medium date string.
     */
    fun formatEpochDayMedium(epochDay: Long): String {
        return formatMedium(LocalDate.ofEpochDay(epochDay))
    }

    /**
     * Get today's date as epoch day.
     */
    fun todayEpochDay(): Long {
        return LocalDate.now().toEpochDay()
    }

    /**
     * Convert epoch day to LocalDate.
     */
    fun epochDayToLocalDate(epochDay: Long): LocalDate {
        return LocalDate.ofEpochDay(epochDay)
    }

    /**
     * Convert LocalDate to epoch day.
     */
    fun localDateToEpochDay(date: LocalDate): Long {
        return date.toEpochDay()
    }
}
