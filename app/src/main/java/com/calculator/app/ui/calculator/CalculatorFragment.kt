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

    fun performAction(action: CalculatorAction) {

    fun clear() {
        viewModel.clear()
    }
        viewModel.onAction(action)
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

        binding.tvExpression.text = state.expression.ifEmpty { "" }
        binding.tvResult.text = state.result

        val colorRes = if (state.isError) {
            com.google.android.material.R.attr.colorError
        } else {
            com.google.android.material.R.attr.colorOnSurface
        }
        binding.tvResult.setTextColor(MaterialColors.getColor(binding.tvResult, colorRes))
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
