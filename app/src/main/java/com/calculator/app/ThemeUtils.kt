package com.calculator.app

import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

object ThemeUtils {

    fun apply(activity: AppCompatActivity) {
        val prefs = activity.getSharedPreferences("calcduo_settings", Context.MODE_PRIVATE)
        val mode = prefs.getString("theme", "system") ?: "system"

        AppCompatDelegate.setDefaultNightMode(
            when (mode) {
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                "dark", "grey" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )

        activity.setTheme(resolveTheme(mode))
    }

    fun applySystemBarInsets(root: View, includeIme: Boolean = false) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeBottom = if (includeIme) insets.getInsets(WindowInsetsCompat.Type.ime()).bottom else 0
            v.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, imeBottom))
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun resolveTheme(mode: String): Int = when (mode) {
        "light" -> R.style.Theme_CalculatorApp_Light
        "dark" -> R.style.Theme_CalculatorApp_Dark
        "grey" -> R.style.Theme_CalculatorApp_Grey
        else -> R.style.Theme_CalculatorApp
    }
}
