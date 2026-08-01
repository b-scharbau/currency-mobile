package com.bscharbau.currencymobile

object CurrencyConverter {

    enum class Direction {
        JpyToEur,
        EurToJpy;

        fun swapped(): Direction = when (this) {
            JpyToEur -> EurToJpy
            EurToJpy -> JpyToEur
        }
    }

    fun fromCurrency(direction: Direction): String = when (direction) {
        Direction.JpyToEur -> "JPY"
        Direction.EurToJpy -> "EUR"
    }

    fun toCurrency(direction: Direction): String = when (direction) {
        Direction.JpyToEur -> "EUR"
        Direction.EurToJpy -> "JPY"
    }

    fun convert(amount: Double, rate: Double): Double = amount * rate
}
