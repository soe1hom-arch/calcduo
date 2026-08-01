package com.calculator.app

import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

object ThemeUtils {
    fun applySystemBarInsets(root: View, includeIme: Boolean = false) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeBottom = if (includeIme) insets.getInsets(WindowInsetsCompat.Type.ime()).bottom else 0
            v.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, imeBottom))
            WindowInsetsCompat.CONSUMED
        }
    }

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
