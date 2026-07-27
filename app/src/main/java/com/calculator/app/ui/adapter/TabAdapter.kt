package com.calculator.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.calculator.app.databinding.ItemCalculatorTabBinding

data class CalculatorTab(
    val id: Int,
    val title: String = "Calc ${id + 1}"
)

class TabAdapter(
    private val onTabClick: (CalculatorTab) -> Unit,
    private val onTabClose: (CalculatorTab) -> Unit,
    private val onTabRename: ((CalculatorTab) -> Unit)? = null
) : ListAdapter<CalculatorTab, TabAdapter.TabViewHolder>(DiffCallback()) {

    var activeTabId: Int = -1
        set(value) {
            field = value
            notifyDataSetChanged()
        }

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
            binding.tvTabTitle.text = tab.title
            binding.root.setOnClickListener { onTabClick(tab) }
            binding.root.setOnLongClickListener {
                onTabRename?.invoke(tab)
                true
            }
            binding.btnCloseTab.setOnClickListener { onTabClose(tab) }
            
            // Highlight active tab
            val ctx = binding.root.context
            if (tab.id == activeTabId) {
                binding.root.setBackgroundResource(com.calculator.app.R.drawable.bg_tab_active)
                binding.tvTabTitle.setTextColor(
                    com.google.android.material.color.MaterialColors.getColor(ctx, com.google.android.material.R.attr.colorOnSurface, 0)
                )
            } else {
                binding.root.setBackgroundResource(android.R.color.transparent)
                binding.tvTabTitle.setTextColor(
                    com.google.android.material.color.MaterialColors.getColor(ctx, com.google.android.material.R.attr.colorOnSurfaceVariant, 0)
                )
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CalculatorTab>() {
        override fun areItemsTheSame(a: CalculatorTab, b: CalculatorTab) = a.id == b.id
        override fun areContentsTheSame(a: CalculatorTab, b: CalculatorTab) = a == b
    }
}
