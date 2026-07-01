package com.calculator.app.ui.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.HapticFeedbackConstants
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.calculator.app.R
import com.calculator.app.data.CalculatorAction
import com.calculator.app.databinding.FragmentCalculatorBinding

class CalculatorFragment : Fragment() {

    private var _binding: FragmentCalculatorBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CalculatorViewModel
    private var tabId: Int = 0

    companion object {
        fun newInstance(tabId: Int): CalculatorFragment {
            val fragment = CalculatorFragment()
            fragment.arguments = Bundle().apply { putInt("tab_id", tabId) }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tabId = arguments?.getInt("tab_id", 0) ?: 0
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
        setupClickListeners()
        observeState()
    }

    private fun observeState() {
        // Update UI when state changes - we'll use manual refresh
        updateDisplay()
    }

    fun refreshDisplay() {
        updateDisplay()
    }

    private fun updateDisplay() {
        val state = viewModel.state
        binding.tvExpression.text = state.expression.ifEmpty { "" }
        binding.tvResult.text = state.result

        if (state.isError) {
            binding.tvResult.setTextColor(
                resources.getColor(R.color.md_theme_error, requireActivity().theme)
            )
        } else {
            binding.tvResult.setTextColor(
                resources.getColor(R.color.btn_text_primary, requireActivity().theme)
            )
        }
    }

    private fun triggerHaptic() {
        try {
            binding.root.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        } catch (e: Exception) { /* ignore */ }
    }

    private fun setupClickListeners() {
        // Number buttons (0-9)
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

        // Operator buttons
        binding.btnAdd.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Operator("+")); updateDisplay() }
        binding.btnSubtract.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Operator("-")); updateDisplay() }
        binding.btnMultiply.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Operator("×")); updateDisplay() }
        binding.btnDivide.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Operator("÷")); updateDisplay() }

        // Function buttons
        binding.btnClear.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Clear); updateDisplay() }
        binding.btnEquals.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Equals); updateDisplay() }
        binding.btnDecimal.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Decimal); updateDisplay() }
        binding.btnPercent.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Percent); updateDisplay() }
        binding.btnBackspace.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Backspace); updateDisplay() }
        binding.btnToggleSign.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.ToggleSign); updateDisplay() }

        // Advanced functions
        binding.btnSqrt.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.SquareRoot); updateDisplay() }
        binding.btnSquare.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Square); updateDisplay() }
        binding.btnReciprocal.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Reciprocal); updateDisplay() }
        binding.btnPi.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Pi); updateDisplay() }
        binding.btnEuler.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Euler); updateDisplay() }
        binding.btnPower.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.Power); updateDisplay() }
        binding.btnParenthesisOpen.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.ParenthesisOpen); updateDisplay() }
        binding.btnParenthesisClose.setOnClickListener { triggerHaptic(); viewModel.onAction(CalculatorAction.ParenthesisClose); updateDisplay() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
