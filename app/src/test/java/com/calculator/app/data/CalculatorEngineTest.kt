package com.calculator.app.data

import org.junit.Assert.*
import org.junit.Test

class CalculatorEngineTest {

    // ─── Basic Operations ───

    @Test
    fun testNumberInput() {
        val state = CalculatorEngine.processAction(CalculatorState(), CalculatorAction.Number("5"))
        assertEquals("5", state.expression)
        assertEquals("5", state.result)
    }

    @Test
    fun testMultiDigitNumber() {
        val state = CalculatorEngine.processAction(
            CalculatorEngine.processAction(CalculatorState(), CalculatorAction.Number("1")),
            CalculatorAction.Number("5")
        )
        assertEquals("15", state.expression)
        assertEquals("15", state.result)
    }

    @Test
    fun testAddition() {
        var state = CalculatorEngine.processAction(CalculatorState(), CalculatorAction.Number("3"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Operator("+"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Number("4"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Equals)
        assertEquals("3 + 4 =", state.history)
        assertEquals("7", state.result)
    }

    @Test
    fun testSubtraction() {
        var state = CalculatorEngine.processAction(CalculatorState(), CalculatorAction.Number("10"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Operator("-"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Number("3"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Equals)
        assertEquals("7", state.result)
    }

    @Test
    fun testMultiplication() {
        var state = CalculatorEngine.processAction(CalculatorState(), CalculatorAction.Number("7"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Operator("×"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Number("6"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Equals)
        assertEquals("42", state.result)
    }

    @Test
    fun testDivision() {
        var state = CalculatorEngine.processAction(CalculatorState(), CalculatorAction.Number("15"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Operator("÷"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Number("3"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Equals)
        assertEquals("5", state.result)
    }

    @Test
    fun testDivisionByZero() {
        var state = CalculatorEngine.processAction(CalculatorState(), CalculatorAction.Number("5"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Operator("÷"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Number("0"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Equals)
        assertTrue(state.isError)
        assertEquals("Error", state.result)
    }

    // ─── Power ───

    @Test
    fun testPower() {
        var state = CalculatorEngine.processAction(CalculatorState(), CalculatorAction.Number("2"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Power)
        state = CalculatorEngine.processAction(state, CalculatorAction.Number("3"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Equals)
        assertEquals("8", state.result)
    }

    // ─── Scientific Functions ───

    @Test
    fun testSquareRoot() {
        var state = CalculatorEngine.processAction(CalculatorState(), CalculatorAction.Number("16"))
        state = CalculatorEngine.processAction(state, CalculatorAction.SquareRoot)
        assertEquals("4", state.result)
    }

    @Test
    fun testSquare() {
        var state = CalculatorEngine.processAction(CalculatorState(), CalculatorAction.Number("7"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Square)
        assertEquals("49", state.result)
    }

    @Test
    fun testReciprocal() {
        var state = CalculatorEngine.processAction(CalculatorState(), CalculatorAction.Number("4"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Reciprocal)
        assertEquals("0.25", state.result)
    }

    @Test
    fun testPi() {
        val state = CalculatorEngine.processAction(CalculatorState(), CalculatorAction.Pi)
        assertTrue(state.expression.startsWith("3.141592"))
    }

    // ─── Parentheses ───

    @Test
    fun testParentheses() {
        var state = CalculatorState()
        state = CalculatorEngine.processAction(state, CalculatorAction.ParenthesisOpen)
        state = CalculatorEngine.processAction(state, CalculatorAction.Number("2"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Operator("+"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Number("3"))
        state = CalculatorEngine.processAction(state, CalculatorAction.ParenthesisClose)
        state = CalculatorEngine.processAction(state, CalculatorAction.Operator("×"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Number("4"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Equals)
        assertEquals("20", state.result)
    }

    @Test
    fun testUnmatchedParenthesisClosed() {
        var state = CalculatorState()
        state = CalculatorEngine.processAction(state, CalculatorAction.ParenthesisClose)
        // Should not add close paren if no open paren exists
        assertEquals("", state.expression)
    }

    // ─── Special ───

    @Test
    fun testClear() {
        var state = CalculatorEngine.processAction(CalculatorState(), CalculatorAction.Number("42"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Clear)
        assertEquals("", state.expression)
        assertEquals("0", state.result)
    }

    @Test
    fun testBackspace() {
        var state = CalculatorEngine.processAction(CalculatorState(), CalculatorAction.Number("123"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Backspace)
        assertEquals("12", state.expression)
        assertEquals("12", state.result)
    }

    @Test
    fun testDecimal() {
        var state = CalculatorEngine.processAction(CalculatorState(), CalculatorAction.Number("3"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Decimal)
        state = CalculatorEngine.processAction(state, CalculatorAction.Number("14"))
        assertEquals("3.14", state.expression)
        assertEquals("3.14", state.result)
    }

    @Test
    fun testToggleSign() {
        var state = CalculatorEngine.processAction(CalculatorState(), CalculatorAction.Number("5"))
        state = CalculatorEngine.processAction(state, CalculatorAction.ToggleSign)
        assertEquals("-5", state.expression)
        assertEquals("-5", state.result)
    }

    @Test
    fun testPercent() {
        var state = CalculatorEngine.processAction(CalculatorState(), CalculatorAction.Number("200"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Percent)
        assertEquals("2", state.result)
    }

    // ─── After Equals / Unary Commit ───

    @Test
    fun testNumberAfterEqualsStartsFresh() {
        var state = CalculatorEngine.processAction(CalculatorState(), CalculatorAction.Number("3"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Operator("+"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Number("4"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Equals)
        assertEquals("7", state.result)

        state = CalculatorEngine.processAction(state, CalculatorAction.Number("5"))
        assertEquals("5", state.expression)
        assertEquals("5", state.result)
    }

    @Test
    fun testOperatorAfterEqualsContinuesFromResult() {
        var state = CalculatorEngine.processAction(CalculatorState(), CalculatorAction.Number("3"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Operator("+"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Number("4"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Equals)
        assertEquals("7", state.result)

        state = CalculatorEngine.processAction(state, CalculatorAction.Operator("+"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Number("2"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Equals)
        assertEquals("9", state.result)
    }

    @Test
    fun testHistoryLogCappedAt100() {
        var state = CalculatorState()
        repeat(105) {
            state = CalculatorEngine.processAction(state, CalculatorAction.Number("1"))
            state = CalculatorEngine.processAction(state, CalculatorAction.Operator("+"))
            state = CalculatorEngine.processAction(state, CalculatorAction.Number("1"))
            state = CalculatorEngine.processAction(state, CalculatorAction.Equals)
        }
        assertEquals(100, state.historyLog.size)
    }

    @Test
    fun testParseHistoryEntry() {
        val parsed = CalculatorEngine.parseHistoryEntry("3 + 4 = 7")
        assertNotNull(parsed)
        assertEquals("3 + 4", parsed?.first)
        assertEquals("7", parsed?.second)
        assertNull(CalculatorEngine.parseHistoryEntry("not history"))
    }

    @Test
    fun testUnaryOpCommittedToExpression() {
        var state = CalculatorEngine.processAction(CalculatorState(), CalculatorAction.Number("16"))
        state = CalculatorEngine.processAction(state, CalculatorAction.SquareRoot)
        assertEquals("4", state.expression)
        assertEquals("4", state.result)

        state = CalculatorEngine.processAction(state, CalculatorAction.Equals)
        assertEquals("4", state.result)
    }

    @Test
    fun testUnaryOpThenOperatorUsesResult() {
        var state = CalculatorEngine.processAction(CalculatorState(), CalculatorAction.Number("16"))
        state = CalculatorEngine.processAction(state, CalculatorAction.SquareRoot)
        assertEquals("4", state.result)

        state = CalculatorEngine.processAction(state, CalculatorAction.Operator("+"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Number("5"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Equals)
        assertEquals("9", state.result)
    }

    @Test
    fun testPercentThenEquals() {
        var state = CalculatorEngine.processAction(CalculatorState(), CalculatorAction.Number("200"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Percent)
        assertEquals("2", state.result)

        state = CalculatorEngine.processAction(state, CalculatorAction.Equals)
        assertEquals("2", state.result)
    }

    @Test
    fun testPercentThenOperatorUsesResult() {
        var state = CalculatorEngine.processAction(CalculatorState(), CalculatorAction.Number("200"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Percent)
        state = CalculatorEngine.processAction(state, CalculatorAction.Operator("+"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Number("10"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Equals)
        assertEquals("12", state.result)
    }

    @Test
    fun testOperatorPrecedence() {
        // Test: 2 + 3 × 4 = 14 (multiplication first)
        var state = CalculatorState()
        state = CalculatorEngine.processAction(state, CalculatorAction.Number("2"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Operator("+"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Number("3"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Operator("×"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Number("4"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Equals)
        assertEquals("14", state.result)
    }

    @Test
    fun testComplexExpression() {
        // Test: (2 + 3) × (10 - 4) ÷ 2
        var state = CalculatorState()
        state = CalculatorEngine.processAction(state, CalculatorAction.ParenthesisOpen)
        state = CalculatorEngine.processAction(state, CalculatorAction.Number("2"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Operator("+"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Number("3"))
        state = CalculatorEngine.processAction(state, CalculatorAction.ParenthesisClose)
        state = CalculatorEngine.processAction(state, CalculatorAction.Operator("×"))
        state = CalculatorEngine.processAction(state, CalculatorAction.ParenthesisOpen)
        state = CalculatorEngine.processAction(state, CalculatorAction.Number("10"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Operator("-"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Number("4"))
        state = CalculatorEngine.processAction(state, CalculatorAction.ParenthesisClose)
        state = CalculatorEngine.processAction(state, CalculatorAction.Operator("÷"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Number("2"))
        state = CalculatorEngine.processAction(state, CalculatorAction.Equals)
        assertEquals("15", state.result)
    }

    // ─── Evaluate ───

    @Test
    fun testEvaluateSimple() {
        assertEquals(7.0, CalculatorEngine.evaluate("3+4"), 0.001)
    }

    @Test
    fun testEvaluateComplex() {
        assertEquals(20.0, CalculatorEngine.evaluate("(2+3)*4"), 0.001)
    }

    @Test
    fun testEvaluatePower() {
        assertEquals(8.0, CalculatorEngine.evaluate("2^3"), 0.001)
    }

    @Test
    fun testEvaluatePercent() {
        assertEquals(2.0, CalculatorEngine.evaluate("200%"), 0.001)
    }

    // ─── Format ───

    @Test
    fun testFormatInteger() {
        assertEquals("42", CalculatorEngine.formatResult(42.0))
    }

    @Test
    fun testFormatThousandsSeparator() {
        assertEquals("1\u202F234", CalculatorEngine.formatResult(1234.0))
    }

    @Test
    fun testFormatDecimal() {
        assertEquals("3.14", CalculatorEngine.formatResult(3.14))
    }

    @Test
    fun testFormatLargeNumber() {
        val result = CalculatorEngine.formatResult(12345678901234.0)
        assertTrue(result.length <= 15)
    }
}
