package com.bscharbau.currencymobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.bscharbau.currencymobile.db.AppDatabase
import com.bscharbau.currencymobile.db.DatabaseDriverFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // The app's background is light (BrandColors.paper), so tell the system to use dark
        // status/navigation bar icons — otherwise the default light icons are barely visible
        // against it, since edge-to-edge draws the app's background behind both bars.
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        val driver = DatabaseDriverFactory(applicationContext).createDriver()
        val repository = CurrencyRepository(AppDatabase(driver))
        setContent {
            App(repository)
        }
    }
}
