package com.bscharbau.currencymobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bscharbau.currencymobile.db.AppDatabase
import com.bscharbau.currencymobile.db.DatabaseDriverFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val driver = DatabaseDriverFactory(applicationContext).createDriver()
        val repository = CurrencyRepository(AppDatabase(driver))
        setContent {
            App(repository)
        }
    }
}
