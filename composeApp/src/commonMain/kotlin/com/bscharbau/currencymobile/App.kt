package com.bscharbau.currencymobile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun App() {
    CurrencyMobileTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            var amountText by remember { mutableStateOf("1") }
            var direction by remember { mutableStateOf(CurrencyConverter.Direction.JpyToEur) }
            val api = remember { CurrencyApi() }

            var rateEntry by remember { mutableStateOf<RateEntry?>(null) }
            var rateDate by remember { mutableStateOf<String?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var errorMessage by remember { mutableStateOf<String?>(null) }

            val fromCurrency = CurrencyConverter.fromCurrency(direction)
            val toCurrency = CurrencyConverter.toCurrency(direction)

            LaunchedEffect(direction) {
                isLoading = true
                errorMessage = null
                rateEntry = null
                try {
                    val rates = api.fetchRates(fromCurrency)
                    val entry = rates.rates.firstOrNull { it.to == toCurrency }
                    if (entry == null) {
                        errorMessage = "No rate found for $fromCurrency → $toCurrency"
                    } else {
                        rateEntry = entry
                        rateDate = rates.date
                    }
                } catch (e: Exception) {
                    errorMessage = "Could not load rates: ${e.message ?: "unknown error"}"
                } finally {
                    isLoading = false
                }
            }

            val amount = amountText.toDoubleOrNull()
            val converted = if (amount != null && rateEntry != null) {
                CurrencyConverter.convert(amount, rateEntry!!.rate)
            } else {
                null
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Top,
            ) {
                Text(
                    text = "LIVE RATES · FRANKFURTER API",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = ibmPlexMonoFamily(),
                    color = BrandColors.signal,
                )

                Text(
                    text = "Convert currencies",
                    style = MaterialTheme.typography.headlineSmall,
                    color = BrandColors.ink,
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                )

                Text(
                    text = "Rates are fetched from the currency-calculator API" +
                        (rateDate?.let { ", as of $it" } ?: "") + ".",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandColors.muted,
                    modifier = Modifier.padding(bottom = 20.dp),
                )

                SignalDivider(modifier = Modifier.padding(bottom = 28.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = BrandColors.paper,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, BrandColors.line),
                ) {
                    Column(modifier = Modifier.padding(28.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CurrencyBadge(label = "FROM", currency = fromCurrency, modifier = Modifier.weight(1f))

                            SwapButton(onClick = { direction = direction.swapped() })

                            CurrencyBadge(label = "TO", currency = toCurrency, modifier = Modifier.weight(1f))
                        }

                        Text(
                            text = "AMOUNT ($fromCurrency)",
                            style = MaterialTheme.typography.labelMedium,
                            color = BrandColors.muted,
                            modifier = Modifier.padding(top = 22.dp),
                        )

                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = ibmPlexMonoFamily()),
                            singleLine = true,
                            shape = RoundedCornerShape(4.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = BrandColors.line,
                                focusedBorderColor = BrandColors.signal,
                                unfocusedContainerColor = BrandColors.paper,
                                focusedContainerColor = BrandColors.paper,
                            ),
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 22.dp),
                        )

                        HorizontalDivider(color = BrandColors.line)

                        Column(modifier = Modifier.padding(top = 22.dp)) {
                            Text(
                                text = "RESULT",
                                style = MaterialTheme.typography.labelMedium,
                                color = BrandColors.muted,
                            )
                            when {
                                isLoading -> CircularProgressIndicator(
                                    color = BrandColors.signal,
                                    modifier = Modifier.padding(top = 8.dp).size(20.dp),
                                )
                                errorMessage != null -> Text(
                                    text = errorMessage!!,
                                    fontFamily = ibmPlexMonoFamily(),
                                    color = BrandColors.error,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                                else -> Text(
                                    text = if (converted != null) {
                                        "$amountText $fromCurrency = ${formatAmount(converted)} $toCurrency"
                                    } else {
                                        "Enter a valid amount"
                                    },
                                    fontFamily = ibmPlexMonoFamily(),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 17.sp,
                                    color = BrandColors.ink,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(top = 18.dp)) {
                            Text(
                                text = "RATE",
                                style = MaterialTheme.typography.labelMedium,
                                color = BrandColors.muted,
                            )
                            Text(
                                text = rateEntry?.let { "1 $fromCurrency = ${formatRate(it.rate)} $toCurrency" }
                                    ?: "—",
                                fontFamily = ibmPlexMonoFamily(),
                                fontSize = 14.sp,
                                color = BrandColors.muted,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencyBadge(label: String, currency: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = BrandColors.muted,
        )
        Text(
            text = currency,
            fontFamily = ibmPlexMonoFamily(),
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = BrandColors.ink,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun SwapButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(BrandColors.paper, RoundedCornerShape(4.dp))
            .border(1.dp, BrandColors.line, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "⇄",
            color = BrandColors.signal,
            fontSize = 18.sp,
        )
    }
}

private fun formatAmount(value: Double): String {
    val rounded = kotlin.math.round(value * 100) / 100
    return rounded.toString()
}

private fun formatRate(value: Double): String {
    val decimals = if (value >= 1) 100.0 else 1_000_000.0
    val rounded = kotlin.math.round(value * decimals) / decimals
    return rounded.toString()
}
