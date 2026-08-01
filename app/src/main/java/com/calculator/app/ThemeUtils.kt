package com.calculator.app

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

object ThemeUtils {
    fun apply(activity: AppCompatActivity) {
        val mode = activity.getSharedPreferences("calcduo_settings", Context.MODE_PRIVATE)
            .getString("theme", "system") ?: "system"
        when (mode) {
            "light" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                activity.setTheme(R.style.Theme_CalculatorApp_Light)
            }
            "dark" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                activity.setTheme(R.style.Theme_CalculatorApp_Dark)
            }
            "grey" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                activity.setTheme(R.style.Theme_CalculatorApp_Grey)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                activity.setTheme(R.style.Theme_CalculatorApp)
            }
        }
    }
}
