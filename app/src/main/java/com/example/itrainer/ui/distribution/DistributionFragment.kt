// app/src/main/java/com/example/itrainer/ui/distribution/DistributionFragment.kt
package com.example.itrainer.ui.distribution

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.itrainer.R
import com.example.itrainer.data.entities.Player
import com.example.itrainer.databinding.FragmentDistributionBinding
import com.example.itrainer.databinding.DialogSaveDistributionBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar

class DistributionFragment : Fragment() {
    private var _binding: FragmentDistributionBinding? = null
    private val binding get() = _binding!!

    private var isPlayersExpanded = true
    private var periodsCount = 6

    private val args: DistributionFragmentArgs by navArgs()
    private val viewModel: DistributionViewModel by viewModels {
        DistributionViewModel.Factory(requireActivity().application, args.teamId, args.categoryId)
    }

    private val gridCells = mutableMapOf<Pair<Int, Int>, CheckBox>()

    companion object {
        const val INFANTIL_CATEGORY_ID = 2 // Ajusta este ID según tu base de datos
    }

    private fun showSubstitutionMenu(playerId: Int, period: Int, markerView: TextView) {
        val options = arrayOf("Sale (X)", "Entra (Verde)", "Quitar marca")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cambio en período $period")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> { // Sale
                        markerView.text = "X"
                        markerView.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                        markerView.visibility = View.VISIBLE
                        viewModel.toggleSubstitution(playerId, period, isOut = true)
                    }
                    1 -> { // Entra
                        markerView.text = "↑"
                        markerView.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                        markerView.visibility = View.VISIBLE
                        viewModel.toggleSubstitution(playerId, period, isOut = false)
                    }
                    2 -> { // Quitar
                        markerView.visibility = View.GONE
                        viewModel.removeSubstitute(playerId, period)
                    }
                }
            }
            .show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDistributionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.distribution.collect { distribution ->
                if (periodsCount != distribution.size && distribution.isNotEmpty()) {
                    periodsCount = distribution.size
                    setupGrid()
                }
                updateGridSelection(distribution)
            }
        }

        setupExpandableSection()
        setupGrid()
        observeViewModel()
        setupSaveButton()
    }

    private fun setupGrid() {
        binding.gridLayout.columnCount = periodsCount + 1 // CAMBIAR: 1 para nombres + períodos dinámicos
        binding.headerRow.removeAllViews()
        addHeaderCell("")
        for (period in 1..periodsCount) {
            addHeaderCell("P$period")
        }

        viewModel.availablePlayers.observe(viewLifecycleOwner) { players ->
            binding.gridLayout.removeAllViews()
            players.forEach { player ->
                // Añadir celda con nombre de jugador
                val playerCell = TextView(requireContext()).apply {
                    text = "${player.number} - ${player.name}"
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(16, 8, 8, 8)
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = resources.getDimensionPixelSize(R.dimen.player_name_width)
                        height = resources.getDimensionPixelSize(R.dimen.grid_cell_size)
                        columnSpec = GridLayout.spec(0, 1)
                    }
                }
                binding.gridLayout.addView(playerCell)

                // Añadir checkboxes para los períodos
                for (period in 1..periodsCount) {
                    val cellParams = GridLayout.LayoutParams().apply {
                        width = resources.getDimensionPixelSize(R.dimen.grid_cell_size)
                        height = resources.getDimensionPixelSize(R.dimen.grid_cell_size)
                        setMargins(2, 2, 2, 2)
                    }

                    val isLastPeriod = (period == periodsCount)

                    val cell = if (isLastPeriod && args.categoryId == INFANTIL_CATEGORY_ID) {
                        layoutInflater.inflate(R.layout.grid_cell_substitution, binding.gridLayout, false)
                    } else {
                        layoutInflater.inflate(R.layout.grid_cell, binding.gridLayout, false)
                    }

                    cell.layoutParams = cellParams

                    val checkbox = cell.findViewById<CheckBox>(R.id.checkbox)
                    val markerText = cell.findViewById<TextView>(R.id.substitution_marker)

                    if (isLastPeriod && args.categoryId == INFANTIL_CATEGORY_ID) {
                        // Lógica especial para el último período
                        checkbox.setOnClickListener {
                            handleLastPeriodClick(player.id, period, checkbox)
                        }

                        // Long click para marcar salidas (X)
                        checkbox.setOnLongClickListener {
                            if (checkbox.isChecked && !isPlayerSubstitute(player.id, period)) {
                                togglePlayerOut(player.id, period, markerText)
                            }
                            true
                        }
                    } else {
                        // Click normal para otros períodos
                        checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                            if (isChecked && viewModel.isPeriodFull(period)) {
                                buttonView.isChecked = false
                                return@setOnCheckedChangeListener
                            }
                            viewModel.togglePlayerInPeriod(player.id, period, isChecked)
                        }
                    }

                    gridCells[Pair(period, player.id)] = checkbox
                    binding.gridLayout.addView(cell)
                }
            }
        }
    }

    private fun handleLastPeriodClick(playerId: Int, period: Int, checkbox: CheckBox) {
        val currentlyChecked = viewModel.distribution.value[period]?.any { it.id == playerId } == true
        val isSubstitute = isPlayerSubstitute(playerId, period)

        if (!currentlyChecked && !isSubstitute) {
            // Intentar añadir jugador
            if (viewModel.isPeriodFull(period)) {
                // Ya hay 5 azules, añadir como sustituto (verde)
                viewModel.addSubstitute(playerId, period)
                checkbox.setBackgroundResource(R.drawable.bg_substitute_green)
                checkbox.isChecked = true
            } else {
                // Añadir como titular (azul)
                viewModel.togglePlayerInPeriod(playerId, period, true)
                checkbox.isChecked = true
            }
        } else if (isSubstitute) {
            // Quitar sustituto
            viewModel.removeSubstitute(playerId, period)
            checkbox.setBackgroundResource(R.drawable.checkbox_background)
            checkbox.isChecked = false
        } else {
            // Quitar titular
            viewModel.togglePlayerInPeriod(playerId, period, false)
            checkbox.isChecked = false
        }
    }

    private fun isPlayerSubstitute(playerId: Int, period: Int): Boolean {
        val key = period * 1000 + playerId
        return viewModel.substitutions.value[key]?.isSubstitute == true
    }

    private fun togglePlayerOut(playerId: Int, period: Int, markerView: TextView?) {
        val key = period * 1000 + playerId
        val currentSub = viewModel.substitutions.value[key]

        if (currentSub?.isOut == true) {
            // Quitar marca de salida
            viewModel.toggleSubstitution(playerId, period, false)
            markerView?.visibility = View.GONE
        } else {
            // Marcar como sale
            viewModel.toggleSubstitution(playerId, period, true)
            markerView?.visibility = View.VISIBLE
        }
    }

    private fun addHeaderCell(text: String) {
        val cell = TextView(requireContext()).apply {
            this.text = text
            gravity = Gravity.CENTER
            // Crear los parámetros de layout con márgenes
            val params = if (text.isEmpty()) {
                LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.player_name_width),
                    resources.getDimensionPixelSize(R.dimen.grid_cell_size)
                )
            } else {
                LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.grid_cell_size),
                    resources.getDimensionPixelSize(R.dimen.grid_cell_size)
                )
            }

            // Establecer los márgenes
            params.setMargins(2, 2, 2, 2)
            layoutParams = params
        }
        binding.headerRow.addView(cell)
    }

    private fun observeViewModel() {
        viewModel.allTeamPlayers.observe(viewLifecycleOwner) { players ->
            setupPlayerSelection(players)
        }

//        viewLifecycleOwner.lifecycleScope.launch {
//            viewModel.distribution.collect { distribution ->
//                updateGridSelection(distribution)
//            }
//        }

        viewModel.maxPlayersReachedEvent.observe(viewLifecycleOwner) { period ->
            Snackbar.make(
                binding.root,
                "Ya hay 5 jugadores en el período $period",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        viewModel.validationMessages.observe(viewLifecycleOwner) { messages ->
            binding.validationMessages.text = messages.joinToString("\n")
            binding.saveButton.isEnabled = messages.isEmpty()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.substitutions.collect { substitutions ->
                updateSubstitutionMarkers(substitutions)
            }
        }
    }

    private fun setupPlayerSelection(allPlayers: List<Player>) {
        binding.availablePlayersGroup.removeAllViews()
        allPlayers.forEach { player ->
            val chip = Chip(requireContext()).apply {
                text = "${player.number} - ${player.name}"
                isCheckable = true
                isChecked = true
                setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updatePlayerAvailability(player.id, isChecked)
                }
            }
            binding.availablePlayersGroup.addView(chip)
        }
    }

    private fun updateGridSelection(distribution: Map<Int, List<Player>>) {
        gridCells.forEach { (position, checkbox) ->
            val (period, playerId) = position
            checkbox.isChecked = distribution[period]?.any { it.id == playerId } == true
        }
    }

    private fun updateSubstitutionMarkers(substitutions: Map<Int, SubstitutionInfo>) {
        substitutions.forEach { (key, info) ->
            val period = key / 1000
            val playerId = key % 1000

            val cellPair = Pair(period, playerId)
            val checkbox = gridCells[cellPair]

            checkbox?.let { cb ->
                if (info.isSubstitute) {
                    // Marcar como sustituto (verde)
                    cb.setBackgroundResource(R.drawable.bg_substitute_green)
                    cb.isChecked = true
                }

                // Buscar el TextView de la X
                cb.parent?.let { parent ->
                    if (parent is ViewGroup) {
                        val markerView = parent.findViewById<TextView>(R.id.substitution_marker)
                        markerView?.visibility = if (info.isOut) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            if (viewModel.validationMessages.value?.isEmpty() == true) {
                showSaveDialog()
            }
        }
    }

    private fun showSaveDialog() {
        val dialogBinding = DialogSaveDistributionBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Guardar distribución")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                val gameDate = dialogBinding.gameDateInput.text.toString()
                val opponent = dialogBinding.opponentInput.text.toString()
                viewModel.saveDistribution(gameDate, opponent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupExpandableSection() {
        binding.toggleButton.setOnClickListener {
            isPlayersExpanded = !isPlayersExpanded
            togglePlayersSection()
        }
    }

    private fun togglePlayersSection() {
        binding.availablePlayersGroup.visibility = if (isPlayersExpanded) View.VISIBLE else View.GONE
        binding.toggleButton.setImageResource(
            if (isPlayersExpanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more
        )
    }

    private fun setupObservers() {
        // ... otros observers ...

        // Observer para el evento de máximo de jugadores alcanzado
        viewModel.maxPlayersReachedEvent.observe(viewLifecycleOwner) { period ->
            Snackbar.make(
                binding.root,
                "Ya hay 5 jugadores seleccionados en el período $period",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}