package com.bscharbau.currencymobile

import androidx.compose.ui.text.AnnotatedString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AmountFormattingTest {
    @Test
    fun parsesAPeriodDecimalPoint() {
        assertEquals(1234.5, parseAmount("1234.5"))
    }

    @Test
    fun parsesACommaAsADecimalPointToo() {
        // Android's decimal keyboard commonly offers both '.' and ',' regardless of locale.
        assertEquals(1234.5, parseAmount("1234,5"))
    }

    @Test
    fun rejectsUnparsableText() {
        assertNull(parseAmount("not a number"))
    }

    @Test
    fun groupsAnIntegerIntoThreeDigitChunks() {
        val transformation = ThousandsVisualTransformation(decimalSeparator = '.', groupingSeparator = ',')
        val result = transformation.filter(AnnotatedString("1234567"))
        assertEquals("1,234,567", result.text.text)
    }

    @Test
    fun leavesShortIntegersUngrouped() {
        val transformation = ThousandsVisualTransformation(decimalSeparator = '.', groupingSeparator = ',')
        val result = transformation.filter(AnnotatedString("42"))
        assertEquals("42", result.text.text)
    }

    @Test
    fun groupsTheIntegerPartAndShowsTheLocaleDecimalGlyph() {
        // German conventions: '.' groups, ',' is the decimal point — the opposite of en-US.
        val transformation = ThousandsVisualTransformation(decimalSeparator = ',', groupingSeparator = '.')
        val result = transformation.filter(AnnotatedString("1234567.89"))
        assertEquals("1.234.567,89", result.text.text)
    }

    @Test
    fun mapsOriginalOffsetsToTransformedOffsetsAroundInsertedSeparators() {
        val transformation = ThousandsVisualTransformation(decimalSeparator = '.', groupingSeparator = ',')
        val mapping = transformation.filter(AnnotatedString("1234567")).offsetMapping

        // "1234567" -> "1,234,567": one separator inserted before index 1, another before index 4.
        assertEquals(0, mapping.originalToTransformed(0))
        assertEquals(2, mapping.originalToTransformed(1))
        assertEquals(6, mapping.originalToTransformed(4))
        assertEquals(9, mapping.originalToTransformed(7))
    }

    @Test
    fun mapsTransformedOffsetsBackToOriginalOffsets() {
        val transformation = ThousandsVisualTransformation(decimalSeparator = '.', groupingSeparator = ',')
        val mapping = transformation.filter(AnnotatedString("1234567")).offsetMapping

        // "1,234,567" -> "1234567"
        assertEquals(0, mapping.transformedToOriginal(0))
        assertEquals(1, mapping.transformedToOriginal(2))
        assertEquals(4, mapping.transformedToOriginal(6))
        assertEquals(7, mapping.transformedToOriginal(9))
    }

    @Test
    fun mapsOffsetsCorrectlyAroundTheDecimalPoint() {
        val transformation = ThousandsVisualTransformation(decimalSeparator = '.', groupingSeparator = ',')
        val filtered = transformation.filter(AnnotatedString("1234.56"))
        assertEquals("1,234.56", filtered.text.text)

        val mapping = filtered.offsetMapping
        assertEquals(5, mapping.originalToTransformed(4)) // right before '.'
        assertEquals(8, mapping.originalToTransformed(7)) // end of string
        assertEquals(4, mapping.transformedToOriginal(5))
        assertEquals(7, mapping.transformedToOriginal(8))
    }
}
