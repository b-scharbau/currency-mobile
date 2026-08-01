package com.bscharbau.currencymobile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bscharbau.currencymobile.ui.ConversionFields
import com.bscharbau.currencymobile.ui.CurrencySelection
import com.bscharbau.currencymobile.ui.HeroContent

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
