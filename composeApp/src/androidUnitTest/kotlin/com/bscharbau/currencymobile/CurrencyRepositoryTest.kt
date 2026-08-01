package com.bscharbau.currencymobile

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.bscharbau.currencymobile.db.AppDatabase
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Exercises CurrencyRepository against a real (in-memory) SQLite database via SQLDelight's JDBC
 * driver — the same generated schema/queries production uses, just a JVM-friendly driver instead
 * of the real AndroidSqliteDriver, which needs a real Android runtime local unit tests don't have.
 */
class CurrencyRepositoryTest {

    private fun newInMemoryDatabase(): AppDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        AppDatabase.Schema.create(driver)
        return AppDatabase(driver)
    }

    private fun apiRespondingWith(body: String): CurrencyApi {
        val mockEngine = MockEngine {
            respond(
                content = body,
                status = HttpStatusCode.OK,
                headers = Headers.build { append(HttpHeaders.ContentType, "application/json") },
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        return CurrencyApi(httpClient = client)
    }

    private fun apiThatFails(): CurrencyApi {
        val mockEngine = MockEngine {
            respond(content = "not valid json", status = HttpStatusCode.InternalServerError)
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        return CurrencyApi(httpClient = client)
    }

    @Test
    fun cachesFetchedCurrenciesAndReadsThemBack() = runTest {
        val repository = CurrencyRepository(
            database = newInMemoryDatabase(),
            api = apiRespondingWith(
                """[{"code":"AUD","name":"Australian Dollar"},{"code":"EUR","name":"Euro"}]""",
            ),
        )

        assertEquals(emptyList(), repository.loadCachedCurrencies())

        val fetched = repository.refreshCurrencies()

        assertEquals(2, fetched.size)
        assertEquals(fetched.sortedBy { it.code }, repository.loadCachedCurrencies())
    }

    @Test
    fun cachesTheMostRecentRateAndReadsItBack() = runTest {
        val repository = CurrencyRepository(
            database = newInMemoryDatabase(),
            api = apiRespondingWith(
                """{"code":"JPY","name":"Japanese Yen","date":"2026-07-31","rates":[{"to":"EUR","rate":0.00543}]}""",
            ),
        )

        assertNull(repository.loadCachedRate("JPY", "EUR"))

        repository.refreshRate("JPY", "EUR")

        val cached = repository.loadCachedRate("JPY", "EUR")
        assertNotNull(cached)
        assertEquals(0.00543, cached.rate)
        assertEquals("2026-07-31", cached.date)
    }

    @Test
    fun cachedRateSurvivesAFailedRefresh() = runTest {
        val database = newInMemoryDatabase()
        val goodRepository = CurrencyRepository(
            database = database,
            api = apiRespondingWith(
                """{"code":"JPY","name":"Japanese Yen","date":"2026-07-31","rates":[{"to":"EUR","rate":0.00543}]}""",
            ),
        )
        goodRepository.refreshRate("JPY", "EUR")

        val failingRepository = CurrencyRepository(database = database, api = apiThatFails())

        assertFailsWith<Exception> { failingRepository.refreshRate("JPY", "EUR") }

        // The earlier successful fetch is still there, even though the last refresh failed.
        val cached = failingRepository.loadCachedRate("JPY", "EUR")
        assertNotNull(cached)
        assertEquals(0.00543, cached.rate)
    }
}
