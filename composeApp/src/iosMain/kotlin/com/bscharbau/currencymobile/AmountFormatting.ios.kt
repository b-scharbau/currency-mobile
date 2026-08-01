package com.bscharbau.currencymobile

import platform.Foundation.NSLocale
import platform.Foundation.decimalSeparator
import platform.Foundation.groupingSeparator

actual fun decimalSeparator(): Char = (NSLocale.currentLocale.decimalSeparator ?: ".").first()
actual fun groupingSeparator(): Char = (NSLocale.currentLocale.groupingSeparator ?: ",").first()
