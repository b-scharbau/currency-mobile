package com.bscharbau.currencymobile

import com.bscharbau.currencymobile.db.AppDatabase

data class CachedRate(val rate: Double, val date: String)

/**
 * Combines the network API with a local SQLDelight cache: the full currency list and the most
 * recently fetched rate per (from, to) pair are persisted, so the app has something to show
 * immediately on next launch and something to fall back to if a refresh fails.
 */
class CurrencyRepository(
    private val database: AppDatabase,
    private val api: CurrencyApi = CurrencyApi(),
) {
    fun loadCachedCurrencies(): List<Currency> =
        database.currencyQueries.selectAll().executeAsList().map { Currency(it.code, it.name) }

    suspend fun refreshCurrencies(): List<Currency> {
        val fetched = api.fetchCurrencies()
        database.currencyQueries.transaction {
            database.currencyQueries.deleteAll()
            fetched.forEach { database.currencyQueries.insert(it.code, it.name) }
        }
        return fetched
    }

    fun loadCachedRate(from: String, to: String): CachedRate? =
        database.rateQueries.selectRate(from, to).executeAsOneOrNull()
            ?.let { CachedRate(rate = it.rate, date = it.date) }

    suspend fun refreshRate(from: String, to: String): CurrencyRates {
        val rates = api.fetchRates(from)
        val entry = rates.rates.firstOrNull { it.to == to }
        if (entry != null) {
            database.rateQueries.upsert(from, to, entry.rate, rates.date)
        }
        return rates
    }
}
