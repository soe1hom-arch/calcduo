package com.calculator.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.calculator.app.databinding.ItemCalculatorTabBinding

data class CalculatorTab(
    val id: Int,
    val title: String = "Calc ${id + 1}",
    val isActive: Boolean = false,
    val panel: Int = 0 // 0 = left, 1 = right
)

class TabAdapter(
    private val onTabClick: (CalculatorTab) -> Unit,
    private val onTabClose: (CalculatorTab) -> Unit,
    private val onTabLongClick: (CalculatorTab) -> Unit = {}
) : ListAdapter<CalculatorTab, TabAdapter.TabViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        val binding = ItemCalculatorTabBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TabViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TabViewHolder(
        private val binding: ItemCalculatorTabBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tab: CalculatorTab) {
            val panelLabel = if (tab.panel == 0) "[L]" else "[R]"
            binding.tvTabTitle.text = "$panelLabel ${tab.title}"
            binding.root.isSelected = tab.isActive
            binding.root.setOnClickListener { onTabClick(tab) }
            binding.root.setOnLongClickListener {
                onTabLongClick(tab)
                true
            }
            binding.btnCloseTab.setOnClickListener { onTabClose(tab) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CalculatorTab>() {
        override fun areItemsTheSame(a: CalculatorTab, b: CalculatorTab) = a.id == b.id
        override fun areContentsTheSame(a: CalculatorTab, b: CalculatorTab) = a == b
    }
}
