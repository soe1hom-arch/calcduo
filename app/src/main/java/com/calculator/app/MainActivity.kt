package com.calculator.app

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.calculator.app.databinding.ActivityMainBinding
import com.calculator.app.ui.adapter.CalculatorTab
import com.calculator.app.ui.adapter.TabAdapter
import com.calculator.app.ui.calculator.CalculatorFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val tabs = mutableListOf<CalculatorTab>()  // max 2: index 0=left, 1=right
    private val calculatorFragments = mutableListOf<CalculatorFragment>()
    private var tabCounter = 0
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

        if (savedInstanceState != null) {
            restoreState(savedInstanceState)
        } else {
            // Start with one calculator on left panel
            addCalculator(0)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("tab_counter", tabCounter)
        val tabIds = tabs.map { it.id }.toIntArray()
        val tabTitles = tabs.map { it.title }.toTypedArray()
        val tabPanels = tabs.map { it.panel }.toIntArray()
        outState.putIntArray("tab_ids", tabIds)
        outState.putStringArray("tab_titles", tabTitles)
        outState.putIntArray("tab_panels", tabPanels)
    }

    private fun restoreState(savedInstanceState: Bundle) {
        tabCounter = savedInstanceState.getInt("tab_counter", 0)
        val tabIds = savedInstanceState.getIntArray("tab_ids") ?: intArrayOf()
        val tabTitles = savedInstanceState.getStringArray("tab_titles") ?: arrayOf()
        val tabPanels = savedInstanceState.getIntArray("tab_panels") ?: intArrayOf()

        val existingFragments =
            supportFragmentManager.fragments.filterIsInstance<CalculatorFragment>()

        for (i in tabIds.indices) {
            val tab = CalculatorTab(
                id = tabIds[i],
                title = tabTitles.getOrElse(i) { "Calc ${i + 1}" },
                panel = tabPanels.getOrElse(i) { i.coerceAtMost(1) }
            )
            tabs.add(tab)
            val fragment = existingFragments.getOrNull(i)
                ?: CalculatorFragment.newInstance(tab.id, tab.title)
            calculatorFragments.add(fragment)
        }

        if (tabs.isEmpty()) {
            addCalculator(0)
        }
        updatePanels()
        refreshSidebar()
    }

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
                    showAboutDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun clearAll() {
        if (tabs.isEmpty()) return
        for (fragment in calculatorFragments) {
            if (fragment.isAdded) {
                supportFragmentManager.beginTransaction()
                    .remove(fragment)
                    .commitNow()
            }
        }
        tabs.clear()
        calculatorFragments.clear()
        tabCounter = 0
        addCalculator(0)
        refreshSidebar()
        updatePanels()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("CalcDuo")
            .setMessage(getString(R.string.about_message))
            .setPositiveButton("OK", null)
            .show()
    }

    private fun setupDrawer() {
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                tabAdapter.submitList(tabs.toList())
                updateEmptySidebar()
            }
        })
    }

    private fun updateEmptySidebar() {
        binding.tvEmptySidebar.visibility = if (tabs.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupSidebar() {
        tabAdapter = TabAdapter(
            onTabClick = { tab ->
                // Tab click activates the tab
                tabs.replaceAll { it.copy(isActive = it.id == tab.id) }
                refreshSidebar()
            },
            onTabClose = { tab ->
                removeCalculator(tab.id)
            },
            onTabLongClick = { tab ->
                showMovePanelDialog(tab)
            }
        )

        binding.rvSidebarTabs.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = tabAdapter
        }

        binding.fabAddCalculator.setOnClickListener { showAddPanelMenu() }
    }

    private fun showAddPanelMenu() {
        val hasLeft = tabs.any { it.panel == 0 }
        val hasRight = tabs.any { it.panel == 1 }

        if (hasLeft && hasRight) {
            Toast.makeText(this, "Max 2 calculators (one per panel)", Toast.LENGTH_SHORT).show()
            return
        }

        val options = mutableListOf<String>().apply {
            if (!hasLeft) add("Add to Left Panel")
            if (!hasRight) add("Add to Right Panel")
        }

        AlertDialog.Builder(this)
            .setTitle("Add Calculator")
            .setItems(options.toTypedArray()) { _, which ->
                when (options[which]) {
                    "Add to Left Panel" -> {
                        addCalculator(0)
                        binding.drawerLayout.closeDrawer(GravityCompat.START)
                    }
                    "Add to Right Panel" -> {
                        addCalculator(1)
                        binding.drawerLayout.closeDrawer(GravityCompat.START)
                    }
                }
            }
            .show()
    }

    private fun addCalculator(panel: Int) {
        // Max 2 tabs, one per panel
        if (tabs.any { it.panel == panel }) return
        if (tabs.size >= 2) return

        val tabId = tabCounter++
        val tab = CalculatorTab(id = tabId, title = "Calc $tabCounter", panel = panel)
        tabs.add(tab)

        val fragment = CalculatorFragment.newInstance(tabId, tab.title)
        calculatorFragments.add(fragment)

        updatePanels()
        refreshSidebar()
    }

    private fun removeCalculator(tabId: Int) {
        val idx = tabs.indexOfFirst { it.id == tabId }
        if (idx < 0) return

        val removedTab = tabs.removeAt(idx)
        val removed = calculatorFragments.removeAt(idx)

        if (removed.isAdded) {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade_out, R.anim.fade_in)
                .remove(removed)
                .commit()
        }

        updatePanels()
        refreshSidebar()
    }

    private fun showMovePanelDialog(tab: CalculatorTab) {
        val currentPanel = if (tab.panel == 0) "Left" else "Right"
        val targetPanel = if (tab.panel == 0) "Right" else "Left"
        val targetCode = if (tab.panel == 0) 1 else 0

        // Cek apakah panel tujuan sudah terisi
        if (tabs.any { it.panel == targetCode }) {
            Toast.makeText(this, "Panel $targetPanel is already occupied.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Move Tab")
            .setMessage("Move "${tab.title}" from [$currentPanel] to [$targetPanel] panel?")
            .setPositiveButton("Move to $targetPanel") { _, _ ->
                moveTab(tab.id, targetCode)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun moveTab(tabId: Int, targetPanel: Int) {
        val idx = tabs.indexOfFirst { it.id == tabId }
        if (idx < 0) return

        // Update panel assignment
        tabs[idx] = tabs[idx].copy(panel = targetPanel)
        updatePanels()
        refreshSidebar()
    }

    private fun refreshSidebar() {
        tabAdapter.submitList(tabs.toList())
        updateEmptySidebar()
    }

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

    private fun updatePanels() {
        val leftTab = tabs.find { it.panel == 0 }
        val rightTab = tabs.find { it.panel == 1 }

        // Update active state
        tabs.replaceAll { tab ->
            tab.copy(isActive = tab.id == leftTab?.id || tab.id == rightTab?.id)
        }

        // Left panel
        if (leftTab != null) {
            val idx = tabs.indexOf(leftTab)
            val frag = calculatorFragments.getOrNull(idx)
            if (frag != null) {
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.panel_left, frag, "panel_left")
                    .commit()
                binding.panelLeft.visibility = View.VISIBLE
                frag.refreshDisplay()
            }
        } else {
            val frag = supportFragmentManager.findFragmentById(R.id.panel_left)
            if (frag != null) {
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.fade_out, R.anim.fade_in)
                    .remove(frag)
                    .commit()
            }
            binding.panelLeft.visibility = View.GONE
        }

        // Right panel
        if (rightTab != null) {
            val idx = tabs.indexOf(rightTab)
            val frag = calculatorFragments.getOrNull(idx)
            if (frag != null) {
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.panel_right, frag, "panel_right")
                    .commit()
                binding.panelRight.visibility = View.VISIBLE
                binding.panelDivider.visibility = View.VISIBLE
                frag.refreshDisplay()
            }
        } else {
            val frag = supportFragmentManager.findFragmentById(R.id.panel_right)
            if (frag != null) {
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.fade_out, R.anim.fade_in)
                    .remove(frag)
                    .commit()
            }
            binding.panelRight.visibility = View.GONE
            binding.panelDivider.visibility = View.GONE
        }

        // Show empty state if both panels empty
        if (leftTab == null && rightTab == null) {
            binding.tvEmptyState.visibility = View.VISIBLE
        } else {
            binding.tvEmptyState.visibility = View.GONE
        }
    }
}
