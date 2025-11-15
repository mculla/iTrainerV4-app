// app/src/main/java/com/example/itrainer/ui/distribution/PeriodsAdapter.kt
package com.example.itrainer.ui.distribution

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.itrainer.data.entities.Player
import com.example.itrainer.databinding.ItemPeriodBinding
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PeriodsAdapter(
    private val onPlayersSelected: (Int, List<Player>) -> Unit
) : ListAdapter<Pair<Int, List<Player>>, PeriodsAdapter.PeriodViewHolder>(PeriodDiffCallback()) {

    private var periodCount: Int = 0
    private var availablePlayers: List<Player> = emptyList()

    fun setPeriodCount(count: Int) {
        periodCount = count
        val currentList = currentList.toMutableList()
        // Actualizar la lista si es necesario
        if (currentList.size != count) {
            val newList = (1..count).map { period ->
                currentList.find { it.first == period } ?: Pair(period, emptyList())
            }
            submitList(newList)
        }
    }

    fun setAvailablePlayers(players: List<Player>) {
        availablePlayers = players
        // Forzar una actualización de la vista
        submitList(currentList.toList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeriodViewHolder {
        return PeriodViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: PeriodViewHolder, position: Int) {
        val (period, players) = getItem(position)
        holder.bind(period, players, availablePlayers) { selectedPlayers ->
            onPlayersSelected(period, selectedPlayers)
        }
    }

    class PeriodViewHolder private constructor(
        private val binding: ItemPeriodBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            period: Int,
            selectedPlayers: List<Player>,
            availablePlayers: List<Player>,
            onSelectionChanged: (List<Player>) -> Unit
        ) {
            binding.periodTitle.text = "Período $period"
            setupPlayerChips(selectedPlayers, availablePlayers, onSelectionChanged)
        }

        private fun setupPlayerChips(
            selectedPlayers: List<Player>,
            availablePlayers: List<Player>,
            onSelectionChanged: (List<Player>) -> Unit
        ) {
            binding.periodPlayerChips.removeAllViews()

            // Agregar chip para añadir jugador
            val addPlayerChip = Chip(binding.root.context).apply {
                text = "+"
                setOnClickListener {
                    showPlayerSelectionDialog(
                        binding.root.context,
                        availablePlayers,
                        selectedPlayers
                    ) { newSelection ->
                        onSelectionChanged(newSelection)
                    }
                }
            }
            binding.periodPlayerChips.addView(addPlayerChip)

            // Mostrar jugadores seleccionados
            selectedPlayers.forEach { player ->
                val chip = Chip(binding.root.context).apply {
                    text = "${player.number} - ${player.name}"
                    isCloseIconVisible = true
                    setOnCloseIconClickListener {
                        val newSelection = selectedPlayers.filter { it != player }
                        onSelectionChanged(newSelection)
                    }
                }
                binding.periodPlayerChips.addView(chip)
            }
        }

        private fun showPlayerSelectionDialog(
            context: Context,
            availablePlayers: List<Player>,
            currentlySelected: List<Player>,
            onPlayersSelected: (List<Player>) -> Unit
        ) {
            val availableForSelection = availablePlayers.filter { it !in currentlySelected }
            val playerNames = availableForSelection.map { "${it.number} - ${it.name}" }.toTypedArray()

            MaterialAlertDialogBuilder(context)
                .setTitle("Seleccionar jugador")
                .setItems(playerNames) { _, index ->
                    val selectedPlayer = availableForSelection[index]
                    onPlayersSelected(currentlySelected + selectedPlayer)
                }
                .show()
        }

        companion object {
            fun from(parent: ViewGroup): PeriodViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ItemPeriodBinding.inflate(inflater, parent, false)
                return PeriodViewHolder(binding)
            }
        }
    }

    class PeriodDiffCallback : DiffUtil.ItemCallback<Pair<Int, List<Player>>>() {
        override fun areItemsTheSame(
            oldItem: Pair<Int, List<Player>>,
            newItem: Pair<Int, List<Player>>
        ): Boolean {
            return oldItem.first == newItem.first
        }

        override fun areContentsTheSame(
            oldItem: Pair<Int, List<Player>>,
            newItem: Pair<Int, List<Player>>
        ): Boolean {
            return oldItem == newItem
        }
    }
}