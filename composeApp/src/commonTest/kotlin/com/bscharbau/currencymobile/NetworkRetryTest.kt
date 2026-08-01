package com.bscharbau.currencymobile

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NetworkRetryTest {
    @Test
    fun succeedsWithoutRetryingWhenTheBlockSucceedsFirstTry() = runTest {
        var calls = 0

        val result = NetworkRetry.execute(times = 3, initialDelayMs = 1) {
            calls++
            "ok"
        }

        assertEquals("ok", result)
        assertEquals(1, calls)
    }

    @Test
    fun retriesTransientFailuresAndEventuallySucceeds() = runTest {
        var calls = 0

        val result = NetworkRetry.execute(times = 3, initialDelayMs = 1) {
            calls++
            if (calls < 3) throw RuntimeException("transient failure")
            "ok"
        }

        assertEquals("ok", result)
        assertEquals(3, calls)
    }

    @Test
    fun givesUpAfterExhaustingAllAttempts() = runTest {
        var calls = 0

        assertFailsWith<RuntimeException> {
            NetworkRetry.execute(times = 3, initialDelayMs = 1) {
                calls++
                throw RuntimeException("always fails")
            }
        }

        assertEquals(3, calls)
    }

    @Test
    fun neverRetriesAClientRequestException() = runTest {
        var calls = 0
        val mockEngine = MockEngine { respond("bad request", HttpStatusCode.BadRequest) }
        val client = HttpClient(mockEngine) { expectSuccess = true }

        assertFailsWith<ClientRequestException> {
            NetworkRetry.execute(times = 3, initialDelayMs = 1) {
                calls++
                client.get("https://example.test/")
            }
        }

        assertEquals(1, calls, "a 4xx should fail immediately, not be retried")
    }
}
