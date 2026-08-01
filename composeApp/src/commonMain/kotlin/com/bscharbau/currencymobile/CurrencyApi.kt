package com.bscharbau.currencymobile

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class RateEntry(val to: String, val rate: Double)

@Serializable
data class CurrencyRates(val code: String, val name: String, val date: String, val rates: List<RateEntry>)

/**
 * Talks to the currency-calculator backend (currency.bscharbau.com) — specifically the
 * `/currency?code=` endpoint, which returns one base currency's rates against every other
 * supported currency. Mirrors the web frontend's approach: fetch once per "from" currency, then
 * derive any conversion locally from the returned rates.
 */
class CurrencyApi(
    private val baseUrl: String = "https://currency.bscharbau.com",
    private val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    },
) {
    suspend fun fetchRates(code: String): CurrencyRates =
        httpClient.get("$baseUrl/currency") { parameter("code", code) }.body()
}
