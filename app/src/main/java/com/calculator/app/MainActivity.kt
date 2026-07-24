package com.calculator.app

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.calculator.app.data.CalculatorAction
import com.calculator.app.databinding.ActivityMainBinding
import com.calculator.app.ui.adapter.CalculatorTab
import com.calculator.app.ui.adapter.TabAdapter
import com.calculator.app.ui.calculator.CalculatorFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val tabs = mutableListOf<CalculatorTab>()
    private val calculatorFragments = mutableListOf<CalculatorFragment>()
    private var tabCounter = 0
    private var activeTabIndex = 0
    private lateinit var tabAdapter: TabAdapter
    private var notesVisible = false
    private val notesPrefsKey = "calcduo_notes"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupDrawer()
        setupSidebar()
        setupBackPressed()
        setupNotes()
        setupKeyboard()

        if (savedInstanceState != null) {
            restoreState(savedInstanceState)
        } else {
            // Start with 1 calculator tab
            addCalculator(0)
            activeTabIndex = 0
            updatePanels()
            updateActiveTabHighlight()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("tab_counter", tabCounter)
        outState.putInt("active_tab_index", activeTabIndex)
        val tabIds = tabs.map { it.id }.toIntArray()
        val tabTitles = tabs.map { it.title }.toTypedArray()
        outState.putIntArray("tab_ids", tabIds)
        outState.putStringArray("tab_titles", tabTitles)
        outState.putString("notes_text", binding.etNotes.text.toString())
        outState.putBoolean("notes_visible", notesVisible)
    }

    private fun restoreState(savedInstanceState: Bundle) {
        tabCounter = savedInstanceState.getInt("tab_counter", 0)
        activeTabIndex = savedInstanceState.getInt("active_tab_index", 0)
        val tabIds = savedInstanceState.getIntArray("tab_ids") ?: intArrayOf()
        val tabTitles = savedInstanceState.getStringArray("tab_titles") ?: arrayOf()

        val existingFragments =
            supportFragmentManager.fragments.filterIsInstance<CalculatorFragment>()

        for (i in tabIds.indices) {
            val tab = CalculatorTab(
                id = tabIds[i],
                title = tabTitles.getOrElse(i) { "Calc ${i + 1}" }
            )
            tabs.add(tab)
            val fragment = existingFragments.getOrNull(i)
                ?: CalculatorFragment.newInstance(tab.id, tab.title)
            calculatorFragments.add(fragment)
        }

        if (tabs.isEmpty()) {
            addCalculator(0)
            activeTabIndex = 0
        }

        val savedNotes = savedInstanceState.getString("notes_text", "")
        if (savedNotes.isNotEmpty()) {
            binding.etNotes.setText(savedNotes)
        }
        notesVisible = savedInstanceState.getBoolean("notes_visible", false)
        binding.panelNotes.visibility = if (notesVisible) View.VISIBLE else View.GONE
        binding.toolbar.menu.findItem(R.id.action_notes)?.isChecked = notesVisible
        updatePanels()
        updateActiveTabHighlight()
        refreshSidebar()
    }

    // ───────────────── Keyboard Setup ─────────────────

    private fun setupKeyboard() {
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
                routeKeyboardAction(action)
            }
        }
    }

    private fun routeKeyboardAction(action: CalculatorAction) {
        if (calculatorFragments.isEmpty()) return
        val idx = activeTabIndex.coerceIn(0, calculatorFragments.size - 1)
        calculatorFragments[idx].performAction(action)
    }

    // ───────────────── Tab Management ─────────────────

    private fun addCalculator(slotIndex: Int) {
        if (tabs.size >= 2) {
            Toast.makeText(this, "Maximum 2 tabs", Toast.LENGTH_SHORT).show()
            return
        }

        val tabId = tabCounter++
        val tabTitle = "Calc ${tabs.size + 1}"
        val tab = CalculatorTab(id = tabId, title = tabTitle)
        tabs.add(tab)

        val fragment = CalculatorFragment.newInstance(tabId, tabTitle)
        calculatorFragments.add(fragment)

        updatePanels()
        activeTabIndex = tabs.size - 1
        updateActiveTabHighlight()
        refreshSidebar()
    }

    private fun removeCalculator(tabId: Int) {
        if (tabs.size <= 1) {
            Toast.makeText(this, "At least 1 tab required", Toast.LENGTH_SHORT).show()
            return
        }

        val idx = tabs.indexOfFirst { it.id == tabId }
        if (idx < 0) return

        tabs.removeAt(idx)
        val fragment = calculatorFragments.removeAt(idx)

        supportFragmentManager.beginTransaction()
            .remove(fragment)
            .commitAllowingStateLoss()

        if (activeTabIndex >= tabs.size) {
            activeTabIndex = tabs.size - 1
        }
        updatePanels()
        updateActiveTabHighlight()
        refreshSidebar()
    }

    private fun selectTab(tabId: Int) {
        val idx = tabs.indexOfFirst { it.id == tabId }
        if (idx < 0 || idx >= calculatorFragments.size) return
        activeTabIndex = idx
        updateActiveTabHighlight()
    }

    // ───────────────── Panel Management ─────────────────

    private fun updatePanels() {
        // Tab 1 slot
        if (tabs.size >= 1 && calculatorFragments.size >= 1) {
            val frag1 = calculatorFragments[0]
            if (frag1.parentFragment == null && !frag1.isAdded) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.panel_tab1, frag1, "tab_1")
                    .commitAllowingStateLoss()
            }
            binding.panelTab1.visibility = View.VISIBLE
        } else {
            binding.panelTab1.visibility = View.GONE
        }

        // Tab 2 slot
        if (tabs.size >= 2 && calculatorFragments.size >= 2) {
            val frag2 = calculatorFragments[1]
            if (frag2.parentFragment == null && !frag2.isAdded) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.panel_tab2, frag2, "tab_2")
                    .commitAllowingStateLoss()
            }
            binding.panelTab2.visibility = View.VISIBLE
            binding.panelDivider.visibility = View.VISIBLE
        } else {
            binding.panelTab2.visibility = View.GONE
            binding.panelDivider.visibility = View.GONE
        }

        // Update fragment listeners for tab selection
        calculatorFragments.forEach { frag ->
            frag.setOnTabSelectedListener { tabId ->
                selectTab(tabId)
            }
        }

        // Show/hide empty state
        binding.tvEmptyState.visibility =
            if (tabs.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateActiveTabHighlight() {
        val activeColor = ContextCompat.getColor(this, R.color.md_theme_primaryContainer)
        val inactiveColor = ContextCompat.getColor(this, R.color.display_background)

        val borderActive = GradientDrawable().apply {
            setStroke(3, activeColor)
            setColor(inactiveColor)
        }
        val borderInactive = GradientDrawable().apply {
            setStroke(0, 0)
            setColor(inactiveColor)
        }

        binding.panelTab1.background =
            if (activeTabIndex == 0 && tabs.size >= 1) borderActive else borderInactive
        binding.panelTab2.background =
            if (activeTabIndex == 1 && tabs.size >= 2) borderActive else borderInactive
    }

    // ───────────────── Sidebar ─────────────────

    private fun setupDrawer() {
        // drawer setup done in XML
    }

    private fun setupSidebar() {
        tabAdapter = TabAdapter(
            onTabClick = { tab -> selectTab(tab.id) },
            onTabClose = { tab -> removeCalculator(tab.id) }
        )
        binding.rvSidebarTabs.layoutManager = LinearLayoutManager(this)
        binding.rvSidebarTabs.adapter = tabAdapter
        binding.fabAddCalculator.setOnClickListener {
            addCalculator(tabs.size)
        }
    }

    private fun refreshSidebar() {
        tabAdapter.submitList(tabs.toList())
        updateEmptySidebar()
    }

    private fun updateEmptySidebar() {
        binding.tvEmptySidebar.visibility =
            if (tabs.isEmpty()) View.VISIBLE else View.GONE
    }

    // ───────────────── Toolbar ─────────────────

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.toolbar.inflateMenu(R.menu.main_menu)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_clear_all -> {
                    clearAll()
                    true
                }
                R.id.action_notes -> {
                    toggleNotes()
                    true
                }
                R.id.action_about -> {
                    showAbout()
                    true
                }
                else -> false
            }
        }
    }

    private fun clearAll() {
        calculatorFragments.forEach { it.clear() }
    }

    private fun showAbout() {
        AlertDialog.Builder(this)
            .setTitle("About CalcDuo")
            .setMessage(getString(R.string.about_message))
            .setPositiveButton("OK", null)
            .show()
    }

    // ───────────────── Back Press ─────────────────

    private fun setupBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }

    // ───────────────── Notes ─────────────────

    private fun setupNotes() {
        val savedNotes = getSharedPreferences("calcduo_prefs", Context.MODE_PRIVATE)
            .getString(notesPrefsKey, "") ?: ""
        if (savedNotes.isNotEmpty()) {
            binding.etNotes.setText(savedNotes)
        }

        binding.etNotes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                saveNotes(s?.toString() ?: "")
            }
        })

        binding.btnClearNotes.setOnClickListener {
            binding.etNotes.setText("")
            saveNotes("")
        }
    }

    private fun saveNotes(text: String) {
        getSharedPreferences("calcduo_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString(notesPrefsKey, text)
            .apply()
    }

    private fun toggleNotes() {
        notesVisible = !notesVisible
        binding.panelNotes.visibility = if (notesVisible) View.VISIBLE else View.GONE
        updatePanels()
        binding.toolbar.menu.findItem(R.id.action_notes)?.isChecked = notesVisible
    }
}
