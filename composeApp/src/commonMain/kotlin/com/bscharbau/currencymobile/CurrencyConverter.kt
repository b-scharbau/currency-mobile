package com.bscharbau.currencymobile

object CurrencyConverter {
    fun convert(amount: Double, rate: Double): Double = amount * rate
}
