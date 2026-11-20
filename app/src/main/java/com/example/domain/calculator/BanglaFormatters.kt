package com.example.domain.calculator

import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone
import kotlin.math.roundToInt

/**
 * API-24 safe immutable calendar date helper using UTC Gregorian calendar day arithmetic.
 */
data class BanglaDate(
    val year: Int,
    val month: Int, // 1..12
    val day: Int    // 1..31
) : Comparable<BanglaDate> {

    fun toEpochDay(): Long {
        val cal = GregorianCalendar(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(year, month - 1, day, 0, 0, 0)
        }
        return Math.floorDiv(cal.timeInMillis, MILLIS_PER_DAY)
    }

    fun toUtcMillis(): Long = toEpochDay() * MILLIS_PER_DAY

    fun plusDays(days: Int): BanglaDate = fromEpochDay(toEpochDay() + days)

    fun minusDays(days: Int): BanglaDate = fromEpochDay(toEpochDay() - days)

    fun formatBangla(): String {
        val monthName = BANGLA_MONTHS.getOrElse(month - 1) { "" }
        return "${BanglaFormatters.toBanglaDigits(day)} $monthName ${BanglaFormatters.toBanglaDigits(year)}"
    }

    fun formatShortBangla(): String {
        val monthName = BANGLA_MONTHS_SHORT.getOrElse(month - 1) { "" }
        return "${BanglaFormatters.toBanglaDigits(day)} $monthName"
    }

    override fun compareTo(other: BanglaDate): Int =
        compareValuesBy(this, other, { it.year }, { it.month }, { it.day })

    companion object {
        private const val MILLIS_PER_DAY = 86_400_000L

        private val BANGLA_MONTHS = listOf(
            "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
            "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
        )

        private val BANGLA_MONTHS_SHORT = listOf(
            "জানু", "ফেব্রু", "মার্চ", "এপ্রি", "মে", "জুন",
            "জুলা", "আগ", "সেপ্টে", "অক্টো", "নভে", "ডিসে"
        )

        fun fromEpochDay(epochDay: Long): BanglaDate {
            val cal = GregorianCalendar(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = epochDay * MILLIS_PER_DAY
            }
            return BanglaDate(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH) + 1,
                day = cal.get(Calendar.DAY_OF_MONTH)
            )
        }

        fun fromUtcMillis(utcMillis: Long): BanglaDate {
            val cal = GregorianCalendar(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = utcMillis
            }
            return BanglaDate(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH) + 1,
                day = cal.get(Calendar.DAY_OF_MONTH)
            )
        }

        fun today(): BanglaDate {
            val cal = Calendar.getInstance()
            return BanglaDate(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH) + 1,
                day = cal.get(Calendar.DAY_OF_MONTH)
            )
        }
    }
}

object BanglaFormatters {
    private val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun toBanglaDigits(number: Int): String = toBanglaDigits(number.toString())

    fun toBanglaDigits(number: Long): String = toBanglaDigits(number.toString())

    fun toBanglaDigits(raw: String): String = buildString(raw.length) {
        for (ch in raw) {
            if (ch in '0'..'9') {
                append(bnDigits[ch - '0'])
            } else {
                append(ch)
            }
        }
    }

    fun formatDecimal(value: Double, decimals: Int = 2): String {
        val factor = when (decimals) {
            0 -> 1.0
            1 -> 10.0
            2 -> 100.0
            else -> 1000.0
        }
        val rounded = (value * factor).roundToInt() / factor
        val text = if (decimals == 0) {
            rounded.roundToInt().toString()
        } else {
            val formatted = "%.${decimals}f".format(java.util.Locale.US, rounded)
            formatted.trimEnd('0').trimEnd('.')
        }
        return toBanglaDigits(text)
    }

    fun formatKgOrGram(kg: Double): String {
        if (kg <= 0.001) return "০ কেজি"
        return if (kg < 1.0) {
            val grams = (kg * 1000.0).roundToInt()
            "${toBanglaDigits(grams)} গ্রাম"
        } else {
            "${formatDecimal(kg, 2)} কেজি"
        }
    }

    fun parseNumber(input: String): Double? {
        if (input.isBlank()) return null
        val normalized = buildString(input.length) {
            for (ch in input.trim()) {
                val bnIdx = bnDigits.indexOf(ch)
                when {
                    bnIdx >= 0 -> append(('0'.code + bnIdx).toChar())
                    ch == '।' -> append('.')
                    else -> append(ch)
                }
            }
        }
        return normalized.toDoubleOrNull()
    }
}
