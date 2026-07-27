package com.calculator.app.data

import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.tan
import kotlin.math.log10
import kotlin.math.ln
import kotlin.math.absoluteValue
import kotlin.math.PI
import kotlin.math.E

data class CalculatorState(
    val expression: String = "",
    val result: String = "0",
    val isError: Boolean = false,
    val errorMessage: String = "",
    val history: String = "", // shows previous operation
    val memory: Double = 0.0,
    val hasMemory: Boolean = false,
    val historyLog: List<String> = emptyList()
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
                        result.copy(historyLog = state.historyLog + entry)
                    } else result
                }
                is CalculatorAction.Clear -> CalculatorState()
                is CalculatorAction.Backspace -> handleBackspace(state)
                is CalculatorAction.Percent -> handlePercent(state)
                is CalculatorAction.ToggleSign -> handleToggleSign(state)
                is CalculatorAction.ParenthesisOpen -> state.copy(
                    expression = state.expression + "(",
                    result = "("
                )
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
                is CalculatorAction.Pi -> state.copy(
                    expression = state.expression + PI.toString().take(8),
                    result = PI.toString().take(8)
                )
                is CalculatorAction.Euler -> state.copy(
                    expression = state.expression + E.toString().take(8),
                    result = E.toString().take(8)
                )
                is CalculatorAction.Power -> handleOperator(state, "^")
                is CalculatorAction.MemoryClear -> state.copy(memory = 0.0, hasMemory = false)
                is CalculatorAction.MemoryRecall -> {
                    if (!state.hasMemory) state
                    else {
                        val memStr = CalculatorEngine.formatResult(state.memory)
                        state.copy(
                            expression = state.expression + memStr,
                            result = memStr
                        )
                    }
                }
                is CalculatorAction.MemoryAdd -> {
                    val currentValue = try { CalculatorEngine.evaluate(state.expression).let { if (it.isNaN()) 0.0 else it } } catch (e: Exception) { 0.0 }
                    if (currentValue == 0.0 && state.result != "0") {
                        val fallback = state.result.toDoubleOrNull() ?: 0.0
                        state.copy(memory = state.memory + fallback, hasMemory = true)
                    } else {
                        state.copy(memory = state.memory + currentValue, hasMemory = true)
                    }
                }
                is CalculatorAction.MemorySubtract -> {
                    val currentValue = try { CalculatorEngine.evaluate(state.expression).let { if (it.isNaN()) 0.0 else it } } catch (e: Exception) { 0.0 }
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

        // Get the last number in the expression
        val lastNumber = state.expression.split(Regex("[+\\-×÷^()]")).lastOrNull() ?: ""
        if (lastNumber.contains(".")) return state

        val newExpr = state.expression + "."
        val newResult = if (state.result.contains(".")) state.result else state.result + "."
        return state.copy(expression = newExpr, result = newResult)
    }

    private fun handleOperator(state: CalculatorState, op: String): CalculatorState {
        if (state.isError) return state.copy(isError = false, errorMessage = "", result = "0")
        val expr = state.expression.trimEnd()

        // Replace trailing operator
        val cleanedExpr = expr.replace(Regex("[+\\-×÷^]$"), "").trimEnd()
        val newExpr = "$cleanedExpr $op "

        return state.copy(
            expression = newExpr,
            result = state.result,
            history = if (state.result != "0" && state.result != "Error") state.result else state.history,
        )
    }
    private fun handleEquals(state: CalculatorState): CalculatorState {
        if (state.expression.isBlank() || state.isError) return state

        val result = evaluate(state.expression)
        val formatted = formatResult(result)

        return state.copy(
            expression = state.expression,
            result = formatted,
            history = state.expression + " =",
            isError = result.isNaN() || result.isInfinite(),
            errorMessage = if (result.isNaN() || result.isInfinite()) "Cannot divide by zero" else ""
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
                state.copy(expression = newExpr, result = formatResult(result))
            } else {
                state.copy(expression = newExpr, result = newExpr)
            }
        } catch (e: Exception) {
            state.copy(expression = newExpr, result = newExpr)
        }
    }

    private fun handlePercent(state: CalculatorState): CalculatorState {
        if (state.expression.isEmpty() || state.isError) return state
        return try {
            val value = evaluate(state.expression)
            val percent = value / 100.0
            state.copy(
                expression = state.expression + "%",
                result = formatResult(percent)
            )
        } catch (e: Exception) {
            state
        }
    }

    private fun handleToggleSign(state: CalculatorState): CalculatorState {
        if (state.expression.isEmpty() || state.isError) return state
        // Simple toggle: wrap in negation if not already
        return if (state.expression.startsWith("-(") && state.expression.endsWith(")")) {
            val inner = state.expression.removePrefix("-(").removeSuffix(")")
            state.copy(expression = inner, result = inner)
        } else {
            state.copy(expression = "-(${state.expression})", result = "-(${state.result})")
        }
    }

    private fun handleUnaryOp(state: CalculatorState, op: String): CalculatorState {
        if (state.expression.isEmpty() || state.isError) return state
        return try {
            val value = evaluate(state.expression)
            val result = when (op) {
                "sin" -> sin(Math.toRadians(value))
                "cos" -> cos(Math.toRadians(value))
                "tan" -> {
                    val rad = Math.toRadians(value)
                    if (cos(rad).absoluteValue < 1e-15) Double.NaN else tan(rad)
                }
                "log" -> if (value > 0) log10(value) else Double.NaN
                "ln" -> if (value > 0) ln(value) else Double.NaN
                "sqrt" -> if (value >= 0) sqrt(value) else Double.NaN
                "sqr" -> value * value
                "1/" -> if (value != 0.0) 1.0 / value else Double.NaN
                else -> value
            }
            if (result.isNaN() || result.isInfinite()) {
                state.copy(isError = true, errorMessage = "Math Error", result = "Error")
            } else {
                state.copy(
                    expression = state.expression,
                    result = formatResult(result),
                    history = "$op(${formatResult(value)}) ="
                )
            }
        } catch (e: Exception) {
            state.copy(isError = true, errorMessage = "Error", result = "Error")
        }
    }

    fun evaluate(expression: String): Double {
        val sanitized = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("^", "**")
            .replace(" ", "")
            .replace("--", "+")
            .replace("+-", "-")
            .replace("-+", "-")
            .replace("++", "+")

        return try {
            evaluateSimple(sanitized)
        } catch (e: Exception) {
            Double.NaN
        }
    }

    private fun evaluateSimple(expr: String): Double {
        var e = expr.trim()
        // Edge case: empty expression
        if (e.isEmpty()) return 0.0
        // Edge case: just a decimal point
        if (e == ".") return 0.0
        // Edge case: just operators
        if (e.all { it in "+-×÷^*" }) return 0.0
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
            val result = evaluateSimple(inner)
            e = e.substring(0, start) + result.toString() + e.substring(end + 1)
        }
        return evaluateTokens(e)
    }

    private fun evaluateTokens(expr: String): Double {
        // Handle ** (power) first
        var e = expr
        if (e.contains("**")) {
            val parts = e.split("\\*\\*".toRegex(), 2)
            return evaluateTokens(parts[0]).pow(evaluateTokens(parts[1]))
        }

        // Tokenize expression into numbers and operators
        val ops = mutableListOf<Char>()
        val nums = mutableListOf<Double>()
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
                        nums.add(currentNum.toString().toDouble())
                        currentNum.clear()
                    }
                    ops.add(e[i])
                }
                e[i] == '*' || e[i] == '/' -> {
                    if (currentNum.isNotEmpty()) {
                        nums.add(currentNum.toString().toDouble())
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
            nums.add(currentNum.toString().toDouble())
        }

        // If no numbers parsed, return NaN
        if (nums.isEmpty()) return Double.NaN
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
                val result = if (ops[j] == '*') left * right else left / right
                nums[j] = result
                nums.removeAt(j + 1)
                ops.removeAt(j)
            } else {
                j++
            }
        }

        // Process + and -
        var result = nums[0]
        for (k in ops.indices) {
            if (ops[k] == '+') result += nums[k + 1]
            else if (ops[k] == '-') result -= nums[k + 1]
        }
        return result

    }

    fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        if (value == value.toLong().toDouble()) {
            val longVal = value.toLong()
            if (longVal.toString().length > 15) return String.format("%.6e", value)
            return addThousandsSeparator(longVal.toString())
        }
        val s = String.format("%.10f", value).trimEnd('0').trimEnd('.')
        val formatted = if (s.length > 15) String.format("%.6e", value) else s
        // Add thousand separators to the integer part
        val parts = formatted.split(".")
        if (parts.size == 2) {
            return addThousandsSeparator(parts[0]) + "." + parts[1]
        }
        return addThousandsSeparator(formatted)
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
