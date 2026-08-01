package com.bscharbau.currencymobile

/**
 * Placeholder conversion logic for the first version of the app: a single hardcoded JPY <-> EUR
 * rate, standing in until this is wired up to the currency-calculator API's /convert endpoint.
 */
object CurrencyConverter {

    enum class Direction {
        JpyToEur,
        EurToJpy;

        fun swapped(): Direction = when (this) {
            JpyToEur -> EurToJpy
            EurToJpy -> JpyToEur
        }
    }

    // Hardcoded placeholder rate (approximate, as of this writing) — not live data. The reverse
    // direction is derived from it rather than hardcoded separately, so the pair stays consistent.
    private const val JPY_TO_EUR_RATE = 0.0062

    fun fromCurrency(direction: Direction): String = when (direction) {
        Direction.JpyToEur -> "JPY"
        Direction.EurToJpy -> "EUR"
    }

    fun toCurrency(direction: Direction): String = when (direction) {
        Direction.JpyToEur -> "EUR"
        Direction.EurToJpy -> "JPY"
    }

    fun rate(direction: Direction): Double = when (direction) {
        Direction.JpyToEur -> JPY_TO_EUR_RATE
        Direction.EurToJpy -> 1.0 / JPY_TO_EUR_RATE
    }

    fun convert(amount: Double, direction: Direction): Double = amount * rate(direction)
}
