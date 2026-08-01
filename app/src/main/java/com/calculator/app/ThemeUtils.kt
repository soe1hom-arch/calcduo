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

        activity.setTheme(resolveTheme(mode, palette))
    }

    fun applySystemBarInsets(root: View, includeIme: Boolean = false) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeBottom = if (includeIme) insets.getInsets(WindowInsetsCompat.Type.ime()).bottom else 0
            v.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, imeBottom))
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun resolveTheme(mode: String, palette: String): Int {
        if (mode == "grey") return R.style.Theme_CalculatorApp_Grey
        val (base, light, dark) = when (palette) {
            "orange" -> Triple(
                R.style.Theme_CalculatorApp_Palette_Orange,
                R.style.Theme_CalculatorApp_Palette_Orange_Light,
                R.style.Theme_CalculatorApp_Palette_Orange_Dark
            )
            "green" -> Triple(
                R.style.Theme_CalculatorApp_Palette_Green,
                R.style.Theme_CalculatorApp_Palette_Green_Light,
                R.style.Theme_CalculatorApp_Palette_Green_Dark
            )
            "blue" -> Triple(
                R.style.Theme_CalculatorApp_Palette_Blue,
                R.style.Theme_CalculatorApp_Palette_Blue_Light,
                R.style.Theme_CalculatorApp_Palette_Blue_Dark
            )
            "pink" -> Triple(
                R.style.Theme_CalculatorApp_Palette_Pink,
                R.style.Theme_CalculatorApp_Palette_Pink_Light,
                R.style.Theme_CalculatorApp_Palette_Pink_Dark
            )
            "grey" -> Triple(
                R.style.Theme_CalculatorApp_Palette_Grey,
                R.style.Theme_CalculatorApp_Palette_Grey_Light,
                R.style.Theme_CalculatorApp_Palette_Grey_Dark
            )
            else -> Triple(
                R.style.Theme_CalculatorApp_Palette_Purple,
                R.style.Theme_CalculatorApp_Palette_Purple_Light,
                R.style.Theme_CalculatorApp_Palette_Purple_Dark
            )
        }
        return when (mode) {
            "light" -> light
            "dark" -> dark
            else -> base
        }
    }
}
