package com.calculator.app.ui.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.ViewModelProvider
import com.calculator.app.data.CalculatorAction
import com.calculator.app.data.CalculatorState
import com.calculator.app.databinding.FragmentCalculatorBinding
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
        setupCopyListeners()
        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state ->
                    updateDisplay(state)
                }
            }
        }
    }

    private fun updateDisplay(state: CalculatorState) {
        if (_binding == null) return

        binding.tvExpression.text = state.expression.ifEmpty { "" }
        binding.tvResult.text = state.result

        val colorRes = if (state.isError) {
            com.google.android.material.R.attr.colorError
        } else {
            com.google.android.material.R.attr.colorOnSurface
        }
        binding.tvResult.setTextColor(MaterialColors.getColor(binding.tvResult, colorRes))
    }

    private fun triggerHaptic() {
        try {
            if (_binding != null) {
                binding.root.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
        } catch (_: Exception) { }
    }

    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    private fun setupClickListeners() {
        val actions: List<Pair<View?, CalculatorAction>> = listOf(
            binding.btn0 to CalculatorAction.Number("0"),
            binding.btn1 to CalculatorAction.Number("1"),
            binding.btn2 to CalculatorAction.Number("2"),
            binding.btn3 to CalculatorAction.Number("3"),
            binding.btn4 to CalculatorAction.Number("4"),
            binding.btn5 to CalculatorAction.Number("5"),
            binding.btn6 to CalculatorAction.Number("6"),
            binding.btn7 to CalculatorAction.Number("7"),
            binding.btn8 to CalculatorAction.Number("8"),
            binding.btn9 to CalculatorAction.Number("9"),
            binding.btnAdd to CalculatorAction.Operator("+"),
            binding.btnSubtract to CalculatorAction.Operator("-"),
            binding.btnMultiply to CalculatorAction.Operator("×"),
            binding.btnDivide to CalculatorAction.Operator("÷"),
            binding.btnClear to CalculatorAction.Clear,
            binding.btnEquals to CalculatorAction.Equals,
            binding.btnDecimal to CalculatorAction.Decimal,
            binding.btnPercent to CalculatorAction.Percent,
            binding.btnBackspace to CalculatorAction.Backspace,
            binding.btnToggleSign to CalculatorAction.ToggleSign,
            binding.btnSqrt to CalculatorAction.SquareRoot,
            binding.btnSquare to CalculatorAction.Square,
            binding.btnReciprocal to CalculatorAction.Reciprocal,
            binding.btnPi to CalculatorAction.Pi,
            binding.btnEuler to CalculatorAction.Euler,
            binding.btnPower to CalculatorAction.Power,
            binding.btnParenthesisOpen to CalculatorAction.ParenthesisOpen,
            binding.btnParenthesisClose to CalculatorAction.ParenthesisClose,
        )

        actions.forEach { (btn, action) ->
            btn?.setOnClickListener {
                triggerHaptic()
                viewModel.onAction(action)
            }
        }
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
