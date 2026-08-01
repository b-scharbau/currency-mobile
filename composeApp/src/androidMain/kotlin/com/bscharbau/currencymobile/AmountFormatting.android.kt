package com.bscharbau.currencymobile

import java.text.DecimalFormatSymbols
import java.util.Locale

actual fun decimalSeparator(): Char = DecimalFormatSymbols.getInstance(Locale.getDefault()).decimalSeparator
actual fun groupingSeparator(): Char = DecimalFormatSymbols.getInstance(Locale.getDefault()).groupingSeparator
