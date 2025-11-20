// app/src/main/java/com/example/itrainer/ui/history/HistoryAdapter.kt
package com.example.itrainer.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.itrainer.R
import com.example.itrainer.data.models.GameDistributionWithTeam
import com.example.itrainer.databinding.ItemHistoryBinding

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
            val category = distributionWithTeam.category

            binding.apply {
                // Nombre de la categoría
                categoryChip.text = category?.name?.uppercase() ?: "SIN CATEGORÍA"

                // Nombre del equipo
                teamName.text = team.name

                // Fecha del partido
                gameDate.text = distribution.gameDate

                // Rival
                opponent.text = "vs ${distribution.opponent}"

                // Color según la categoría
                val categoryColor = getCategoryColor(category?.name)
                categoryColorBar.setBackgroundColor(
                    ContextCompat.getColor(root.context, categoryColor)
                )

                // Cambiar también el color del chip
                categoryChip.setBackgroundColor(
                    ContextCompat.getColor(root.context, categoryColor)
                )

                // Click listeners
                root.setOnClickListener { onDistributionClick(distributionWithTeam) }
                root.setOnLongClickListener { onDistributionLongClick(distributionWithTeam) }
            }
        }

        private fun getCategoryColor(categoryName: String?): Int {
            return when (categoryName?.lowercase()) {
                "minibasket" -> R.color.category_minibasket
                "benjamín" -> R.color.category_benjamin
                "infantil y preinf 2ª" -> R.color.category_infantil
                "cadete" -> R.color.category_cadete
                else -> R.color.category_default
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