// app/src/main/java/com/example/itrainer/ui/history/HistoryAdapter.kt
package com.example.itrainer.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.itrainer.data.entities.GameDistribution
import com.example.itrainer.data.models.GameDistributionWithTeam

import com.example.itrainer.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryAdapter(
    private val onDistributionClick: (GameDistributionWithTeam) -> Unit,
    private val onDistributionLongClick: (GameDistributionWithTeam) -> Boolean
) : ListAdapter<GameDistributionWithTeam, HistoryAdapter.ViewHolder>(DistributionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(distributionWithTeam: GameDistributionWithTeam) {
            val distribution = distributionWithTeam.distribution
            val team = distributionWithTeam.team
            binding.apply {
                gameDate.text = distribution.gameDate
                opponent.text = "${team.name} vs ${distribution.opponent}"
                root.setOnClickListener { onDistributionClick(distributionWithTeam) }
                root.setOnLongClickListener { onDistributionLongClick(distributionWithTeam) }
            }
        }
    }

    class DistributionDiffCallback : DiffUtil.ItemCallback<GameDistributionWithTeam>() {
        override fun areItemsTheSame(oldItem: GameDistributionWithTeam, newItem: GameDistributionWithTeam): Boolean {
            return oldItem.distribution.id == newItem.distribution.id
        }

        override fun areContentsTheSame(oldItem: GameDistributionWithTeam, newItem: GameDistributionWithTeam): Boolean {
            return oldItem == newItem
        }
    }
}