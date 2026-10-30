package com.spamguard.feature.calllog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.spamguard.feature.calllog.databinding.ItemCallBinding
import com.spamguard.feature.calllog.model.CallUiModel

internal class CallLogAdapter : ListAdapter<CallUiModel, CallLogAdapter.CallViewHolder>(CallDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallViewHolder {
        val binding = ItemCallBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CallViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CallViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    internal class CallViewHolder(
        private val binding: ItemCallBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(call: CallUiModel) = with(binding) {
            val context = root.context
            callTypeIcon.setImageResource(call.typeIconRes)
            callTypeIcon.contentDescription = context.getString(call.typeLabelRes)
            title.text = call.title ?: context.getString(R.string.call_log_private_number)
            subtitle.isVisible = call.subtitle != null
            subtitle.text = call.subtitle
            time.text = call.timeText
            blockedLabel.isVisible = call.isBlocked
            blockedLabel.text = call.blockedPattern?.let { pattern ->
                context.getString(R.string.call_log_blocked_by, pattern)
            }
        }
    }

    private object CallDiffCallback : DiffUtil.ItemCallback<CallUiModel>() {
        override fun areItemsTheSame(oldItem: CallUiModel, newItem: CallUiModel): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: CallUiModel, newItem: CallUiModel): Boolean =
            oldItem == newItem
    }
}
