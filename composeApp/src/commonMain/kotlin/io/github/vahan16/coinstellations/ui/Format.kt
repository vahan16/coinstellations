package io.github.vahan16.coinstellations.ui

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Pure-Kotlin number formatting — no java.text / String.format, so it works
 * identically on Android, iOS, Desktop and Wasm.
 */

private fun Double.toFixed(digits: Int): String {
    if (isNaN()) return "0"
    if (isInfinite()) return if (this > 0) "∞" else "-∞"
    var factor = 1L
    repeat(digits) { factor *= 10 }
    val scaled = (abs(this) * factor).roundToLong()
    val intPart = scaled / factor
    val frac = scaled % factor
    val sb = StringBuilder()
    if (this < 0 && (intPart != 0L || frac != 0L)) sb.append('-')
    sb.append(intPart.toString())
    if (digits > 0) {
        sb.append('.')
        sb.append(frac.toString().padStart(digits, '0'))
    }
    return sb.toString()
}

private fun groupThousands(intStr: String): String {
    val neg = intStr.startsWith("-")
    val digits = if (neg) intStr.substring(1) else intStr
    val sb = StringBuilder()
    val n = digits.length
    for (i in 0 until n) {
        if (i > 0 && (n - i) % 3 == 0) sb.append(',')
        sb.append(digits[i])
    }
    return (if (neg) "-" else "") + sb.toString()
}

/** $1,234.56 — precision adapts so sub-cent coins still show meaningful digits. */
fun formatPrice(value: Double?): String {
    if (value == null || value.isNaN()) return "—"
    val a = abs(value)
    val digits = when {
        a >= 1.0 -> 2
        a >= 0.01 -> 4
        a >= 0.0001 -> 6
        a > 0.0 -> 8
        else -> 2
    }
    val fixed = value.toFixed(digits)
    val intPart = fixed.substringBefore('.')
    val frac = if ('.' in fixed) fixed.substringAfter('.') else ""
    return "$" + groupThousands(intPart) + if (frac.isNotEmpty()) ".$frac" else ""
}

/** $2.41T / $812.5M compact money. */
fun formatCompactUsd(value: Double?): String {
    if (value == null || value.isNaN()) return "—"
    val a = abs(value)
    val sign = if (value < 0) "-" else ""
    return sign + "$" + when {
        a >= 1e12 -> (a / 1e12).toFixed(2) + "T"
        a >= 1e9 -> (a / 1e9).toFixed(2) + "B"
        a >= 1e6 -> (a / 1e6).toFixed(2) + "M"
        a >= 1e3 -> (a / 1e3).toFixed(2) + "K"
        else -> a.toFixed(2)
    }
}

/** 19.7M / 1.2B plain compact count (e.g. circulating supply). */
fun formatCompact(value: Double?): String {
    if (value == null || value.isNaN()) return "—"
    val a = abs(value)
    val sign = if (value < 0) "-" else ""
    return sign + when {
        a >= 1e12 -> (a / 1e12).toFixed(2) + "T"
        a >= 1e9 -> (a / 1e9).toFixed(2) + "B"
        a >= 1e6 -> (a / 1e6).toFixed(2) + "M"
        a >= 1e3 -> (a / 1e3).toFixed(2) + "K"
        else -> a.toFixed(0)
    }
}

/** +1.23% / -4.50% with explicit sign. */
fun formatPercent(value: Double?): String {
    if (value == null || value.isNaN()) return "—"
    val sign = if (value >= 0) "+" else ""
    return sign + value.toFixed(2) + "%"
}
