package com.bscharbau.currencymobile

import com.bscharbau.currencymobile.db.AppDatabase
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class CachedRate(val rate: Double, val date: String)

/**
 * Combines the network API with a local SQLDelight cache: the full currency list and the most
 * recently fetched rate per (from, to) pair are persisted.
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

    /**
     * The rate for [from] -> [to] — from the local cache without touching the network if it's
     * already from today, otherwise fetched fresh and cached. Matches the backend's own "cached
     * unless stale" philosophy (CachedExchangeRate / CachedCurrencyRates, keyed by fetch day).
     * Falls back to a stale cached value, if any, when a live fetch is needed but fails.
     */
    suspend fun rateFor(from: String, to: String): CachedRate {
        val cached = loadCachedRate(from, to)
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        if (cached != null && cached.date == today) {
            return cached
        }

        return try {
            val rates = api.fetchRates(from)
            val entry = rates.rates.firstOrNull { it.to == to }
                ?: throw NoSuchElementException("No rate found for $from → $to")
            database.rateQueries.upsert(from, to, entry.rate, rates.date)
            CachedRate(rate = entry.rate, date = rates.date)
        } catch (e: Exception) {
            cached ?: throw e
        }
    }
}
