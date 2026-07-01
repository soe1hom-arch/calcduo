package com.calculator.app.ui.calculator

import androidx.lifecycle.ViewModel
import com.calculator.app.data.CalculatorAction
import com.calculator.app.data.CalculatorEngine
import com.calculator.app.data.CalculatorState

class CalculatorViewModel : ViewModel() {
    var state = CalculatorState()
        private set

    fun onAction(action: CalculatorAction) {
        state = CalculatorEngine.processAction(state, action)
    }

    fun clear() {
        state = CalculatorState()
    }
}
