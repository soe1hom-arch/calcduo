package com.calculator.app

import android.content.Context
import android.content.res.ColorStateList
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.edit
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton

object CustomAccent {
    private const val PREFS = "calcduo_settings"
    private const val KEY = "accent_custom"

    fun get(context: Context): Int? {
        val hex = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
            ?: return null
        val rgb = hex.removePrefix("#").toIntOrNull(16) ?: return null
        return 0xFF000000.toInt() or rgb
    }

    fun set(context: Context, hex: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
            if (hex == null) remove(KEY) else putString(KEY, hex)
        }
    }

    fun darken(color: Int, factor: Float): Int {
        val r = (color shr 16 and 0xFF) * (1 - factor)
        val g = (color shr 8 and 0xFF) * (1 - factor)
        val b = (color and 0xFF) * (1 - factor)
        return 0xFF000000.toInt() or (r.toInt() shl 16) or (g.toInt() shl 8) or b.toInt()
    }

    fun readableTextColor(background: Int): Int {
        val lum = 0.299 * (background shr 16 and 0xFF) +
            0.587 * (background shr 8 and 0xFF) +
            0.114 * (background and 0xFF)
        return if (lum > 140) 0xFF1C1B1F.toInt() else 0xFFFFFFFF.toInt()
    }

    fun applyOperatorButton(context: Context, button: MaterialButton) {
        val accent = get(context) ?: return
        val bg = darken(accent, 0.28f)
        button.backgroundTintList = ColorStateList.valueOf(bg)
        button.setTextColor(readableTextColor(bg))
    }

    fun applyEqualsButton(context: Context, button: MaterialButton) {
        val accent = get(context) ?: return
        button.backgroundTintList = ColorStateList.valueOf(accent)
        button.setTextColor(readableTextColor(accent))
    }

    fun tintFab(context: Context, fab: FloatingActionButton) {
        val accent = get(context) ?: return
        fab.backgroundTintList = ColorStateList.valueOf(accent)
    }

    fun tintTextView(context: Context, view: TextView) {
        val accent = get(context) ?: return
        view.setTextColor(accent)
    }

    fun tintImageView(context: Context, view: ImageView) {
        val accent = get(context) ?: return
        view.imageTintList = ColorStateList.valueOf(accent)
    }

    fun tintCardStroke(context: Context, card: MaterialCardView) {
        val accent = get(context) ?: return
        card.strokeColor = accent
    }
}
