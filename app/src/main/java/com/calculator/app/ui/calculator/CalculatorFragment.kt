package com.calculator.app.ui.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.ClipData
import android.content.ClipboardManager
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.calculator.app.R
import com.calculator.app.data.CalculatorAction
import com.calculator.app.databinding.FragmentCalculatorBinding
import com.google.android.material.color.MaterialColors

class CalculatorFragment : Fragment() {

    private var _binding: FragmentCalculatorBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CalculatorViewModel
    private var tabId: Int = 0

    companion object {
        private const val ARG_TAB_ID = "tab_id"
        private const val ARG_LABEL = "label"

        fun newInstance(tabId: Int, label: String = "Calculator"): CalculatorFragment {
            val fragment = CalculatorFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_TAB_ID, tabId)
                putString(ARG_LABEL, label)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tabId = arguments?.getInt(ARG_TAB_ID, 0) ?: 0
        viewModel = ViewModelProvider(this)[CalculatorViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalculatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val label = arguments?.getString(ARG_LABEL, "Calculator") ?: "Calculator"
        binding.tvCalculatorLabel.text = label
        setupClickListeners()
        updateDisplay()
        setupCopyListeners()
    }

    fun refreshDisplay() {
        if (_binding == null || !isAdded) return
        updateDisplay()
    }

    private fun updateDisplay() {
        if (_binding == null || !isAdded) return

        val state = viewModel.state
        val ctx = context ?: return

        binding.tvExpression.text = state.expression.ifEmpty { "" }
        binding.tvResult.text = state.result

        val color = if (state.isError) {
            com.google.android.material.R.attr.colorError
        } else {
            com.google.android.material.R.attr.colorOnSurface
        }
        binding.tvResult.setTextColor(MaterialColors.getColor(binding.tvResult, color))
    }

    private fun triggerHaptic() {
        try {
            if (_binding != null) {
                binding.root.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
        } catch (_: Exception) { }
    }

    private fun setupClickListeners() {
        binding.btn0.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Number("0")); updateDisplay() }
        binding.btn1.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Number("1")); updateDisplay() }
        binding.btn2.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Number("2")); updateDisplay() }
        binding.btn3.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Number("3")); updateDisplay() }
        binding.btn4.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Number("4")); updateDisplay() }
        binding.btn5.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Number("5")); updateDisplay() }
        binding.btn6.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Number("6")); updateDisplay() }
        binding.btn7.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Number("7")); updateDisplay() }
        binding.btn8.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Number("8")); updateDisplay() }
        binding.btn9.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Number("9")); updateDisplay() }

        binding.btnAdd.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Operator("+")); updateDisplay() }
        binding.btnSubtract.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Operator("-")); updateDisplay() }
        binding.btnMultiply.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Operator("×")); updateDisplay() }
        binding.btnDivide.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Operator("÷")); updateDisplay() }

        binding.btnClear.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Clear); updateDisplay() }
        binding.btnEquals.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Equals); updateDisplay() }
        binding.btnDecimal.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Decimal); updateDisplay() }
        binding.btnPercent.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Percent); updateDisplay() }
        binding.btnBackspace.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Backspace); updateDisplay() }
        binding.btnToggleSign.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.ToggleSign); updateDisplay() }

        binding.btnSqrt.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.SquareRoot); updateDisplay() }
        binding.btnSquare.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Square); updateDisplay() }
        binding.btnReciprocal.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Reciprocal); updateDisplay() }
        binding.btnPi.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Pi); updateDisplay() }
        binding.btnEuler.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Euler); updateDisplay() }
        binding.btnPower.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Power); updateDisplay() }
        binding.btnParenthesisOpen.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.ParenthesisOpen); updateDisplay() }
        binding.btnParenthesisClose.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.ParenthesisClose); updateDisplay() }
    }

    private fun setupCopyListeners() {
        val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

        binding.tvResult.setOnLongClickListener {
            val text = binding.tvResult.text.toString()
            if (text.isNotEmpty() && text != "0" && text != "Error") {
                clipboard?.setPrimaryClip(ClipData.newPlainText("CalcDuo", text))
                Toast.makeText(context, "Copied: $text", Toast.LENGTH_SHORT).show()
                true
            } else false
        }

        binding.tvExpression.setOnLongClickListener {
            val text = binding.tvExpression.text.toString()
            if (text.isNotEmpty()) {
                clipboard?.setPrimaryClip(ClipData.newPlainText("CalcDuo", text))
                Toast.makeText(context, "Copied: $text", Toast.LENGTH_SHORT).show()
                true
            } else false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
