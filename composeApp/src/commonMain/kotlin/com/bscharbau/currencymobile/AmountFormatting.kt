package com.bscharbau.currencymobile

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

// The device's decimal and thousands-grouping separator characters (e.g. '.'/',' in en-US,
// ','/'.' in de-DE) — used to display the amount field grouped and with the locale-correct
// decimal glyph, matching the conventions every other numeric field on the user's device follows.
expect fun decimalSeparator(): Char
expect fun groupingSeparator(): Char

// Android's decimal numeric keyboard commonly offers both '.' and ',' as candidate keys
// regardless of the device's actual locale (confirmed on an AOSP keyboard) — parsing accepts
// either as "the" decimal point rather than only the one decimalSeparator() reports, since
// requiring an exact match would reject perfectly reasonable input on a device whose keyboard
// and locale don't happen to agree.
fun parseAmount(text: String): Double? = text.replace(',', '.').toDoubleOrNull()

/**
 * Displays the amount field's raw value with thousands grouping inserted into the integer part,
 * and its decimal marker (if any — either '.' or ',', see parseAmount) shown as the locale's own
 * decimal glyph — without changing what's actually stored as the field's value, so parsing stays
 * simple and never has to deal with grouping separators.
 */
class ThousandsVisualTransformation(
    private val decimalSeparator: Char,
    private val groupingSeparator: Char,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val decimalIndex = raw.indexOfFirst { it == '.' || it == ',' }
        val integerLength = if (decimalIndex >= 0) decimalIndex else raw.length
        val groupedInteger = groupDigits(raw.substring(0, integerLength), groupingSeparator)
        val fraction = if (decimalIndex >= 0) raw.substring(decimalIndex + 1) else null
        val transformed = if (fraction != null) "$groupedInteger$decimalSeparator$fraction" else groupedInteger

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, raw.length)
                return if (decimalIndex >= 0 && clamped > decimalIndex) {
                    groupedInteger.length + 1 + (clamped - decimalIndex - 1)
                } else {
                    integerOffsetToGrouped(clamped.coerceAtMost(integerLength), integerLength)
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, transformed.length)
                return if (decimalIndex >= 0 && clamped > groupedInteger.length) {
                    decimalIndex + 1 + (clamped - groupedInteger.length - 1)
                } else {
                    groupedOffsetToInteger(clamped.coerceAtMost(groupedInteger.length), groupedInteger)
                }
            }
        }

        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }

    private fun groupDigits(digits: String, groupingSeparator: Char): String {
        val n = digits.length
        return buildString {
            for (i in digits.indices) {
                if (i > 0 && (n - i) % 3 == 0) append(groupingSeparator)
                append(digits[i])
            }
        }
    }

    private fun integerOffsetToGrouped(offset: Int, integerLength: Int): Int {
        var separators = 0
        // Separators only sit strictly between digits (gap i for 1 <= i < integerLength) — the
        // boundary right after the last digit (i == integerLength) is never one, even though the
        // grouping arithmetic below would otherwise satisfy the same divisibility check there.
        for (i in 1 until integerLength) {
            if (i > offset) break
            if ((integerLength - i) % 3 == 0) separators++
        }
        return offset + separators
    }

    private fun groupedOffsetToInteger(offset: Int, grouped: String): Int {
        var count = 0
        for (i in 0 until offset) {
            if (grouped[i] != groupingSeparator) count++
        }
        return count
    }
}
