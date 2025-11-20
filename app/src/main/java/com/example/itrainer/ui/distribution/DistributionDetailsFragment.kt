package com.example.itrainer.ui.distribution

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.itrainer.data.models.SubstitutionInfoModel
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

        val periodsCount = details.periodsCount

        // Configurar cabecera de períodos
        binding.periodHeader.removeAllViews()
        addHeaderCell("")
        for (period in 1..periodsCount) {
            addHeaderCell("P$period")
        }

        // Configurar grid
        binding.distributionGrid.apply {
            removeAllViews()
            columnCount = periodsCount + 1
            rowCount = details.players.size

            details.players.forEach { player ->
                addPlayerCell(player)
                for (period in 1..periodsCount) {
                    addPeriodCell(
                        player,
                        period,
                        details.distribution[period] ?: emptyList(),
                        details.substitutions,
                        period == periodsCount && details.categoryId == 2 // Es último período de infantil
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
        substitutions: Map<String, SubstitutionInfoModel>?,
        isLastPeriod: Boolean
    ) {
        val isPlayerInPeriod = periodPlayers.any { it.id == player.id }
        val key = "${period * 1000 + player.id}"
        val substitutionInfo = substitutions?.get(key)

        TextView(requireContext()).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = 48.dpToPx()
                height = 48.dpToPx()
                columnSpec = GridLayout.spec(period, 1)
            }
            gravity = Gravity.CENTER

            // Determinar el color de fondo y el texto
            when {
                substitutionInfo?.isOut == true && isPlayerInPeriod -> {
                    // Jugador TITULAR que SALE (X roja con fondo azul)
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                    text = "X"
                    setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                    textSize = 24f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                substitutionInfo?.isSubstitute == true -> {
                    // Sustituto que ENTRA (fondo verde)
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.substitute_green))
                    text = "✓"
                    setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                }
                isPlayerInPeriod -> {
                    // Jugador titular normal (fondo azul)
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                    text = "✓"
                    setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                }
                else -> {
                    // No juega
                    setBackgroundResource(R.drawable.period_cell_background)
                    text = ""
                }
            }

            binding.distributionGrid.addView(this)
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}