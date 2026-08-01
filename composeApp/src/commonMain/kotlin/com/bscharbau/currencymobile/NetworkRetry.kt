package com.bscharbau.currencymobile

import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.delay

/**
 * Mirrors the backend's own retry policy (FrankfurterRetry): retries transient failures (timeouts,
 * connection errors, 5xx server errors, ...) with increasing backoff, but never retries a 4xx
 * client error (e.g. an unknown currency code) — that would just fail the same way again.
 */
object NetworkRetry {
    suspend fun <T> execute(times: Int = 3, initialDelayMs: Long = 300, block: suspend () -> T): T {
        var attempt = 0
        var delayMs = initialDelayMs
        while (true) {
            try {
                return block()
            } catch (e: ClientRequestException) {
                throw e
            } catch (e: Exception) {
                attempt++
                if (attempt >= times) {
                    throw e
                }
                delay(delayMs)
                delayMs *= 2
            }
        }
    }
}
