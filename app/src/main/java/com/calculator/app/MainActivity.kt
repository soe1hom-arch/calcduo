package com.calculator.app

import android.os.Bundle
import android.view.View
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

        addCalculator()
        addCalculator()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_menu, theme)
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

        val fragment = CalculatorFragment.newInstance(tabId)
        calculatorFragments.add(fragment)

        updatePanels()
        tabAdapter.submitList(tabs.toList())
        updateEmptySidebar()
    }

    private fun removeCalculator(tabId: Int) {
        val idx = tabs.indexOfFirst { it.id == tabId }
        if (idx < 0 || tabs.size <= 1) return

        tabs.removeAt(idx)
        val removed = calculatorFragments.removeAt(idx)

        // Remove from FragmentManager if added
        if (removed.isAdded) {
            supportFragmentManager.beginTransaction()
                .remove(removed)
                .commitNow()
        }

        if (activeIndex >= tabs.size) activeIndex = tabs.size - 1

        updatePanels()
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

        val fragments = calculatorFragments.toList()
        val idx = activeIndex.coerceIn(0, fragments.size - 1)

        val showList = when {
            fragments.size == 1 -> listOf(fragments[0])
            idx == fragments.size - 1 -> listOf(fragments[idx - 1], fragments[idx])
            else -> listOf(fragments[idx], fragments[idx + 1])
        }

        // Mark active tabs
        tabs.replaceAll { tab ->
            val i = tabs.indexOf(tab)
            val isActive = i >= 0 && i < showList.size && tabs[i] == showList.getOrNull(if (i == idx) 0 else 1)
            tab.copy(isActive = isActive)
        }

        // Remove any fragment not in the show list from panels
        for (f in fragments) {
            if (f !in showList && f.isAdded) {
                supportFragmentManager.beginTransaction()
                    .remove(f)
                    .commitNow()
            }
        }

        // Place fragments in panels
        val txn1 = supportFragmentManager.beginTransaction()
        txn1.replace(R.id.panel_left, showList[0], "panel_left")
        txn1.commitNow()

        binding.panelLeft.visibility = View.VISIBLE

        if (showList.size >= 2) {
            val txn2 = supportFragmentManager.beginTransaction()
            txn2.replace(R.id.panel_right, showList[1], "panel_right")
            txn2.commitNow()
            binding.panelRight.visibility = View.VISIBLE
            binding.panelDivider.visibility = View.VISIBLE
        } else {
            val rightFrag = supportFragmentManager.findFragmentById(R.id.panel_right)
            if (rightFrag != null) {
                supportFragmentManager.beginTransaction()
                    .remove(rightFrag)
                    .commitNow()
            }
            binding.panelRight.visibility = View.GONE
            binding.panelDivider.visibility = View.GONE
        }

        // Refresh displays
        for (f in showList) {
            f.refreshDisplay()
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
