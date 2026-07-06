package com.calculator.app

import android.os.Bundle
import android.view.View
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
    private val tabs = mutableListOf<CalculatorTab>()
    private val calculatorFragments = mutableListOf<CalculatorFragment>()
    private var activeIndex = 0
    private var tabCounter = 0
    private lateinit var tabAdapter: TabAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupDrawer()
        setupSidebar()
        setupBackPressed()

        if (savedInstanceState != null) {
            restoreState(savedInstanceState)
        } else {
            addCalculator()
            addCalculator()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("tab_counter", tabCounter)
        outState.putInt("active_index", activeIndex)
        val tabIds = tabs.map { it.id }.toIntArray()
        val tabTitles = tabs.map { it.title }.toTypedArray()
        outState.putIntArray("tab_ids", tabIds)
        outState.putStringArray("tab_titles", tabTitles)
    }

    private fun restoreState(savedInstanceState: Bundle) {
        tabCounter = savedInstanceState.getInt("tab_counter", 0)
        activeIndex = savedInstanceState.getInt("active_index", 0)
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
            addCalculator()
            addCalculator()
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
        activeIndex = 0
        addCalculator()
        addCalculator()
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
                val idx = tabs.indexOfFirst { it.id == tab.id }
                if (idx >= 0) {
                    activeIndex = idx
                    tabs.replaceAll { it.copy(isActive = it.id == tab.id) }
                    updatePanels()
                }
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            },
            onTabClose = { tab ->
                if (tabs.size > 1) removeCalculator(tab.id)
            }
        )

        binding.rvSidebarTabs.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = tabAdapter
        }

        binding.fabAddCalculator.setOnClickListener {
            addCalculator()
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun addCalculator() {
        val tabId = tabCounter++
        val tab = CalculatorTab(id = tabId, title = "Calc $tabCounter")
        tabs.add(tab)
        activeIndex = tabs.size - 1

        val fragment = CalculatorFragment.newInstance(tabId, tab.title)
        calculatorFragments.add(fragment)

        updatePanels()
        refreshSidebar()
    }

    private fun removeCalculator(tabId: Int) {
        val idx = tabs.indexOfFirst { it.id == tabId }
        if (idx < 0 || tabs.size <= 1) return

        tabs.removeAt(idx)
        val removed = calculatorFragments.removeAt(idx)

        if (removed.isAdded) {
            supportFragmentManager.beginTransaction()
                .remove(removed)
                .commitNowAllowingStateLoss()
        }

        if (activeIndex >= tabs.size) activeIndex = tabs.size - 1

        updatePanels()
        refreshSidebar()
    }

    private fun refreshSidebar() {
        tabAdapter.submitList(tabs.toList())
        updateEmptySidebar()
    }

    private fun updatePanels() {
        if (calculatorFragments.isEmpty()) {
            binding.panelLeft.visibility = View.GONE
            binding.panelRight.visibility = View.GONE
            binding.panelDivider.visibility = View.GONE
            binding.tvEmptyState.visibility = View.VISIBLE
            return
        }

        binding.tvEmptyState.visibility = View.GONE

        val allFragments = calculatorFragments.toList()
        val idx = activeIndex.coerceIn(0, allFragments.size - 1)

        val showList = when {
            allFragments.size == 1 -> listOf(allFragments[0])
            idx == allFragments.size - 1 -> listOf(allFragments[idx - 1], allFragments[idx])
            else -> listOf(allFragments[idx], allFragments[idx + 1])
        }

        val visibleFragmentIds = showList.map { it.hashCode() }.toSet()
        tabs.replaceAll { tab ->
            val ti = tabs.indexOf(tab)
            val frag = if (ti < calculatorFragments.size) calculatorFragments[ti] else null
            tab.copy(isActive = frag != null && frag.hashCode() in visibleFragmentIds)
        }

        for (f in allFragments) {
            if (f !in showList && f.isAdded) {
                supportFragmentManager.beginTransaction()
                    .remove(f)
                    .commitNowAllowingStateLoss()
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.panel_left, showList[0], "panel_left")
            .commitNowAllowingStateLoss()
        binding.panelLeft.visibility = View.VISIBLE

        if (showList.size >= 2) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.panel_right, showList[1], "panel_right")
                .commitNowAllowingStateLoss()
            binding.panelRight.visibility = View.VISIBLE
            binding.panelDivider.visibility = View.VISIBLE
        } else {
            val rightFrag = supportFragmentManager.findFragmentById(R.id.panel_right)
            if (rightFrag != null) {
                supportFragmentManager.beginTransaction()
                    .remove(rightFrag)
                    .commitNowAllowingStateLoss()
            }
            binding.panelRight.visibility = View.GONE
            binding.panelDivider.visibility = View.GONE
        }

        for (f in showList) {
            f.refreshDisplay()
        }
    }
}
