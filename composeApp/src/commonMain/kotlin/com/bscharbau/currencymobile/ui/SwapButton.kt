package com.bscharbau.currencymobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bscharbau.currencymobile.BrandColors

@Composable
fun SwapButton(onClick: () -> Unit) {
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
