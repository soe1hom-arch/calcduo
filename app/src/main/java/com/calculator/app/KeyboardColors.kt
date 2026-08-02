/*
 * Copyright 2026 soe1hom-arch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * User-configurable colors for the keyboard button edges.
 */
object KeyboardColors {
    private const val PREFS = "calcduo_settings"
    const val KEY_EDGE = "button_edge_color"
    const val KEY_OPERATOR_EDGE = "operator_edge_color"

    fun edgeColor(context: Context): Int? = readHex(context, KEY_EDGE)

    fun operatorEdgeColor(context: Context): Int? = readHex(context, KEY_OPERATOR_EDGE)

    fun setEdge(context: Context, hex: String?) = write(context, KEY_EDGE, hex)

    fun setOperatorEdge(context: Context, hex: String?) = write(context, KEY_OPERATOR_EDGE, hex)

    fun applyEdge(context: Context, root: View) {
        val color = edgeColor(context) ?: return
        collectButtons(root).forEach { applyStroke(it, color) }
    }

    fun applyOperatorEdge(context: Context, buttons: List<MaterialButton>) {
        val color = operatorEdgeColor(context) ?: return
        buttons.forEach { applyStroke(it, color) }
    }

    private fun applyStroke(button: MaterialButton, color: Int) {
        val density = button.resources.displayMetrics.density
        button.strokeWidth = (2f * density).toInt()
        button.strokeColor = ColorStateList.valueOf(color)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            button.outlineSpotShadowColor = color
            button.outlineAmbientShadowColor = ColorUtils.setAlphaComponent(color, 0x40)
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
