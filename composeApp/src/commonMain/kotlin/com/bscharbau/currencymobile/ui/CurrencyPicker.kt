package com.bscharbau.currencymobile.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bscharbau.currencymobile.BrandColors
import com.bscharbau.currencymobile.Currency
import com.bscharbau.currencymobile.ibmPlexMonoFamily
import com.bscharbau.currencymobile.ibmPlexSansFamily

@Composable
fun CurrencyPicker(
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
