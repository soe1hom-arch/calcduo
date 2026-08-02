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

package com.soe1hom.calcduo

import android.view.View
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalculatorUiTest {

    @Test
    fun launchApp_showsBothCalculators() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.panel_tab1)).check(matches(isDisplayed()))
            onView(withId(R.id.panel_tab2)).check(matches(isDisplayed()))
            assertPanelResult(R.id.panel_tab1, "0")
        }
    }

    @Test
    fun decimalPrecision_showsExactResult() {
        ActivityScenario.launch(MainActivity::class.java).use {
            tap(R.id.btn0)
            tap(R.id.btn_decimal)
            tap(R.id.btn1)
            tap(R.id.btn_add)
            tap(R.id.btn0)
            tap(R.id.btn_decimal)
            tap(R.id.btn2)
            tap(R.id.btn_equals)
            assertPanelResult(R.id.panel_tab1, "0.3")
        }
    }

    @Test
    fun backspaceAfterEquals_deletesOneAtATime() {
        ActivityScenario.launch(MainActivity::class.java).use {
            tap(R.id.btn3)
            tap(R.id.btn_add)
            tap(R.id.btn4)
            tap(R.id.btn_equals)
            assertPanelResult(R.id.panel_tab1, "7")

            tap(R.id.btn_backspace)
            assertPanelResult(R.id.panel_tab1, "3")
        }
    }

    @Test
    fun toggleFullMode_showsScientificKeys() {
        ActivityScenario.launch(MainActivity::class.java).use {
            tap(R.id.btn_toggle_mode)
            onView(withId(R.id.btn_sin)).check(matches(isDisplayed()))
            onView(withId(R.id.btn_power)).check(matches(isDisplayed()))
        }
    }

    private fun tap(viewId: Int) {
        onView(withId(viewId)).perform(click())
    }

    private fun assertPanelResult(panelId: Int, expected: String) {
        onView(withId(panelId)).check { view, noViewFound ->
            if (noViewFound != null) throw noViewFound
            val resultView = view.findViewById<TextView>(R.id.tv_result)
            assertEquals(expected, resultView.text.toString())
        }
    }
}
