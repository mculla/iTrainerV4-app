// app/src/main/java/com/example/itrainer/ui/stats/PlayerStatsAdapter.kt
package com.example.itrainer.ui.stats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.itrainer.databinding.ItemPlayerStatsBinding

class PlayerStatsAdapter : ListAdapter<PlayerWithStats, PlayerStatsAdapter.ViewHolder>(StatsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPlayerStatsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemPlayerStatsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playerWithStats: PlayerWithStats) {
            binding.apply {
                playerName.text = "${playerWithStats.player.number} - ${playerWithStats.player.name}"
                totalGames.text = "Partidos: ${playerWithStats.stats.totalGames}"
                totalPeriods.text = "Períodos totales: ${playerWithStats.stats.totalPeriods}"
                averagePeriods.text = "Media: ${String.format("%.1f", playerWithStats.stats.averagePeriodsPerGame)} períodos/partido"
            }
        }
    }

    class StatsDiffCallback : DiffUtil.ItemCallback<PlayerWithStats>() {
        override fun areItemsTheSame(oldItem: PlayerWithStats, newItem: PlayerWithStats): Boolean {
            return oldItem.player.id == newItem.player.id
        }

        override fun areContentsTheSame(oldItem: PlayerWithStats, newItem: PlayerWithStats): Boolean {
            return oldItem == newItem
        }
    }
}