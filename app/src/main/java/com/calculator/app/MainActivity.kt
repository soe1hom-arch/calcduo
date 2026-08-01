package com.calculator.app

import android.content.Context
import android.content.Intent
import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.LayoutInflater
import android.view.HapticFeedbackConstants
import android.widget.Toast
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.core.net.toUri
import com.google.android.material.color.MaterialColors
import com.calculator.app.data.CalculatorAction
import com.calculator.app.data.CalculatorEngine
import com.calculator.app.databinding.ActivityMainBinding
import com.calculator.app.ui.calculator.CalculatorFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val calculatorFragments = mutableListOf<CalculatorFragment>()
    private var activeTabIndex = 0
    private var isFullMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val savedTheme = getSharedPreferences("calcduo_settings", Context.MODE_PRIVATE).getString("theme", "system") ?: "system"
        applyTheme(savedTheme)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupBackPressed()
        binding.rowDrawerNotes.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, NotesActivity::class.java))
        }
        setupKeyboard()
        setupCalculators()
        setupDisplayTapToCollapse()
    }

    private fun setupCalculators() {
        // Always create exactly 2 permanent calculators
        val frag1 = CalculatorFragment.newInstance(0, getString(R.string.calculator_label_1))
        val frag2 = CalculatorFragment.newInstance(1, getString(R.string.calculator_label_2))
        calculatorFragments.add(frag1)
        calculatorFragments.add(frag2)

        activeTabIndex = 0

        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.panel_tab1, frag1, "tab_0")
        ft.replace(R.id.panel_tab2, frag2, "tab_1")
        ft.commitAllowingStateLoss()

        // Tab selection listeners
        frag1.setOnTabSelectedListener { tabId -> selectTab(tabId) }
        frag2.setOnTabSelectedListener { tabId -> selectTab(tabId) }

        updateLayout()
    }

    // ─────────────── Layout ───────────────

    private fun updateLayout() {
        val ctx: android.content.Context = this
        val ac = MaterialColors.getColor(ctx, com.google.android.material.R.attr.colorPrimaryContainer, 0)
        val ic = MaterialColors.getColor(ctx, com.google.android.material.R.attr.colorSurfaceVariant, 0)
        val ba = GradientDrawable().apply { setStroke(3, ac); setColor(ic) }
        val bi = GradientDrawable().apply { setStroke(0, 0); setColor(ic) }
        binding.panelTab1.background = if (activeTabIndex == 0) ba else bi
        binding.panelTab2.background = if (activeTabIndex == 1) ba else bi
    }

    // ─────────────── Keyboard ───────────────

    private fun setupKeyboard() {
        val actions: List<Pair<View?, CalculatorAction>> = listOf(
            binding.keyboard.btn0 to CalculatorAction.Number("0"),
            binding.keyboard.btn1 to CalculatorAction.Number("1"),
            binding.keyboard.btn2 to CalculatorAction.Number("2"),
            binding.keyboard.btn3 to CalculatorAction.Number("3"),
            binding.keyboard.btn4 to CalculatorAction.Number("4"),
            binding.keyboard.btn5 to CalculatorAction.Number("5"),
            binding.keyboard.btn6 to CalculatorAction.Number("6"),
            binding.keyboard.btn7 to CalculatorAction.Number("7"),
            binding.keyboard.btn8 to CalculatorAction.Number("8"),
            binding.keyboard.btn9 to CalculatorAction.Number("9"),
            binding.keyboard.btnAdd to CalculatorAction.Operator("+"),
            binding.keyboard.btnSubtract to CalculatorAction.Operator("-"),
            binding.keyboard.btnMultiply to CalculatorAction.Operator("×"),
            binding.keyboard.btnDivide to CalculatorAction.Operator("÷"),
            binding.keyboard.btnAc to CalculatorAction.Clear,
            binding.keyboard.btnEquals to CalculatorAction.Equals,
            binding.keyboard.btnDecimal to CalculatorAction.Decimal,
            binding.keyboard.btnBackspace to CalculatorAction.Backspace,
            binding.keyboard.btnToggleSign to CalculatorAction.ToggleSign,
            binding.keyboard.btnPercent to CalculatorAction.Percent,
            binding.keyboard.btnSin to CalculatorAction.Sin,
            binding.keyboard.btnCos to CalculatorAction.Cos,
            binding.keyboard.btnTan to CalculatorAction.Tan,
            binding.keyboard.btnLog to CalculatorAction.Log,
            binding.keyboard.btnLn to CalculatorAction.Ln,
            binding.keyboard.btnSqrt to CalculatorAction.SquareRoot,
            binding.keyboard.btnSquare to CalculatorAction.Square,
            binding.keyboard.btnReciprocal to CalculatorAction.Reciprocal,
            binding.keyboard.btnPower to CalculatorAction.Power,
            binding.keyboard.btnPi to CalculatorAction.Pi,
            binding.keyboard.btnEuler to CalculatorAction.Euler,
            binding.keyboard.btnLeftParen to CalculatorAction.ParenthesisOpen,
            binding.keyboard.btnRightParen to CalculatorAction.ParenthesisClose,
            binding.keyboard.btnDivide2 to CalculatorAction.Operator("÷"),
        )
        actions.forEach { (btn, action) ->
            btn?.setOnClickListener {
                btn?.startAnimation(AnimationUtils.loadAnimation(this, R.anim.btn_press))
                val hapticPref = getSharedPreferences("calcduo_settings", Context.MODE_PRIVATE).getBoolean("haptic", true)
                if (hapticPref) {
                    btn?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
                routeKeyboardAction(action)
            }
        }

        // Toggle mode buttons
        binding.keyboard.btnToggleMode.setOnClickListener {
            toggleKeyboardMode()
        }
        binding.keyboard.btnToggleModeFull.setOnClickListener {
            toggleKeyboardMode()
        }
    }

    private fun routeKeyboardAction(action: CalculatorAction) {
        if (calculatorFragments.isEmpty()) return
        val idx = activeTabIndex.coerceIn(0, calculatorFragments.size - 1)
        calculatorFragments[idx].performAction(action)
    }

    private fun toggleKeyboardMode() {
        isFullMode = !isFullMode
        binding.keyboard.row1Standard.visibility = if (isFullMode) View.GONE else View.VISIBLE
        binding.keyboard.row1Full.visibility = if (isFullMode) View.VISIBLE else View.GONE
        binding.keyboard.fullModeSection.visibility = if (isFullMode) View.VISIBLE else View.GONE
        val toggleText = if (isFullMode) "∧" else "∨"
        binding.keyboard.btnToggleMode.text = toggleText
        binding.keyboard.btnToggleModeFull.text = toggleText
    }

    // ─────────────── Tab Selection ───────────────

    private fun selectTab(tabId: Int) {
        val idx = calculatorFragments.indexOfFirst { it.getTabId() == tabId }
        if (idx < 0) return
        activeTabIndex = idx
        updateLayout()
    }

    // ─────────────── Toolbar ───────────────

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        binding.toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_menu)
        binding.toolbar.setNavigationOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.START) }
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_settings -> { showSettings(); true }
                R.id.action_history -> { showHistory(); true }
                R.id.action_clear_all -> { clearAll(); true }
                R.id.action_privacy -> { showPrivacy(); true }
                R.id.action_about -> { showAbout(); true }
                else -> false
            }
        }
    }

    private fun clearAll() = calculatorFragments.forEach { it.clear() }

    @SuppressLint("InflateParams")
    private fun showAbout() {
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_about, null, false)
        dialog.setContentView(view)
        view.findViewById<View>(R.id.btn_about_close).setOnClickListener { dialog.dismiss() }
        view.findViewById<View>(R.id.tv_about_github).setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, "https://github.com/soe1hom-arch/calcduo".toUri())
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.browser_unavailable), Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun showPrivacy() {
        val msg = android.text.Html.fromHtml(getString(R.string.privacy_body), android.text.Html.FROM_HTML_MODE_LEGACY)
        val tv = android.widget.TextView(this).apply {
            text = msg
            movementMethod = android.text.method.LinkMovementMethod.getInstance()
            setPadding(48, 24, 48, 24)
            textSize = 14f
        }
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.menu_privacy))
            .setView(tv)
            .setPositiveButton(getString(R.string.ok), null)
            .show()
    }

    private fun setupBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                else { isEnabled = false; onBackPressedDispatcher.onBackPressed(); isEnabled = true }
            }
        })
    }

    @SuppressLint("InflateParams")
    private fun showSettings() {
        val prefs = getSharedPreferences("calcduo_settings", Context.MODE_PRIVATE)
        val hapticEnabled = prefs.getBoolean("haptic", true)
        val themeMode = prefs.getString("theme", "system") ?: "system"

        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null, false)
        dialog.setContentView(view)
        view.findViewById<View>(R.id.btn_settings_close).setOnClickListener { dialog.dismiss() }

        val chipGroup = view.findViewById<com.google.android.material.chip.ChipGroup>(R.id.chip_group_theme)
        val chipMap = mapOf(
            R.id.chip_theme_system to "system",
            R.id.chip_theme_light to "light",
            R.id.chip_theme_dark to "dark",
            R.id.chip_theme_grey to "grey"
        )
        for ((id, mode) in chipMap) {
            if (mode == themeMode) { chipGroup.check(id); break }
        }
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val newMode = chipMap[checkedIds[0]] ?: "system"
                if (newMode != themeMode) {
                    prefs.edit { putString("theme", newMode) }
                    applyTheme(newMode)
                    recreate()
                }
            }
        }

        val hapticSwitch = view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switch_haptic)
        hapticSwitch.isChecked = hapticEnabled
        hapticSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean("haptic", isChecked) }
        }

        dialog.show()
    }

    private fun applyTheme(mode: String) {
        when (mode) {
            "light" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                setTheme(R.style.Theme_CalculatorApp_Light)
            }
            "dark" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                setTheme(R.style.Theme_CalculatorApp_Dark)
            }
            "grey" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                setTheme(R.style.Theme_CalculatorApp_Grey)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                setTheme(R.style.Theme_CalculatorApp)
            }
        }
    }

    private fun setupDisplayTapToCollapse() {
        binding.containerDisplays.setOnClickListener {
            if (isFullMode) {
                toggleKeyboardMode()
            }
        }
    }

    private fun showHistory() {
        val idx = activeTabIndex.coerceIn(0, calculatorFragments.size - 1)
        val fragment = calculatorFragments.getOrNull(idx)
        val log = fragment?.getHistoryLog() ?: emptyList()
        if (log.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_history), Toast.LENGTH_SHORT).show()
            return
        }
        val displayItems = log.map { entry ->
            val parsed = CalculatorEngine.parseHistoryEntry(entry)
            if (parsed != null) {
                CalculatorEngine.formatExpression(parsed.first) + " = " + CalculatorEngine.formatDisplayNumber(parsed.second)
            } else {
                entry
            }
        }
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.history_title))
            .setItems(displayItems.toTypedArray()) { _, which ->
                val parsed = CalculatorEngine.parseHistoryEntry(log[which])
                if (parsed != null) {
                    fragment?.restoreHistory(parsed.first, parsed.second)
                }
            }
            .setNegativeButton(getString(R.string.history_clear)) { _, _ ->
                fragment?.clearHistory()
            }
            .setPositiveButton(getString(R.string.ok), null)
            .show()
    }
}
