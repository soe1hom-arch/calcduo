package com.calculator.app

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.calculator.app.databinding.ActivityMainBinding
import com.calculator.app.ui.adapter.CalculatorTab
import com.calculator.app.ui.adapter.TabAdapter
import com.calculator.app.ui.calculator.CalculatorFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val calculatorTabs = mutableListOf<CalculatorTab>()
    private val fragments = mutableListOf<CalculatorFragment>()
    private var activeTabIndex = 0
    private var tabCounter = 0
    private lateinit var tabAdapter: TabAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupDrawer()
        setupSidebarTabs()

        // Start with 2 calculators
        addCalculatorTab()
        addCalculatorTab()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.toolbar.navigationIcon = resources.getDrawable(
            R.drawable.ic_menu, theme
        )
    }

    private fun setupDrawer() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                refreshSidebarList()
            }
        })
    }

    private fun setupSidebarTabs() {
        tabAdapter = TabAdapter(
            onTabClick = { tab -> switchToTab(tab.id) },
            onTabClose = { tab -> removeCalculatorTab(tab.id) }
        )

        binding.rvSidebarTabs.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@MainActivity)
            adapter = tabAdapter
        }

        binding.fabAddCalculator.setOnClickListener {
            addCalculatorTab()
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.tvEmptySidebar.visibility = View.GONE
    }

    @Suppress("DEPRECATION")
    private fun refreshSidebarList() {
        tabAdapter.submitList(calculatorTabs.toList())
        val isEmpty = calculatorTabs.isEmpty()
        binding.tvEmptySidebar.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun addCalculatorTab() {
        val tabId = tabCounter++
        val tab = CalculatorTab(id = tabId, title = "Calc $tabCounter")
        calculatorTabs.add(tab)
        refreshSidebarList()

        val fragment = CalculatorFragment.newInstance(tabId)
        fragments.add(fragment)

        updateSplitScreen()
    }

    private fun removeCalculatorTab(tabId: Int) {
        val index = calculatorTabs.indexOfFirst { it.id == tabId }
        if (index == -1 || calculatorTabs.size <= 1) return

        calculatorTabs.removeAt(index)
        val removedFragment = fragments.removeAt(index)

        supportFragmentManager.beginTransaction()
            .remove(removedFragment)
            .commitAllowingStateLoss()

        if (activeTabIndex >= calculatorTabs.size) {
            activeTabIndex = calculatorTabs.size - 1
        }

        refreshSidebarList()
        updateSplitScreen()
    }

    private fun switchToTab(tabId: Int) {
        val index = calculatorTabs.indexOfFirst { it.id == tabId }
        if (index == -1) return

        activeTabIndex = index
        calculatorTabs.replaceAll { it.copy(isActive = it.id == tabId) }
        refreshSidebarList()
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun updateSplitScreen() {
        val visibleFragments = fragments.toList()
        val container = binding.llSplitContainer
        container.removeAllViews()

        if (visibleFragments.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            return
        }
        binding.tvEmptyState.visibility = View.GONE

        val showIndex = if (activeTabIndex < visibleFragments.size) activeTabIndex else 0

        val fragmentsToShow = when {
            visibleFragments.size == 1 -> listOf(visibleFragments[0])
            showIndex == visibleFragments.size - 1 -> listOf(visibleFragments[showIndex - 1], visibleFragments[showIndex])
            else -> listOf(visibleFragments[showIndex], visibleFragments[showIndex + 1])
        }

        for ((i, fragment) in fragmentsToShow.withIndex()) {
            if (!fragment.isAdded) {
                supportFragmentManager.beginTransaction()
                    .add(binding.flFragmentContainer.id, fragment, "calc_${fragment.hashCode()}")
                    .commitAllowingStateLoss()
            }

            supportFragmentManager.beginTransaction()
                .show(fragment)
                .commitAllowingStateLoss()

            val fragmentView = fragment.requireView()
            if (fragmentView.parent != null) {
                (fragmentView.parent as ViewGroup).removeView(fragmentView)
            }

            val wrapper = layoutInflater.inflate(R.layout.item_calculator_panel, container, false) as ViewGroup
            // Add fragment view to the panel content area
            val panelContent = wrapper.findViewById<ViewGroup>(R.id.panel_content)
            panelContent.addView(fragmentView, 0)

            val divider = wrapper.findViewById<View>(R.id.divider)
            divider.visibility = if (i == 0 && fragmentsToShow.size > 1) View.VISIBLE else View.GONE

            container.addView(wrapper)
        }

        // Hide unused fragments
        for (fragment in fragments) {
            if (fragment !in fragmentsToShow && fragment.isAdded) {
                supportFragmentManager.beginTransaction()
                    .hide(fragment)
                    .commitAllowingStateLoss()
            }
        }

        visibleFragments.forEach { it.refreshDisplay() }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        for (fragment in fragments) {
            if (fragment.isAdded) {
                supportFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
            }
        }
    }
}
