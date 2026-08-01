package com.bscharbau.currencymobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.bscharbau.currencymobile.db.AppDatabase
import com.bscharbau.currencymobile.db.DatabaseDriverFactory
import com.bscharbau.currencymobile.ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val driver = DatabaseDriverFactory(applicationContext).createDriver()
        val repository = CurrencyRepository(AppDatabase(driver))
        setContent {
            // The app's background is BrandColors.paper — near-white in light mode, dark navy in
            // dark mode — so the status/navigation bar icons need to flip to match; otherwise one
            // pairing is barely visible, since edge-to-edge draws the app's background behind
            // both bars. isSystemInDarkTheme() recomposes if the system theme changes at runtime.
            val isDarkTheme = isSystemInDarkTheme()
            val view = LocalView.current
            SideEffect {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !isDarkTheme
                    isAppearanceLightNavigationBars = !isDarkTheme
                }
            }

            App(repository)
        }
    }
}
