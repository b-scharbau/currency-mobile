package com.bscharbau.currencymobile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CurrencyConverterTest {
    @Test
    fun convertsJpyToEurUsingTheHardcodedRate() {
        val result = CurrencyConverter.convert(1000.0)
        assertTrue(kotlin.math.abs(result - 6.2) < 0.0001, "expected ~6.2, got $result")
    }

    @Test
    fun zeroConvertsToZero() {
        assertEquals(0.0, CurrencyConverter.convert(0.0))
    }
}
