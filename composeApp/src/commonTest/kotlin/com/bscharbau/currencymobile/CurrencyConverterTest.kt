package com.bscharbau.currencymobile

import com.bscharbau.currencymobile.CurrencyConverter.Direction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CurrencyConverterTest {
    @Test
    fun convertsJpyToEurUsingTheHardcodedRate() {
        val result = CurrencyConverter.convert(1000.0, Direction.JpyToEur)
        assertTrue(kotlin.math.abs(result - 6.2) < 0.0001, "expected ~6.2, got $result")
    }

    @Test
    fun convertsEurToJpyAsTheInverseRate() {
        val result = CurrencyConverter.convert(1.0, Direction.EurToJpy)
        val expected = 1.0 / 0.0062
        assertTrue(kotlin.math.abs(result - expected) < 0.0001, "expected ~$expected, got $result")
    }

    @Test
    fun zeroConvertsToZeroInEitherDirection() {
        assertEquals(0.0, CurrencyConverter.convert(0.0, Direction.JpyToEur))
        assertEquals(0.0, CurrencyConverter.convert(0.0, Direction.EurToJpy))
    }

    @Test
    fun swappedDirectionTogglesBackAndForth() {
        assertEquals(Direction.EurToJpy, Direction.JpyToEur.swapped())
        assertEquals(Direction.JpyToEur, Direction.EurToJpy.swapped())
    }

    @Test
    fun fromAndToCurrenciesReflectDirection() {
        assertEquals("JPY", CurrencyConverter.fromCurrency(Direction.JpyToEur))
        assertEquals("EUR", CurrencyConverter.toCurrency(Direction.JpyToEur))
        assertEquals("EUR", CurrencyConverter.fromCurrency(Direction.EurToJpy))
        assertEquals("JPY", CurrencyConverter.toCurrency(Direction.EurToJpy))
    }
}
