package com.bscharbau.currencymobile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
fun App(repository: CurrencyRepository) {
    CurrencyMobileTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            var amountText by remember { mutableStateOf("1") }
            var fromCode by remember { mutableStateOf("JPY") }
            var toCode by remember { mutableStateOf("EUR") }

            var currencies by remember { mutableStateOf<List<Currency>>(emptyList()) }
            var currenciesError by remember { mutableStateOf<String?>(null) }

            var rateEntry by remember { mutableStateOf<RateEntry?>(null) }
            var rateDate by remember { mutableStateOf<String?>(null) }
            var isLoadingRate by remember { mutableStateOf(true) }
            var rateError by remember { mutableStateOf<String?>(null) }

            // Uses the cached currency list without touching the network if it's non-empty;
            // otherwise fetches fresh (e.g. on first launch) and caches it.
            LaunchedEffect(Unit) {
                try {
                    currencies = repository.currencies()
                } catch (e: Exception) {
                    currenciesError = "Could not load currency list: ${e.message ?: "unknown error"}"
                }
            }

            // Uses today's cached rate without touching the network if there is one; otherwise
            // fetches fresh (falling back to a stale cached value if that fetch fails).
            LaunchedEffect(fromCode, toCode) {
                isLoadingRate = true
                rateError = null
                rateEntry = null
                rateDate = null

                try {
                    val result = repository.rateFor(fromCode, toCode)
                    rateEntry = RateEntry(toCode, result.rate)
                    rateDate = result.date
                } catch (e: Exception) {
                    rateError = "Could not load rates: ${e.message ?: "unknown error"}"
                } finally {
                    isLoadingRate = false
                }
            }

            val amount = amountText.toDoubleOrNull()
            val converted = if (amount != null && rateEntry != null) {
                CurrencyConverter.convert(amount, rateEntry!!.rate)
            } else {
                null
            }

            // A wide/short window (landscape on a phone, or any wide window) doesn't have room to
            // stack the intro text above the calculator panel without the panel's lower rows
            // (RATE especially) running past the bottom of the screen — so place them side by
            // side instead of changing the content itself.
            BoxWithConstraints(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp)) {
                val isWide = maxWidth > maxHeight

                if (isWide) {
                    Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        HeroContent(rateDate = rateDate, modifier = Modifier.weight(1f))
                        CalculatorPanel(
                            fromCode = fromCode,
                            toCode = toCode,
                            currencies = currencies,
                            currenciesError = currenciesError,
                            amountText = amountText,
                            onAmountChange = { amountText = it },
                            onFromSelect = { fromCode = it },
                            onToSelect = { toCode = it },
                            onSwap = {
                                val previousFrom = fromCode
                                fromCode = toCode
                                toCode = previousFrom
                            },
                            isLoadingRate = isLoadingRate,
                            rateError = rateError,
                            rateEntry = rateEntry,
                            converted = converted,
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    Column {
                        HeroContent(rateDate = rateDate, modifier = Modifier.padding(bottom = 28.dp))
                        CalculatorPanel(
                            fromCode = fromCode,
                            toCode = toCode,
                            currencies = currencies,
                            currenciesError = currenciesError,
                            amountText = amountText,
                            onAmountChange = { amountText = it },
                            onFromSelect = { fromCode = it },
                            onToSelect = { toCode = it },
                            onSwap = {
                                val previousFrom = fromCode
                                fromCode = toCode
                                toCode = previousFrom
                            },
                            isLoadingRate = isLoadingRate,
                            rateError = rateError,
                            rateEntry = rateEntry,
                            converted = converted,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroContent(rateDate: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
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

        SignalDivider()
    }
}

@Composable
private fun CalculatorPanel(
    fromCode: String,
    toCode: String,
    currencies: List<Currency>,
    currenciesError: String?,
    amountText: String,
    onAmountChange: (String) -> Unit,
    onFromSelect: (String) -> Unit,
    onToSelect: (String) -> Unit,
    onSwap: () -> Unit,
    isLoadingRate: Boolean,
    rateError: String?,
    rateEntry: RateEntry?,
    converted: Double?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = BrandColors.paper,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, BrandColors.line),
    ) {
        Column(modifier = Modifier.padding(28.dp)) {
            if (currenciesError != null) {
                Text(
                    text = currenciesError,
                    fontFamily = ibmPlexMonoFamily(),
                    color = BrandColors.error,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CurrencyPicker(
                    label = "FROM",
                    selected = fromCode,
                    currencies = currencies,
                    onSelect = onFromSelect,
                    modifier = Modifier.weight(1f),
                )

                SwapButton(onClick = onSwap)

                CurrencyPicker(
                    label = "TO",
                    selected = toCode,
                    currencies = currencies,
                    onSelect = onToSelect,
                    modifier = Modifier.weight(1f),
                )
            }

            Text(
                text = "AMOUNT ($fromCode)",
                style = MaterialTheme.typography.labelMedium,
                color = BrandColors.muted,
                modifier = Modifier.padding(top = 22.dp),
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = onAmountChange,
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
                    isLoadingRate -> CircularProgressIndicator(
                        color = BrandColors.signal,
                        modifier = Modifier.padding(top = 8.dp).size(20.dp),
                    )
                    rateError != null -> Text(
                        text = rateError,
                        fontFamily = ibmPlexMonoFamily(),
                        color = BrandColors.error,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    else -> Text(
                        text = if (converted != null) {
                            "$amountText $fromCode = ${formatAmount(converted)} $toCode"
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
                    text = rateEntry?.let { "1 $fromCode = ${formatRate(it.rate)} $toCode" } ?: "—",
                    fontFamily = ibmPlexMonoFamily(),
                    fontSize = 14.sp,
                    color = BrandColors.muted,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun CurrencyPicker(
    label: String,
    selected: String,
    currencies: List<Currency>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = BrandColors.muted,
        )
        Box(modifier = Modifier.padding(top = 4.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BrandColors.line, RoundedCornerShape(4.dp))
                    .clickable(enabled = currencies.isNotEmpty()) { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selected,
                    fontFamily = ibmPlexMonoFamily(),
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = BrandColors.ink,
                )
                Text(text = "▾", color = BrandColors.muted, fontSize = 14.sp)
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                currencies.forEach { currency ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "${currency.code} — ${currency.name}",
                                fontFamily = ibmPlexSansFamily(),
                            )
                        },
                        onClick = {
                            onSelect(currency.code)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SwapButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(top = 20.dp)
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
