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
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Two-dimensional saturation/value picker for a fixed hue.
 */
class SatValView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var hue: Float = 0f
        set(value) {
            field = normalize(value)
            rebuildShadersIfNeeded(width.toFloat(), height.toFloat())
            invalidate()
        }

    var saturation: Float = 1f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    var value: Float = 1f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    var onColorChanged: ((saturation: Float, value: Float) -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val rect = RectF()
    private var satShader: LinearGradient? = null
    private var valShader: LinearGradient? = null
    private var cachedHue = -1f
    private var cachedWidth = -1f
    private var cachedHeight = -1f

    private fun normalize(h: Float): Float = ((h % 360f) + 360f) % 360f

    private fun rebuildShadersIfNeeded(w: Float, h: Float) {
        if (w <= 0f || h <= 0f) return
        if (satShader != null && hue == cachedHue && w == cachedWidth && h == cachedHeight) return
        val hueColor = Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
        satShader = LinearGradient(0f, 0f, w, 0f, intArrayOf(Color.WHITE, hueColor), null, Shader.TileMode.CLAMP)
        valShader = LinearGradient(0f, 0f, 0f, h, intArrayOf(Color.TRANSPARENT, Color.BLACK), null, Shader.TileMode.CLAMP)
        cachedHue = hue
        cachedWidth = w
        cachedHeight = h
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        rebuildShadersIfNeeded(w, h)
        rect.set(0f, 0f, w, h)
        paint.shader = satShader
        canvas.drawRect(rect, paint)
        paint.shader = valShader
        canvas.drawRect(rect, paint)
        paint.shader = null

        val density = resources.displayMetrics.density
        val x = saturation * w
        val y = (1f - value) * h
        val radius = 10f * density
        indicatorPaint.strokeWidth = 3f * density
        indicatorPaint.color = Color.WHITE
        canvas.drawCircle(x, y, radius, indicatorPaint)
        indicatorPaint.strokeWidth = 1.5f * density
        indicatorPaint.color = Color.argb(200, 0, 0, 0)
        canvas.drawCircle(x, y, radius, indicatorPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (width <= 0 || height <= 0) return true
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                saturation = event.x / width
                value = 1f - event.y / height
                onColorChanged?.invoke(saturation, value)
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
