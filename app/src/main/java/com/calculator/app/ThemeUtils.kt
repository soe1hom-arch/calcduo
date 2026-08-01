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
        val palette = prefs.getString("palette", "purple") ?: "purple"

        AppCompatDelegate.setDefaultNightMode(
            when (mode) {
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                "dark", "grey" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )

        activity.setTheme(resolveTheme(activity, mode, palette))
    }

    fun applySystemBarInsets(root: View, includeIme: Boolean = false) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeBottom = if (includeIme) insets.getInsets(WindowInsetsCompat.Type.ime()).bottom else 0
            v.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, imeBottom))
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun resolveTheme(activity: AppCompatActivity, mode: String, palette: String): Int {
        if (mode == "grey") return R.style.Theme_CalculatorApp_Grey
        val suffix = when (mode) {
            "light" -> "Light"
            "dark" -> "Dark"
            else -> ""
        }
        val styleName = "Theme_CalculatorApp_Palette_${palette.replaceFirstChar { it.uppercase() }}$suffix"
        val styleRes = activity.resources.getIdentifier(styleName, "style", activity.packageName)
        return if (styleRes != 0) styleRes else R.style.Theme_CalculatorApp
    }
}
