package com.bscharbau.currencymobile.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bscharbau.currencymobile.BrandColors
import com.bscharbau.currencymobile.SignalDivider
import com.bscharbau.currencymobile.ibmPlexMonoFamily
import com.bscharbau.currencymobile.resources.Res
import com.bscharbau.currencymobile.resources.headline
import com.bscharbau.currencymobile.resources.live_rates_label
import com.bscharbau.currencymobile.resources.rates_fetched_no_date
import com.bscharbau.currencymobile.resources.rates_fetched_with_date
import org.jetbrains.compose.resources.stringResource

@Composable
fun HeroContent(rateDate: String?, modifier: Modifier = Modifier) {
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
