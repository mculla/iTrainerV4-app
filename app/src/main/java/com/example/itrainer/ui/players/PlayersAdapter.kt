// app/src/main/java/com/example/itrainer/ui/players/PlayersAdapter.kt
package com.example.itrainer.ui.players

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.itrainer.data.entities.Player
import com.example.itrainer.databinding.ItemPlayerBinding

class PlayersAdapter(
    private val onPlayerClick: (Player) -> Unit,
    private val onPlayerLongClick: (Player) -> Boolean
) : ListAdapter<Player, PlayersAdapter.PlayerViewHolder>(PlayerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        return PlayerViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(getItem(position), onPlayerClick, onPlayerLongClick)
    }

    class PlayerViewHolder private constructor(
        private val binding: ItemPlayerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(player: Player, onPlayerClick: (Player) -> Unit, onPlayerLongClick: (Player) -> Boolean) {
            binding.playerName.text = player.name
            binding.playerNumber.text = "#${player.number}"

            binding.root.setOnClickListener { onPlayerClick(player) }
            binding.root.setOnLongClickListener { onPlayerLongClick(player) }
        }

        companion object {
            fun from(parent: ViewGroup): PlayerViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ItemPlayerBinding.inflate(inflater, parent, false)
                return PlayerViewHolder(binding)
            }
        }
    }

    class PlayerDiffCallback : DiffUtil.ItemCallback<Player>() {
        override fun areItemsTheSame(oldItem: Player, newItem: Player): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Player, newItem: Player): Boolean {
            return oldItem == newItem
        }
    }
}