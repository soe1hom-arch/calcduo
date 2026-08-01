package com.calculator.app

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import com.google.android.material.button.MaterialButton

/**
 * Free-form HSV color picker with a hex input, used for the custom accent color.
 */
object ColorPickerDialog {

    fun show(context: Context, initialColor: Int, onColorPicked: (Int) -> Unit, title: String? = null) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_color_picker, null, false)
        view.findViewById<android.widget.TextView>(R.id.picker_title).text =
            title ?: context.getString(R.string.accent_picker_title)
        val satVal = view.findViewById<SatValView>(R.id.picker_satval)
        val hueSlider = view.findViewById<HueSliderView>(R.id.picker_hue)
        val preview = view.findViewById<View>(R.id.picker_preview)
        val hexInput = view.findViewById<EditText>(R.id.picker_hex)
        val btnSave = view.findViewById<MaterialButton>(R.id.btn_picker_save)

        val previewBg = GradientDrawable().apply { shape = GradientDrawable.OVAL }
        preview.background = previewBg

        var color = initialColor
        var syncingHex = false

        fun syncHex(c: Int) {
            val hex = String.format("#%06X", 0xFFFFFF and c)
            if (hexInput.text.toString() == hex) return
            syncingHex = true
            hexInput.setText(hex)
            hexInput.setSelection(hex.length)
            syncingHex = false
        }

        fun applyColor(c: Int) {
            color = c
            previewBg.setColor(c)
            btnSave.backgroundTintList = ColorStateList.valueOf(c)
            btnSave.setTextColor(CustomAccent.readableTextColor(c))
            syncHex(c)
        }

        hueSlider.onHueChanged = { h ->
            satVal.hue = h
            applyColor(Color.HSVToColor(floatArrayOf(h, satVal.saturation, satVal.value)))
        }
        satVal.onColorChanged = { s, v ->
            applyColor(Color.HSVToColor(floatArrayOf(hueSlider.hue, s, v)))
        }
        hexInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (syncingHex) return
                val parsed = parseHex(s?.toString()) ?: return
                if (parsed == color) return
                val hsv = FloatArray(3)
                Color.colorToHSV(parsed, hsv)
                hueSlider.hue = hsv[0]
                satVal.hue = hsv[0]
                satVal.saturation = hsv[1]
                satVal.value = hsv[2]
                applyColor(parsed)
            }
        })

        val hsv = FloatArray(3)
        Color.colorToHSV(initialColor, hsv)
        hueSlider.hue = hsv[0]
        satVal.hue = hsv[0]
        satVal.saturation = hsv[1]
        satVal.value = hsv[2]
        applyColor(initialColor)

        val dialog = Dialog(context)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.92f).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        view.findViewById<View>(R.id.btn_picker_cancel).setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            onColorPicked(color)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun parseHex(text: String?): Int? {
        val t = text?.trim()?.removePrefix("#") ?: return null
        if (t.length != 6) return null
        val rgb = t.toIntOrNull(16) ?: return null
        return 0xFF000000.toInt() or rgb
    }
}
