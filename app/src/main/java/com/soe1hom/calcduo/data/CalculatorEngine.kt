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

package com.soe1hom.calcduo.data

import kotlin.math.sqrt
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.tan
import kotlin.math.log10
import kotlin.math.ln
import kotlin.math.absoluteValue
import kotlin.math.PI
import kotlin.math.E
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.Locale

data class CalculatorState(
    val expression: String = "",
    val result: String = "0",
    val isError: Boolean = false,
    val errorMessage: String = "",
    val history: String = "", // shows previous operation
    val memory: Double = 0.0,
    val hasMemory: Boolean = false,
    val historyLog: List<String> = emptyList(),
    val justEvaluated: Boolean = false
)

sealed class CalculatorAction {
    data class Number(val value: String) : CalculatorAction()
    data class Operator(val op: String) : CalculatorAction()
    object Equals : CalculatorAction()
    object Clear : CalculatorAction()
    object Backspace : CalculatorAction()
    object Decimal : CalculatorAction()
    object Percent : CalculatorAction()
    object ToggleSign : CalculatorAction()
    object ParenthesisOpen : CalculatorAction()
    object ParenthesisClose : CalculatorAction()
    object SquareRoot : CalculatorAction()
    object Square : CalculatorAction()
    object Reciprocal : CalculatorAction()
    object Pi : CalculatorAction()
    object Euler : CalculatorAction()
    object Power : CalculatorAction()
    object MemoryClear : CalculatorAction()
    object MemoryRecall : CalculatorAction()
    object MemoryAdd : CalculatorAction()
    object MemorySubtract : CalculatorAction()
    object Sin : CalculatorAction()
    object Cos : CalculatorAction()
    object Tan : CalculatorAction()
    object Log : CalculatorAction()
    object Ln : CalculatorAction()
}

object CalculatorEngine {

    private const val MAX_HISTORY_LOG = 100
    private const val DIVISION_SCALE = 12
    private const val DOUBLE_PRECISION_DIGITS = 12
    private const val MAX_POW_EXPONENT = 10000
    private const val MAX_PLAIN_LENGTH = 30
    private const val MAX_RESULT_LENGTH = 15

    fun processAction(state: CalculatorState, action: CalculatorAction): CalculatorState {
        return try {
            when (action) {
                is CalculatorAction.Number -> handleNumber(state, action.value)
                is CalculatorAction.Decimal -> handleDecimal(state)
                is CalculatorAction.Operator -> handleOperator(state, action.op)
                is CalculatorAction.Equals -> {
                    val result = handleEquals(state)
                    // Append to history log if calculation succeeded
                    if (!result.isError && result.history.isNotEmpty()) {
                        val entry = "${result.history} ${result.result}"
                        result.copy(historyLog = (state.historyLog + entry).takeLast(MAX_HISTORY_LOG))
                    } else result
                }
                is CalculatorAction.Clear -> CalculatorState()
                is CalculatorAction.Backspace -> handleBackspace(state)
                is CalculatorAction.Percent -> handlePercent(state)
                is CalculatorAction.ToggleSign -> handleToggleSign(state)
                is CalculatorAction.ParenthesisOpen -> {
                    val base = if (state.justEvaluated) "" else state.expression
                    state.copy(
                        expression = base + "(",
                        result = "(",
                        justEvaluated = false
                    )
                }
                is CalculatorAction.ParenthesisClose -> {
                    val openCount = state.expression.count { it == '(' }
                    val closeCount = state.expression.count { it == ')' }
                    if (closeCount < openCount) {
                        state.copy(expression = state.expression + ")")
                    } else state
                }
                is CalculatorAction.Sin -> handleUnaryOp(state, "sin")
                is CalculatorAction.Cos -> handleUnaryOp(state, "cos")
                is CalculatorAction.Tan -> handleUnaryOp(state, "tan")
                is CalculatorAction.Log -> handleUnaryOp(state, "log")
                is CalculatorAction.Ln -> handleUnaryOp(state, "ln")
                is CalculatorAction.SquareRoot -> handleUnaryOp(state, "sqrt")
                is CalculatorAction.Square -> handleUnaryOp(state, "sqr")
                is CalculatorAction.Reciprocal -> handleUnaryOp(state, "1/")
                is CalculatorAction.Pi -> {
                    val pi = PI.toString().take(8)
                    if (state.justEvaluated) {
                        state.copy(expression = pi, result = pi, history = "", justEvaluated = false)
                    } else {
                        state.copy(expression = state.expression + pi, result = pi)
                    }
                }
                is CalculatorAction.Euler -> {
                    val euler = E.toString().take(8)
                    if (state.justEvaluated) {
                        state.copy(expression = euler, result = euler, history = "", justEvaluated = false)
                    } else {
                        state.copy(expression = state.expression + euler, result = euler)
                    }
                }
                is CalculatorAction.Power -> handleOperator(state, "^")
                is CalculatorAction.MemoryClear -> state.copy(memory = 0.0, hasMemory = false)
                is CalculatorAction.MemoryRecall -> {
                    if (!state.hasMemory) state
                    else {
                        val memStr = CalculatorEngine.formatPlain(state.memory)
                        if (state.justEvaluated) {
                            state.copy(expression = memStr, result = memStr, history = "", justEvaluated = false)
                        } else {
                            state.copy(
                                expression = state.expression + memStr,
                                result = memStr
                            )
                        }
                    }
                }
                is CalculatorAction.MemoryAdd -> {
                    val currentValue = try { CalculatorEngine.evaluate(state.expression)?.toDouble() ?: 0.0 } catch (e: Exception) { 0.0 }
                    if (currentValue == 0.0 && state.result != "0") {
                        val fallback = state.result.toDoubleOrNull() ?: 0.0
                        state.copy(memory = state.memory + fallback, hasMemory = true)
                    } else {
                        state.copy(memory = state.memory + currentValue, hasMemory = true)
                    }
                }
                is CalculatorAction.MemorySubtract -> {
                    val currentValue = try { CalculatorEngine.evaluate(state.expression)?.toDouble() ?: 0.0 } catch (e: Exception) { 0.0 }
                    if (currentValue == 0.0 && state.result != "0") {
                        val fallback = state.result.toDoubleOrNull() ?: 0.0
                        state.copy(memory = state.memory - fallback, hasMemory = true)
                    } else {
                        state.copy(memory = state.memory - currentValue, hasMemory = true)
                    }
                }
            }
        } catch (e: Exception) {
            state.copy(
                isError = true,
                errorMessage = if (e.message.isNullOrEmpty()) "Error" else e.message!!,
                result = "Error"
            )
        }
    }

    private fun handleNumber(state: CalculatorState, value: String): CalculatorState {
        if (state.isError) return CalculatorState(expression = value, result = value)

        // After a calculation, a new number starts a fresh expression
        if (state.justEvaluated) {
            return state.copy(
                expression = value,
                result = value,
                history = "",
                justEvaluated = false
            )
        }

        val newExpr = state.expression + value

        // Start fresh if we just pressed an operator or result is an operator symbol
        val endsWithOp = state.expression.trimEnd().let { expr ->
                listOf("+", "-", "×", "÷", "^").any { expr.endsWith(it) } }
        val newResult = if (state.result == "0" || state.result == "Error" || endsWithOp) value
        else state.result + value

        return state.copy(expression = newExpr, result = newResult)
    }

    private fun handleDecimal(state: CalculatorState): CalculatorState {
        if (state.isError) return CalculatorState(expression = "0.", result = "0.")

        // After a calculation, start a fresh decimal
        if (state.justEvaluated) {
            return state.copy(expression = "0.", result = "0.", history = "", justEvaluated = false)
        }

        // Get the last number in the expression
        val lastNumber = state.expression.split(Regex("[+\\-×÷^()]")).lastOrNull() ?: ""
        if (lastNumber.contains(".")) return state

        val newExpr = state.expression + "."
        val newResult = if (state.result.contains(".")) state.result else state.result + "."
        return state.copy(expression = newExpr, result = newResult)
    }

    private fun handleOperator(state: CalculatorState, op: String): CalculatorState {
        if (state.isError) return state.copy(isError = false, errorMessage = "", result = "0", justEvaluated = false)

        // After a calculation, continue from the displayed result
        val expr = if (state.justEvaluated) state.result else state.expression.trimEnd()

        // Replace trailing operator
        val cleanedExpr = expr.replace(Regex("[+\\-×÷^]$"), "").trimEnd()
        val newExpr = "$cleanedExpr $op "

        return state.copy(
            expression = newExpr,
            result = state.result,
            history = if (state.result != "0" && state.result != "Error") state.result else state.history,
            justEvaluated = false
        )
    }
    private fun handleEquals(state: CalculatorState): CalculatorState {
        if (state.expression.isBlank() || state.isError) return state

        val result = evaluate(state.expression)

        return state.copy(
            expression = state.expression,
            result = if (result == null) "Error" else formatPlain(result),
            history = state.expression + " =",
            isError = result == null,
            errorMessage = if (result == null) "Cannot divide by zero" else "",
            justEvaluated = true
        )
    }

    private fun handleBackspace(state: CalculatorState): CalculatorState {
        if (state.expression.isEmpty() || state.isError) return CalculatorState()

        val newExpr = state.expression.dropLast(1).trimEnd()
        if (newExpr.isEmpty()) return CalculatorState()

        // Recalculate if we can
        return try {
            if (newExpr.contains(Regex("[+\\-×÷^]"))) {
                val result = evaluate(newExpr)
                state.copy(expression = newExpr, result = if (result == null) "Error" else formatPlain(result), history = "", justEvaluated = false)
            } else {
                state.copy(expression = newExpr, result = newExpr, history = "", justEvaluated = false)
            }
        } catch (e: Exception) {
            state.copy(expression = newExpr, result = newExpr, history = "", justEvaluated = false)
        }
    }

    private fun handlePercent(state: CalculatorState): CalculatorState {
        if (state.expression.isEmpty() || state.isError) return state
        return try {
            val value = evaluate(state.expression)
            val percent = value?.movePointLeft(2) ?: return errorState("Math Error")
            val raw = formatPlain(percent)
            state.copy(
                expression = raw,
                result = raw,
                history = "${state.result} % =",
                justEvaluated = true
            )
        } catch (e: Exception) {
            state
        }
    }

    private fun handleToggleSign(state: CalculatorState): CalculatorState {
        if (state.expression.isEmpty() || state.isError) return state

        val baseExpr = if (state.justEvaluated) state.result else state.expression
        val trimmed = baseExpr.trim()

        // Plain number → toggle sign directly ("5" ↔ "-5")
        if (trimmed.toDoubleOrNull() != null) {
            val negated = if (trimmed.startsWith("-")) trimmed.removePrefix("-") else "-$trimmed"
            return state.copy(expression = negated, result = negated, justEvaluated = false)
        }

        // Wrapped negation → unwrap
        return if (trimmed.startsWith("-(") && trimmed.endsWith(")")) {
            val inner = trimmed.removePrefix("-(").removeSuffix(")")
            state.copy(expression = inner, result = inner, justEvaluated = false)
        } else {
            state.copy(expression = "-($trimmed)", result = "-(${state.result})", justEvaluated = false)
        }
    }

    private fun handleUnaryOp(state: CalculatorState, op: String): CalculatorState {
        if (state.expression.isEmpty() || state.isError) return state
        return try {
            val value = evaluate(state.expression) ?: return errorState("Math Error")
            val result = when (op) {
                "sin" -> fromDouble(sin(Math.toRadians(value.toDouble())))
                "cos" -> fromDouble(cos(Math.toRadians(value.toDouble())))
                "tan" -> {
                    val rad = Math.toRadians(value.toDouble())
                    if (cos(rad).absoluteValue < 1e-15) null else fromDouble(tan(rad))
                }
                "log" -> if (value.signum() > 0) fromDouble(log10(value.toDouble())) else null
                "ln" -> if (value.signum() > 0) fromDouble(ln(value.toDouble())) else null
                "sqrt" -> if (value.signum() >= 0) fromDouble(sqrt(value.toDouble())) else null
                "sqr" -> value.multiply(value)
                "1/" -> if (value.signum() == 0) null
                        else BigDecimal.ONE.divide(value, DIVISION_SCALE, RoundingMode.HALF_UP)
                else -> value
            }
            if (result == null) {
                state.copy(isError = true, errorMessage = "Math Error", result = "Error")
            } else {
                val raw = formatPlain(result)
                state.copy(
                    expression = raw,
                    result = raw,
                    history = "$op(${formatPlain(value)}) =",
                    justEvaluated = true
                )
            }
        } catch (e: Exception) {
            state.copy(isError = true, errorMessage = "Error", result = "Error")
        }
    }

    private fun errorState(message: String = "Error"): CalculatorState =
        CalculatorState(isError = true, errorMessage = message, result = "Error")

    fun evaluate(expression: String): BigDecimal? {
        val sanitized = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("^", "**")
            .replace(" ", "")
            .replace("%", "/100")
            .replace("--", "+")
            .replace("+-", "-")
            .replace("-+", "-")
            .replace("++", "+")

        return try {
            evaluateSimple(sanitized)
        } catch (e: Exception) {
            null
        }
    }

    fun parseHistoryEntry(entry: String): Pair<String, String>? {
        val eq = entry.lastIndexOf(" = ")
        if (eq <= 0 || eq + 3 >= entry.length) return null
        return entry.substring(0, eq) to entry.substring(eq + 3)
    }

    private fun evaluateSimple(expr: String): BigDecimal? {
        var e = expr.trim()
        // Edge case: empty expression
        if (e.isEmpty()) return BigDecimal.ZERO
        // Edge case: just a decimal point
        if (e == ".") return BigDecimal.ZERO
        // Edge case: just operators
        if (e.all { it in "+-×÷^*" }) return BigDecimal.ZERO
        // Edge case: starts with operator (except minus)
        if (e.startsWith("+") || e.startsWith("×") || e.startsWith("÷") || e.startsWith("^") || e.startsWith("*") || e.startsWith("/")) {
            e = "0$e"
        }
        // Handle parentheses recursively
        while (e.contains("(")) {
            val start = e.lastIndexOf('(')
            val end = e.indexOf(')', start)
            if (end == -1) throw IllegalArgumentException("Mismatched parentheses")
            val inner = e.substring(start + 1, end)
            val result = evaluateSimple(inner) ?: return null
            e = e.substring(0, start) + plain(result) + e.substring(end + 1)
        }
        return evaluateTokens(e)
    }

    private fun evaluateTokens(expr: String): BigDecimal? {
        // Handle ** (power) first
        var e = expr
        if (e.contains("**")) {
            val parts = e.split("\\*\\*".toRegex(), 2)
            val base = evaluateTokens(parts[0]) ?: return null
            val exponent = evaluateTokens(parts[1]) ?: return null
            return powBigDecimal(base, exponent)
        }

        // Tokenize expression into numbers and operators
        val ops = mutableListOf<Char>()
        val nums = mutableListOf<BigDecimal>()
        val currentNum = StringBuilder()
        var i = 0

        while (i < e.length) {
            when {
                e[i] == '+' || e[i] == '-' -> {
                    // Check for unary plus/minus (after operator or parenthesis)
                    if (i == 0 || e[i - 1] in "*/(+-") {
                        currentNum.append(e[i])
                        i++
                        continue
                    }
                    if (currentNum.isNotEmpty()) {
                        nums.add(currentNum.toString().toBigDecimal())
                        currentNum.clear()
                    }
                    ops.add(e[i])
                }
                e[i] == '*' || e[i] == '/' -> {
                    if (currentNum.isNotEmpty()) {
                        nums.add(currentNum.toString().toBigDecimal())
                        currentNum.clear()
                    }
                    ops.add(e[i])
                }
                else -> {
                    currentNum.append(e[i])
                }
            }
            i++
        }
        if (currentNum.isNotEmpty()) {
            nums.add(currentNum.toString().toBigDecimal())
        }

        // If no numbers parsed, return null (error)
        if (nums.isEmpty()) return null
        // If no operators, just return the single number
        if (ops.isEmpty()) return nums[0]

        // Truncate trailing ops if numbers are fewer (e.g. "5+" → num=1, ops=1)
        while (nums.size <= ops.size) {
            ops.removeAt(ops.lastIndex)
        }

        // Process * and / first
        var j = 0
        while (j < ops.size) {
            if (ops[j] == '*' || ops[j] == '/') {
                val left = nums[j]
                val right = nums[j + 1]
                if (ops[j] == '/' && right.signum() == 0) return null
                nums[j] = if (ops[j] == '*') left.multiply(right)
                          else left.divide(right, DIVISION_SCALE, RoundingMode.HALF_UP)
                nums.removeAt(j + 1)
                ops.removeAt(j)
            } else {
                j++
            }
        }

        // Process + and -
        var result = nums[0]
        for (k in ops.indices) {
            result = if (ops[k] == '+') result.add(nums[k + 1]) else result.subtract(nums[k + 1])
        }
        return result

    }

    private fun powBigDecimal(base: BigDecimal, exponent: BigDecimal): BigDecimal? {
        if (exponent.signum() == 0) return BigDecimal.ONE
        val expInt = try {
            exponent.intValueExact()
        } catch (e: ArithmeticException) {
            null
        }
        if (expInt != null && expInt >= 0 && expInt <= MAX_POW_EXPONENT) {
            return try {
                base.pow(expInt)
            } catch (e: ArithmeticException) {
                null
            }
        }
        val d = Math.pow(base.toDouble(), exponent.toDouble())
        return if (d.isNaN() || d.isInfinite()) null else fromDouble(d)
    }

    private fun fromDouble(d: Double): BigDecimal? {
        if (d.isNaN() || d.isInfinite()) return null
        return BigDecimal.valueOf(d).round(MathContext(DOUBLE_PRECISION_DIGITS, RoundingMode.HALF_UP))
    }

    private fun plain(value: BigDecimal): String {
        if (value.signum() == 0) return "0"
        val stripped = value.stripTrailingZeros()
        val s = stripped.toPlainString()
        return if (s.length > MAX_PLAIN_LENGTH) toSciString(stripped) else s
    }

    private fun toSciString(value: BigDecimal): String {
        val abs = value.abs()
        val exp = abs.precision() - abs.scale() - 1
        val mantissa = abs.movePointLeft(exp).stripTrailingZeros().toPlainString()
        val sign = if (value.signum() < 0) "-" else ""
        return "$sign$mantissa" + "E$exp"
    }

    fun formatPlain(value: BigDecimal): String = plain(value)

    fun formatPlain(value: Double): String =
        if (value.isNaN() || value.isInfinite()) "Error"
        else if (value == value.toLong().toDouble()) value.toLong().toString()
        else value.toString()

    fun formatExpression(raw: String): String {
        val sb = StringBuilder(raw.length)
        var i = 0
        while (i < raw.length) {
            val c = raw[i]
            if (c.isDigit() || c == '.') {
                val start = i
                while (i < raw.length && (raw[i].isDigit() || raw[i] == '.')) i++
                sb.append(formatNumberToken(raw.substring(start, i)))
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }

    fun formatDisplayNumber(raw: String): String {
        if (raw.isEmpty()) return raw
        if (raw.any { !it.isDigit() && it != '.' && it != '-' }) return raw
        val sign = if (raw.startsWith("-")) "-" else ""
        val body = if (raw.startsWith("-")) raw.substring(1) else raw
        val formatted = sign + formatNumberToken(body)
        return if (formatted.length > 15) raw else formatted
    }

    private fun formatNumberToken(token: String): String {
        val dot = token.indexOf('.')
        val intPart = if (dot >= 0) token.substring(0, dot) else token
        val rest = if (dot >= 0) token.substring(dot) else ""
        return addThousandsSeparator(intPart) + rest
    }

    fun formatResult(value: BigDecimal): String {
        val plainStr = plain(value)
        val parts = plainStr.split(".")
        val formatted = if (parts.size == 2) {
            addThousandsSeparator(parts[0]) + "." + parts[1]
        } else {
            addThousandsSeparator(parts[0])
        }
        return if (formatted.length > MAX_RESULT_LENGTH) formatScientific(value) else formatted
    }

    fun formatResult(value: Double): String = formatResult(BigDecimal.valueOf(value))

    private fun formatScientific(value: BigDecimal): String {
        val d = value.toDouble()
        return if (d.isFinite()) String.format(Locale.US, "%.6e", d) else toSciString(value)
    }

    private fun addThousandsSeparator(num: String): String {
        if (num.length <= 3) return num
        val sb = StringBuilder()
        var count = 0
        for (i in num.length - 1 downTo 0) {
            if (count > 0 && count % 3 == 0) {
                sb.append('.')
            }
            sb.append(num[i])
            count++
        }
        return sb.reverse().toString()
    }
}
