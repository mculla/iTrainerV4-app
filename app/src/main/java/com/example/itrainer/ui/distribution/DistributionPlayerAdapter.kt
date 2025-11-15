// app/src/main/java/com/example/itrainer/ui/distribution/DistributionPlayerAdapter.kt
package com.example.itrainer.ui.distribution

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.itrainer.data.entities.Player
import com.example.itrainer.databinding.ItemPlayerDistributionBinding
import com.google.android.material.chip.Chip

class DistributionPlayerAdapter(
    private val onPlayerClick: (Player, Boolean) -> Unit,
    private var currentPeriod: Int
) : ListAdapter<DistributionPlayerAdapter.PlayerWithPeriods, DistributionPlayerAdapter.ViewHolder>(PlayerDiffCallback()) {

    data class PlayerWithPeriods(
        val player: Player,
        val periodsPlayed: List<Int>,
        val isSelectedInCurrentPeriod: Boolean = false
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPlayerDistributionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onPlayerClick, currentPeriod)
    }

    fun updateCurrentPeriod(period: Int) {
        currentPeriod = period
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ItemPlayerDistributionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: PlayerWithPeriods,
            onPlayerClick: (Player, Boolean) -> Unit,
            currentPeriod: Int
        ) {
            val player = item.player
            binding.playerNumber.text = "#${player.number}"
            binding.playerName.text = player.name

            // Resaltar si está seleccionado en el período actual
            itemView.isSelected = item.isSelectedInCurrentPeriod

            // Mostrar períodos en los que juega
            binding.periodsPlayed.removeAllViews()
            item.periodsPlayed.forEach { period ->
                addPeriodChip(period)
            }

            itemView.setOnClickListener {
                onPlayerClick(player, !item.isSelectedInCurrentPeriod)
            }
        }

        private fun addPeriodChip(period: Int) {
            val chip = Chip(binding.root.context).apply {
                text = period.toString()
                isClickable = false
                isChecked = true
            }
            binding.periodsPlayed.addView(chip)
        }
    }

    class PlayerDiffCallback : DiffUtil.ItemCallback<PlayerWithPeriods>() {
        override fun areItemsTheSame(oldItem: PlayerWithPeriods, newItem: PlayerWithPeriods): Boolean {
            return oldItem.player.id == newItem.player.id
        }

        override fun areContentsTheSame(oldItem: PlayerWithPeriods, newItem: PlayerWithPeriods): Boolean {
            return oldItem == newItem
        }
    }
}
