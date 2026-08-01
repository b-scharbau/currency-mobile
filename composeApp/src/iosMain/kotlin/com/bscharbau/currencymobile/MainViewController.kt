package com.bscharbau.currencymobile

import androidx.compose.ui.window.ComposeUIViewController
import com.bscharbau.currencymobile.db.AppDatabase
import com.bscharbau.currencymobile.db.DatabaseDriverFactory
import com.bscharbau.currencymobile.ui.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    val driver = DatabaseDriverFactory().createDriver()
    val repository = CurrencyRepository(AppDatabase(driver))
    return ComposeUIViewController { App(repository) }
}
