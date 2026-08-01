package com.bscharbau.currencymobile

/**
 * Placeholder conversion logic for the first version of the app: a single hardcoded JPY -> EUR
 * rate, standing in until this is wired up to the currency-calculator API's /convert endpoint.
 */
object CurrencyConverter {
    const val FROM_CURRENCY = "JPY"
    const val TO_CURRENCY = "EUR"

    // Hardcoded placeholder rate (approximate, as of this writing) — not live data.
    private const val JPY_TO_EUR_RATE = 0.0062

    fun convert(amountInJpy: Double): Double = amountInJpy * JPY_TO_EUR_RATE
}
