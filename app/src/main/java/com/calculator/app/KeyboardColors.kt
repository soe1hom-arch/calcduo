package com.calculator.app

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.core.graphics.ColorUtils
import com.google.android.material.button.MaterialButton

/**
 * User-configurable colors for the keyboard grid background and button edges.
 */
object KeyboardColors {
    private const val PREFS = "calcduo_settings"
    const val KEY_GRID = "grid_color"
    const val KEY_EDGE = "button_edge_color"

    fun gridColor(context: Context): Int? = readHex(context, KEY_GRID)

    fun edgeColor(context: Context): Int? = readHex(context, KEY_EDGE)

    fun setGrid(context: Context, hex: String?) = write(context, KEY_GRID, hex)

    fun setEdge(context: Context, hex: String?) = write(context, KEY_EDGE, hex)

    fun applyGrid(context: Context, keyboardRoot: View) {
        val color = gridColor(context) ?: return
        keyboardRoot.setBackgroundColor(color)
    }

    fun applyEdge(context: Context, root: View) {
        val color = edgeColor(context) ?: return
        val density = context.resources.displayMetrics.density
        val strokeWidth = (2f * density).toInt()
        collectButtons(root).forEach { button ->
            button.strokeWidth = strokeWidth
            button.strokeColor = ColorStateList.valueOf(color)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                button.outlineSpotShadowColor = color
                button.outlineAmbientShadowColor = ColorUtils.setAlphaComponent(color, 0x40)
            }
        }
    }

    private fun collectButtons(root: View): List<MaterialButton> {
        val result = mutableListOf<MaterialButton>()
        fun walk(view: View) {
            if (view is MaterialButton) {
                result.add(view)
            } else if (view is ViewGroup) {
                for (i in 0 until view.childCount) walk(view.getChildAt(i))
            }
        }
        walk(root)
        return result
    }

    private fun readHex(context: Context, key: String): Int? {
        val hex = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(key, null)
            ?: return null
        val rgb = hex.removePrefix("#").toIntOrNull(16) ?: return null
        return 0xFF000000.toInt() or rgb
    }

    private fun write(context: Context, key: String, hex: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
            if (hex == null) remove(key) else putString(key, hex)
        }
    }
}
