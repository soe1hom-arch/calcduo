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

package com.calculator.app.ui.calculator

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.calculator.app.data.CalculatorAction
import com.calculator.app.data.CalculatorEngine
import com.calculator.app.data.CalculatorState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalculatorViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _state = MutableStateFlow(
        CalculatorState(
            expression = savedStateHandle[KEY_EXPRESSION] ?: "",
            result = savedStateHandle[KEY_RESULT] ?: "0",
            isError = savedStateHandle[KEY_IS_ERROR] ?: false,
            errorMessage = savedStateHandle[KEY_ERROR_MESSAGE] ?: "",
            history = savedStateHandle[KEY_HISTORY] ?: "",
            memory = savedStateHandle[KEY_MEMORY] ?: 0.0,
            hasMemory = savedStateHandle[KEY_HAS_MEMORY] ?: false,
            historyLog = savedStateHandle[KEY_HISTORY_LOG] ?: emptyList()
        )
    )
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    fun onAction(action: CalculatorAction) {
        val newState = CalculatorEngine.processAction(_state.value, action)
        _state.value = newState
        persist(newState)
    }

    fun clear() {
        _state.value = CalculatorState()
        persist(_state.value)
    }

    fun getHistoryLog(): List<String> = _state.value.historyLog

    fun restore(expression: String, result: String) {
        val newState = _state.value.copy(
            expression = expression,
            result = result,
            history = "",
            isError = false,
            errorMessage = "",
            justEvaluated = true
        )
        _state.value = newState
        persist(newState)
    }

    fun clearHistory() {
        val newState = _state.value.copy(historyLog = emptyList())
        _state.value = newState
        persist(newState)
    }

    private fun persist(state: CalculatorState) {
        savedStateHandle[KEY_EXPRESSION] = state.expression
        savedStateHandle[KEY_RESULT] = state.result
        savedStateHandle[KEY_IS_ERROR] = state.isError
        savedStateHandle[KEY_ERROR_MESSAGE] = state.errorMessage
        savedStateHandle[KEY_HISTORY] = state.history
        savedStateHandle[KEY_MEMORY] = state.memory
        savedStateHandle[KEY_HAS_MEMORY] = state.hasMemory
        savedStateHandle[KEY_HISTORY_LOG] = state.historyLog.toMutableList()
    }

    private companion object {
        const val KEY_EXPRESSION = "expression"
        const val KEY_RESULT = "result"
        const val KEY_IS_ERROR = "isError"
        const val KEY_ERROR_MESSAGE = "errorMessage"
        const val KEY_HISTORY = "history"
        const val KEY_MEMORY = "memory"
        const val KEY_HAS_MEMORY = "hasMemory"
        const val KEY_HISTORY_LOG = "historyLog"
    }
}
