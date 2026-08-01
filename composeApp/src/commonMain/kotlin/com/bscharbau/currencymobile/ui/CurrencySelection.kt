package com.bscharbau.currencymobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bscharbau.currencymobile.BrandColors
import com.bscharbau.currencymobile.Currency
import com.bscharbau.currencymobile.ibmPlexMonoFamily
import com.bscharbau.currencymobile.resources.Res
import com.bscharbau.currencymobile.resources.currency_list_error
import com.bscharbau.currencymobile.resources.from_label
import com.bscharbau.currencymobile.resources.to_label
import com.bscharbau.currencymobile.resources.unknown_error
import org.jetbrains.compose.resources.stringResource

@Composable
fun CurrencySelection(
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
