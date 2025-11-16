package com.example.itrainer.ui.distribution

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.gridlayout.widget.GridLayout
import androidx.navigation.fragment.navArgs
import com.example.itrainer.R
import com.example.itrainer.data.entities.Player
import com.example.itrainer.data.models.PlayerModel
import com.example.itrainer.data.models.SubstitutionModel
import com.example.itrainer.databinding.FragmentDistributionDetailsBinding


class DistributionDetailsFragment : Fragment() {
    private var _binding: FragmentDistributionDetailsBinding? = null
    private val binding get() = _binding!!

    private val args: DistributionDetailsFragmentArgs by navArgs()
    private val viewModel: DistributionDetailsViewModel by viewModels {
        DistributionDetailsViewModel.Factory(
            requireActivity().application,
            args.distributionId
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDistributionDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.distributionDetails.observe(viewLifecycleOwner) { details ->
            setupDistributionView(details)
        }
    }

    private fun setupDistributionView(details: DistributionDetailsViewModel.DistributionDetails) {
        // Mostrar información del partido
        binding.gameInfo.text = "Partido vs ${details.opponent} (${details.gameDate})"

        val periodsCount = details.distribution.keys.maxOrNull() ?: 6

        // Configurar cabecera de períodos
        binding.periodHeader.removeAllViews()
        // Añadir celda vacía para alinear con nombres
        addHeaderCell("")
        // Añadir períodos dinámicamente
        for (period in 1..periodsCount) {
            addHeaderCell("P$period")
        }

        // Configurar grid
        binding.distributionGrid.apply {
            removeAllViews()
            columnCount = periodsCount + 1 // 1 para nombre + períodos
            rowCount = details.players.size

            details.players.forEach { player ->
                // Añadir nombre del jugador
                addPlayerCell(player)
                // Añadir celdas de período
                for (period in 1..periodsCount) {
                    addPeriodCell(
                        player,
                        period,
                        details.distribution[period] ?: emptyList(),
                        details.substitutions,
                        periodsCount
                    )
                }
            }
        }
    }

    private fun addHeaderCell(text: String) {
        TextView(requireContext()).apply {
            this.text = text
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                if (text.isEmpty()) 120.dpToPx() else 48.dpToPx(),
                48.dpToPx()
            ).apply {
                setMargins(2.dpToPx(), 2.dpToPx(), 2.dpToPx(), 2.dpToPx())
            }
            binding.periodHeader.addView(this)
        }
    }

    private fun addPlayerCell(player: Player) {
        TextView(requireContext()).apply {
            text = "${player.number} - ${player.name}"
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8.dpToPx(), 0, 0, 0)
            layoutParams = GridLayout.LayoutParams().apply {
                width = 120.dpToPx()
                height = 48.dpToPx()
                columnSpec = GridLayout.spec(0, 1)
            }
            setBackgroundResource(R.drawable.grid_cell_background)
            binding.distributionGrid.addView(this)
        }
    }

    private fun addPeriodCell(
        player: Player,
        period: Int,
        periodPlayers: List<PlayerModel>,
        substitutions: Map<String, SubstitutionModel>,
        totalPeriods: Int
    ) {
        val isLastPeriod = (period == totalPeriods)
        val isPlayerInPeriod = periodPlayers.any { it.id == player.id }

        // Crear el contenedor de la celda
        val cellContainer = FrameLayout(requireContext()).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = 48.dpToPx()
                height = 48.dpToPx()
                columnSpec = GridLayout.spec(period, 1)
                setMargins(1.dpToPx(), 1.dpToPx(), 1.dpToPx(), 1.dpToPx())
            }
        }

        if (isPlayerInPeriod) {
            // Buscar información de sustitución
            val key = (period * 1000 + player.id).toString()
            val subInfo = substitutions[key]

            // TextView para el checkmark
            val checkmarkView = TextView(requireContext()).apply {
                text = "✓"
                gravity = Gravity.CENTER
                textSize = 16f
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )

                // Aplicar color de fondo según el tipo
                when {
                    subInfo?.isSubstitute == true -> {
                        // Verde para sustitutos/cambios
                        setBackgroundColor(ContextCompat.getColor(context, R.color.substitute_green))
                        setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    }
                    isLastPeriod && subInfo == null -> {
                        // Azul para titulares del último período (los 5 primeros)
                        setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                        setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    }
                    else -> {
                        // Color normal para otros períodos
                        setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                        setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    }
                }
            }
            cellContainer.addView(checkmarkView)

            // Si sale (X roja), añadir marcador
            if (subInfo?.isOut == true) {
                val outMarker = TextView(requireContext()).apply {
                    text = "X"
                    gravity = Gravity.CENTER
                    textSize = 20f
                    setTypeface(null, android.graphics.Typeface.BOLD)  // ✅ CORRECTO
                    setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                }
                cellContainer.addView(outMarker)
            }
        } else {
            // Celda vacía con borde
            val emptyView = View(requireContext()).apply {
                setBackgroundResource(R.drawable.period_cell_background)
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
            cellContainer.addView(emptyView)
        }

        binding.distributionGrid.addView(cellContainer)
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
