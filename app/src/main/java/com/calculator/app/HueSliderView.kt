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
 * Vertical hue slider (0..360).
 */
class HueSliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var hue: Float = 0f
        set(value) {
            field = ((value % 360f) + 360f) % 360f
            invalidate()
        }

    var onHueChanged: ((hue: Float) -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private var gradient: LinearGradient? = null
    private var cachedWidth = -1f
    private var cachedHeight = -1f

    private val hueColors = intArrayOf(
        Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED
    )

    private fun rebuildGradientIfNeeded(w: Float, h: Float) {
        if (w <= 0f || h <= 0f) return
        if (gradient != null && w == cachedWidth && h == cachedHeight) return
        gradient = LinearGradient(0f, 0f, 0f, h, hueColors, null, Shader.TileMode.CLAMP)
        cachedWidth = w
        cachedHeight = h
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        rebuildGradientIfNeeded(w, h)
        rect.set(0f, 0f, w, h)
        paint.shader = gradient
        canvas.drawRect(rect, paint)
        paint.shader = null

        val density = resources.displayMetrics.density
        val y = hue / 360f * h
        val barHeight = 4f * density
        indicatorPaint.style = Paint.Style.FILL
        indicatorPaint.color = Color.WHITE
        canvas.drawRect(RectF(0f, y - barHeight / 2f, w, y + barHeight / 2f), indicatorPaint)
        indicatorPaint.style = Paint.Style.STROKE
        indicatorPaint.strokeWidth = 1.5f * density
        indicatorPaint.color = Color.argb(200, 0, 0, 0)
        canvas.drawRect(RectF(0f, y - barHeight / 2f, w, y + barHeight / 2f), indicatorPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (height <= 0) return true
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                hue = (event.y / height * 360f).coerceIn(0f, 359.99f)
                onHueChanged?.invoke(hue)
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
