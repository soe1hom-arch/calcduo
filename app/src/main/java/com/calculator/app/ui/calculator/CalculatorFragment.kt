package com.calculator.app.ui.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.ViewModelProvider
import com.calculator.app.data.CalculatorAction
import com.calculator.app.data.CalculatorEngine
import com.calculator.app.data.CalculatorState
import com.calculator.app.databinding.FragmentCalculatorBinding
import com.calculator.app.R
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CalculatorFragment : Fragment() {

    private var _binding: FragmentCalculatorBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CalculatorViewModel
    private var tabId: Int = 0
    private var onTabSelectedListener: ((Int) -> Unit)? = null

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
        setupTapToSelect()
        setupCopyListeners()
        observeState()
    }

    fun setOnTabSelectedListener(listener: (Int) -> Unit) {
        onTabSelectedListener = listener
    }

    fun getTabId(): Int = tabId

    fun getHistoryLog(): List<String> = viewModel.getHistoryLog()

    fun setLabel(label: String) {
        if (_binding != null) {
            binding.tvCalculatorLabel.text = label
        }
    }

    fun performAction(action: CalculatorAction) {
        viewModel.onAction(action)
    }

    fun clear() {
        viewModel.clear()
    }

    fun restoreHistory(expression: String, result: String) {
        viewModel.restore(expression, result)
    }

    fun clearHistory() {
        viewModel.clearHistory()
    }

    private fun setupTapToSelect() {
        binding.root.setOnClickListener {
            onTabSelectedListener?.invoke(tabId)
        }
        binding.root.isClickable = true
        binding.root.isFocusable = true
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

        binding.tvExpression.text = CalculatorEngine.formatExpression(state.expression).ifEmpty { "" }
        binding.tvResult.text = CalculatorEngine.formatDisplayNumber(state.result)
        binding.tvHistory.text = CalculatorEngine.formatExpression(state.history).ifEmpty { "" }
        binding.tvMemoryIndicator.visibility = if (state.hasMemory) View.VISIBLE else View.GONE
        
        // Show copy button only when result is meaningful
        val hasResult = state.result.isNotEmpty() && state.result != "0" && state.result != "Error"
        binding.btnCopyResult.visibility = if (hasResult) View.VISIBLE else View.GONE
        
        // Auto-scroll expression to end
        binding.tvExpression.post {
            binding.tvExpression.parent?.let { parent ->
                if (parent is android.widget.HorizontalScrollView) {
                    parent.fullScroll(android.view.View.FOCUS_RIGHT)
                }
            }
        }

        val colorRes = if (state.isError) {
            com.google.android.material.R.attr.colorError
        } else {
            com.google.android.material.R.attr.colorOnSurface
        }
        binding.tvResult.setTextColor(MaterialColors.getColor(binding.tvResult, colorRes))
    }

    private fun setupCopyListeners() {
        val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

        binding.btnCopyResult.setOnClickListener {
            val text = binding.tvResult.text.toString()
            if (text.isNotEmpty() && text != "0" && text != "Error") {
                clipboard?.setPrimaryClip(ClipData.newPlainText(getString(R.string.app_name), text))
                Toast.makeText(context, getString(R.string.copied_result, text), Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvExpression.setOnClickListener {
            val text = binding.tvExpression.text.toString()
            if (text.isNotEmpty()) {
                clipboard?.setPrimaryClip(ClipData.newPlainText(getString(R.string.app_name), text))
                Toast.makeText(context, getString(R.string.copied_result, text), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
