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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bscharbau.currencymobile.resources.Res
import com.bscharbau.currencymobile.resources.amount_label
import com.bscharbau.currencymobile.resources.currency_list_error
import com.bscharbau.currencymobile.resources.from_label
import com.bscharbau.currencymobile.resources.headline
import com.bscharbau.currencymobile.resources.invalid_amount
import com.bscharbau.currencymobile.resources.live_rates_label
import com.bscharbau.currencymobile.resources.rate_label
import com.bscharbau.currencymobile.resources.rates_error
import com.bscharbau.currencymobile.resources.rates_fetched_no_date
import com.bscharbau.currencymobile.resources.rates_fetched_with_date
import com.bscharbau.currencymobile.resources.result_label
import com.bscharbau.currencymobile.resources.to_label
import com.bscharbau.currencymobile.resources.unknown_error
import org.jetbrains.compose.resources.stringResource

@Composable
fun App(repository: CurrencyRepository) {
    CurrencyMobileTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            var amountText by remember { mutableStateOf("1") }
            var fromCode by remember { mutableStateOf("JPY") }
            var toCode by remember { mutableStateOf("EUR") }
            val decimalSep = remember { decimalSeparator() }
            val groupingSep = remember { groupingSeparator() }

            var currencies by remember { mutableStateOf<List<Currency>>(emptyList()) }
            // The raw exception detail (untranslated, from the network/platform layer) is kept
            // separate from whether the fetch failed at all, so the surrounding "could not load"
            // message can be localized at display time — stringResource() is @Composable and
            // can't be called from inside these coroutines.
            var currenciesFailed by remember { mutableStateOf(false) }
            var currenciesErrorDetail by remember { mutableStateOf<String?>(null) }

            var rateEntry by remember { mutableStateOf<RateEntry?>(null) }
            var rateDate by remember { mutableStateOf<String?>(null) }
            var isLoadingRate by remember { mutableStateOf(true) }
            var rateFailed by remember { mutableStateOf(false) }
            var rateErrorDetail by remember { mutableStateOf<String?>(null) }

            // Uses the cached currency list without touching the network if it's non-empty;
            // otherwise fetches fresh (e.g. on first launch) and caches it.
            LaunchedEffect(Unit) {
                try {
                    currencies = repository.currencies()
                } catch (e: Exception) {
                    currenciesFailed = true
                    currenciesErrorDetail = e.message
                }
            }

            // Uses today's cached rate without touching the network if there is one; otherwise
            // fetches fresh (falling back to a stale cached value if that fetch fails).
            LaunchedEffect(fromCode, toCode) {
                isLoadingRate = true
                rateFailed = false
                rateErrorDetail = null
                rateEntry = null
                rateDate = null

                try {
                    val result = repository.rateFor(fromCode, toCode)
                    rateEntry = RateEntry(toCode, result.rate)
                    rateDate = result.date
                } catch (e: Exception) {
                    rateFailed = true
                    rateErrorDetail = e.message
                } finally {
                    isLoadingRate = false
                }
            }

            val amount = parseAmount(amountText)
            val converted = if (amount != null && rateEntry != null) {
                CurrencyConverter.convert(amount, rateEntry!!.rate)
            } else {
                null
            }

            // A wide/short window (landscape on a phone, or any wide window) doesn't have room to
            // stack everything in one column without the panel's lower rows (RATE especially)
            // running past the bottom of the screen — so it's arranged as two columns instead:
            // hero text + currency selection on the left, the conversion fields on the right.
            BoxWithConstraints(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp)) {
                val isWide = maxWidth > maxHeight
                val onSwap: () -> Unit = {
                    val previousFrom = fromCode
                    fromCode = toCode
                    toCode = previousFrom
                }

                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            HeroContent(rateDate = rateDate, modifier = Modifier.padding(bottom = 28.dp))
                            CurrencySelection(
                                fromCode = fromCode,
                                toCode = toCode,
                                currencies = currencies,
                                currenciesFailed = currenciesFailed,
                                currenciesErrorDetail = currenciesErrorDetail,
                                onFromSelect = { fromCode = it },
                                onToSelect = { toCode = it },
                                onSwap = onSwap,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        VerticalDivider(color = BrandColors.line)

                        ConversionFields(
                            fromCode = fromCode,
                            toCode = toCode,
                            amountText = amountText,
                            onAmountChange = { amountText = it },
                            decimalSeparator = decimalSep,
                            groupingSeparator = groupingSep,
                            isLoadingRate = isLoadingRate,
                            rateFailed = rateFailed,
                            rateErrorDetail = rateErrorDetail,
                            rateEntry = rateEntry,
                            converted = converted,
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    Column {
                        HeroContent(rateDate = rateDate, modifier = Modifier.padding(bottom = 28.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = BrandColors.paper,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, BrandColors.line),
                        ) {
                            Column(modifier = Modifier.padding(28.dp)) {
                                CurrencySelection(
                                    fromCode = fromCode,
                                    toCode = toCode,
                                    currencies = currencies,
                                    currenciesFailed = currenciesFailed,
                                    currenciesErrorDetail = currenciesErrorDetail,
                                    onFromSelect = { fromCode = it },
                                    onToSelect = { toCode = it },
                                    onSwap = onSwap,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                ConversionFields(
                                    fromCode = fromCode,
                                    toCode = toCode,
                                    amountText = amountText,
                                    onAmountChange = { amountText = it },
                                    decimalSeparator = decimalSep,
                                    groupingSeparator = groupingSep,
                                    isLoadingRate = isLoadingRate,
                                    rateFailed = rateFailed,
                                    rateErrorDetail = rateErrorDetail,
                                    rateEntry = rateEntry,
                                    converted = converted,
                                    modifier = Modifier.fillMaxWidth().padding(top = 22.dp),
                                )
                            }
                        }
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
            text = stringResource(Res.string.live_rates_label),
            style = MaterialTheme.typography.labelMedium,
            fontFamily = ibmPlexMonoFamily(),
            color = BrandColors.signal,
        )

        Text(
            text = stringResource(Res.string.headline),
            style = MaterialTheme.typography.headlineSmall,
            color = BrandColors.ink,
            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
        )

        Text(
            text = rateDate?.let { stringResource(Res.string.rates_fetched_with_date, it) }
                ?: stringResource(Res.string.rates_fetched_no_date),
            style = MaterialTheme.typography.bodyMedium,
            color = BrandColors.muted,
            modifier = Modifier.padding(bottom = 20.dp),
        )

        SignalDivider()
    }
}

@Composable
private fun CurrencySelection(
    fromCode: String,
    toCode: String,
    currencies: List<Currency>,
    currenciesFailed: Boolean,
    currenciesErrorDetail: String?,
    onFromSelect: (String) -> Unit,
    onToSelect: (String) -> Unit,
    onSwap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (currenciesFailed) {
            Text(
                text = stringResource(
                    Res.string.currency_list_error,
                    currenciesErrorDetail ?: stringResource(Res.string.unknown_error),
                ),
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
                label = stringResource(Res.string.from_label),
                selected = fromCode,
                currencies = currencies,
                onSelect = onFromSelect,
                modifier = Modifier.weight(1f),
            )

            SwapButton(onClick = onSwap)

            CurrencyPicker(
                label = stringResource(Res.string.to_label),
                selected = toCode,
                currencies = currencies,
                onSelect = onToSelect,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ConversionFields(
    fromCode: String,
    toCode: String,
    amountText: String,
    onAmountChange: (String) -> Unit,
    decimalSeparator: Char,
    groupingSeparator: Char,
    isLoadingRate: Boolean,
    rateFailed: Boolean,
    rateErrorDetail: String?,
    rateEntry: RateEntry?,
    converted: Double?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.amount_label, fromCode),
            style = MaterialTheme.typography.labelMedium,
            color = BrandColors.muted,
        )

        OutlinedTextField(
            value = amountText,
            onValueChange = onAmountChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = ibmPlexMonoFamily()),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            visualTransformation = remember(decimalSeparator, groupingSeparator) {
                ThousandsVisualTransformation(decimalSeparator, groupingSeparator)
            },
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
                text = stringResource(Res.string.result_label),
                style = MaterialTheme.typography.labelMedium,
                color = BrandColors.muted,
            )
            when {
                isLoadingRate -> CircularProgressIndicator(
                    color = BrandColors.signal,
                    modifier = Modifier.padding(top = 8.dp).size(20.dp),
                )
                rateFailed -> Text(
                    text = stringResource(
                        Res.string.rates_error,
                        rateErrorDetail ?: stringResource(Res.string.unknown_error),
                    ),
                    fontFamily = ibmPlexMonoFamily(),
                    color = BrandColors.error,
                    modifier = Modifier.padding(top = 4.dp),
                )
                else -> Text(
                    text = if (converted != null) {
                        "$amountText $fromCode = ${formatAmount(converted)} $toCode"
                    } else {
                        stringResource(Res.string.invalid_amount)
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
                text = stringResource(Res.string.rate_label),
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
