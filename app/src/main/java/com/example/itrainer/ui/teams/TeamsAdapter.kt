// app/src/main/java/com/example/itrainer/ui/teams/TeamsAdapter.kt
package com.example.itrainer.ui.teams

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.itrainer.data.entities.Team
import com.example.itrainer.databinding.ItemTeamBinding

class TeamsAdapter(
    private val onTeamClick: (Team) -> Unit,
    private val onTeamLongClick: (Team) -> Boolean,
    private val onManagePlayersClick: (Team) -> Unit
) : ListAdapter<Team, TeamsAdapter.TeamViewHolder>(TeamDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        return TeamViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        val team = getItem(position)
        holder.bind(team, onTeamClick, onTeamLongClick, onManagePlayersClick)
    }

    class TeamViewHolder private constructor(
        private val binding: ItemTeamBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            team: Team,
            onTeamClick: (Team) -> Unit,
            onTeamLongClick: (Team) -> Boolean,
            onManagePlayersClick: (Team) -> Unit
        ) {
            binding.teamName.text = team.name

            binding.root.setOnClickListener { onTeamClick(team) }
            binding.root.setOnLongClickListener { onTeamLongClick(team) }
            binding.managePlayersButton.setOnClickListener { onManagePlayersClick(team) }
        }

        companion object {
            fun from(parent: ViewGroup): TeamViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ItemTeamBinding.inflate(inflater, parent, false)
                return TeamViewHolder(binding)
            }
        }
    }

    class TeamDiffCallback : DiffUtil.ItemCallback<Team>() {
        override fun areItemsTheSame(oldItem: Team, newItem: Team): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Team, newItem: Team): Boolean {
            return oldItem == newItem
        }
    }
}