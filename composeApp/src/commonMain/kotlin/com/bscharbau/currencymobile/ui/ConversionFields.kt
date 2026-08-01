package com.bscharbau.currencymobile.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bscharbau.currencymobile.BrandColors
import com.bscharbau.currencymobile.RateEntry
import com.bscharbau.currencymobile.ThousandsVisualTransformation
import com.bscharbau.currencymobile.ibmPlexMonoFamily
import com.bscharbau.currencymobile.resources.Res
import com.bscharbau.currencymobile.resources.amount_label
import com.bscharbau.currencymobile.resources.invalid_amount
import com.bscharbau.currencymobile.resources.rate_label
import com.bscharbau.currencymobile.resources.rates_error
import com.bscharbau.currencymobile.resources.result_label
import com.bscharbau.currencymobile.resources.unknown_error
import org.jetbrains.compose.resources.stringResource

@Composable
fun ConversionFields(
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

private fun formatAmount(value: Double): String {
    val rounded = kotlin.math.round(value * 100) / 100
    return rounded.toString()
}

private fun formatRate(value: Double): String {
    val decimals = if (value >= 1) 100.0 else 1_000_000.0
    val rounded = kotlin.math.round(value * decimals) / decimals
    return rounded.toString()
}
