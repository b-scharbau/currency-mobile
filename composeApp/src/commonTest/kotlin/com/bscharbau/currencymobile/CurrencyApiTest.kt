package com.bscharbau.currencymobile

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

class CurrencyApiTest {
    // A trimmed real response from GET /currency?code=JPY (currency.bscharbau.com).
    private val sampleJpyResponse = """
        {"code":"JPY","name":"Japanese Yen","date":"2026-07-31","rates":[
            {"to":"AUD","rate":0.00889},
            {"to":"EUR","rate":0.00543},
            {"to":"USD","rate":0.0067}
        ]}
    """.trimIndent()

    private fun apiWithMockResponse(body: String, status: HttpStatusCode = HttpStatusCode.OK): CurrencyApi {
        val mockEngine = MockEngine { _ ->
            respond(
                content = body,
                status = status,
                headers = Headers.build { append(HttpHeaders.ContentType, "application/json") },
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        return CurrencyApi(httpClient = client)
    }

    @Test
    fun parsesRatesFromTheRealResponseShape() = runTest {
        val api = apiWithMockResponse(sampleJpyResponse)

        val result = api.fetchRates("JPY")

        assertEquals("JPY", result.code)
        assertEquals("Japanese Yen", result.name)
        assertEquals("2026-07-31", result.date)
        assertEquals(0.00543, result.rates.first { it.to == "EUR" }.rate)
    }

    @Test
    fun ignoresUnknownFieldsInTheResponse() = runTest {
        val api = apiWithMockResponse(
            """{"code":"JPY","name":"Japanese Yen","date":"2026-07-31","rates":[],"extraField":"ignored"}""",
        )

        val result = api.fetchRates("JPY")

        assertEquals("JPY", result.code)
    }
}
