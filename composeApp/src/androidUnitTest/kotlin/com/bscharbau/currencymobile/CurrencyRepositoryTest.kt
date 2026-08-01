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
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

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

    private fun apiThatTracksCalls(onCalled: () -> Unit): CurrencyApi {
        val mockEngine = MockEngine {
            onCalled()
            respond(
                content = """{"code":"JPY","name":"Japanese Yen","date":"irrelevant","rates":[]}""",
                status = HttpStatusCode.OK,
                headers = Headers.build { append(HttpHeaders.ContentType, "application/json") },
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        return CurrencyApi(httpClient = client)
    }

    private fun today(): String = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

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
    fun fetchesAndCachesWhenNothingIsCachedYet() = runTest {
        val repository = CurrencyRepository(
            database = newInMemoryDatabase(),
            api = apiRespondingWith(
                """{"code":"JPY","name":"Japanese Yen","date":"2020-01-01","rates":[{"to":"EUR","rate":0.00543}]}""",
            ),
        )

        assertNull(repository.loadCachedRate("JPY", "EUR"))

        val result = repository.rateFor("JPY", "EUR")

        assertEquals(0.00543, result.rate)
        assertEquals("2020-01-01", result.date)
        assertEquals(result, repository.loadCachedRate("JPY", "EUR"))
    }

    @Test
    fun usesTodaysCachedRateWithoutTouchingTheNetwork() = runTest {
        val database = newInMemoryDatabase()
        database.rateQueries.upsert("JPY", "EUR", 0.0062, today())

        var networkCalled = false
        val repository = CurrencyRepository(database = database, api = apiThatTracksCalls { networkCalled = true })

        val result = repository.rateFor("JPY", "EUR")

        assertEquals(0.0062, result.rate)
        assertEquals(today(), result.date)
        assertFalse(networkCalled, "should not have called the API when the cached rate is from today")
    }

    @Test
    fun refetchesWhenTheCachedRateIsFromAnEarlierDay() = runTest {
        val database = newInMemoryDatabase()
        database.rateQueries.upsert("JPY", "EUR", 0.0062, "2020-01-01")

        var networkCalled = false
        val repository = CurrencyRepository(
            database = database,
            api = apiRespondingWith(
                """{"code":"JPY","name":"Japanese Yen","date":"${today()}","rates":[{"to":"EUR","rate":0.00543}]}""",
            ),
        )

        val result = repository.rateFor("JPY", "EUR")

        assertEquals(0.00543, result.rate)
        assertEquals(today(), result.date)
        assertEquals(result, repository.loadCachedRate("JPY", "EUR"))
    }

    @Test
    fun fallsBackToAStaleCachedRateWhenAFreshFetchFails() = runTest {
        val database = newInMemoryDatabase()
        database.rateQueries.upsert("JPY", "EUR", 0.0062, "2020-01-01")

        val repository = CurrencyRepository(database = database, api = apiThatFails())

        val result = repository.rateFor("JPY", "EUR")

        assertEquals(0.0062, result.rate)
        assertEquals("2020-01-01", result.date)
    }

    @Test
    fun throwsWhenThereIsNoCacheAndTheFetchFails() = runTest {
        val repository = CurrencyRepository(database = newInMemoryDatabase(), api = apiThatFails())

        assertFailsWith<Exception> { repository.rateFor("JPY", "EUR") }
    }

    @Test
    fun sanityCheckTodayHelperProducesAPlausibleIsoDate() {
        assertTrue(today().matches(Regex("""\d{4}-\d{2}-\d{2}""")))
    }
}
