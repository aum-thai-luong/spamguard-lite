package com.spamguard.feature.blocklist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.spamguard.feature.blocklist.databinding.ItemBlockRuleBinding
import com.spamguard.feature.blocklist.model.BlockRuleUiModel

internal class BlockRuleAdapter(
    private val onDeleteClick: (BlockRuleUiModel) -> Unit,
) : ListAdapter<BlockRuleUiModel, BlockRuleAdapter.RuleViewHolder>(RuleDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RuleViewHolder {
        val binding = ItemBlockRuleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RuleViewHolder(binding, onDeleteClick)
    }

    override fun onBindViewHolder(holder: RuleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    internal class RuleViewHolder(
        private val binding: ItemBlockRuleBinding,
        private val onDeleteClick: (BlockRuleUiModel) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(rule: BlockRuleUiModel) = with(binding) {
            val context = root.context
            pattern.text = rule.pattern
            details.text = context.getString(
                R.string.blocklist_rule_details,
                context.getString(rule.matchTypeLabelRes),
                rule.createdText,
            )
            deleteButton.contentDescription = context.getString(R.string.blocklist_delete_rule_description, rule.pattern)
            deleteButton.setOnClickListener { onDeleteClick(rule) }
        }
    }

    private object RuleDiffCallback : DiffUtil.ItemCallback<BlockRuleUiModel>() {
        override fun areItemsTheSame(oldItem: BlockRuleUiModel, newItem: BlockRuleUiModel): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: BlockRuleUiModel, newItem: BlockRuleUiModel): Boolean =
            oldItem == newItem
    }
}
