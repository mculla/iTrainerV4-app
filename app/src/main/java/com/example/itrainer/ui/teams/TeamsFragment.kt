// app/src/main/java/com/example/itrainer/ui/teams/TeamsFragment.kt
package com.example.itrainer.ui.teams

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.itrainer.data.entities.Team
import com.example.itrainer.databinding.FragmentTeamsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class TeamsFragment : Fragment() {
    private var _binding: FragmentTeamsBinding? = null
    private val binding get() = _binding!!

    private val args: TeamsFragmentArgs by navArgs()
    private val viewModel: TeamsViewModel by viewModels {
        TeamsViewModel.Factory(requireActivity().application, args.categoryId)
    }

    private lateinit var teamsAdapter: TeamsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeamsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        teamsAdapter = TeamsAdapter(
            onTeamClick = { team ->
                // Al hacer clic en el equipo, vamos a la distribución
                viewModel.onTeamSelected(team.id)
            },
            onTeamLongClick = { team ->
                showDeleteTeamDialog(team)
                true
            },
            onManagePlayersClick = { team ->
                // Al hacer clic en el botón de jugadores, vamos a la gestión de jugadores
                navigateToPlayers(team.id)
            }
        )

        binding.teamsRecyclerView.apply {
            adapter = teamsAdapter
            setHasFixedSize(true)
        }
    }

    private fun navigateToPlayers(teamId: Int) {
        val action = TeamsFragmentDirections
            .actionTeamsFragmentToPlayersFragment(teamId)
        findNavController().navigate(action)
    }

    private fun setupFab() {
        binding.addTeamFab.setOnClickListener {
            showAddTeamDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.teams.observe(viewLifecycleOwner) { teams ->
            teamsAdapter.submitList(teams)
            binding.emptyView.visibility = if (teams.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.categoryName.observe(viewLifecycleOwner) { categoryName ->
            binding.categoryTitle.text = "Equipos - $categoryName"
        }

        viewModel.navigateToDistribution.observe(viewLifecycleOwner) { teamAndCategory ->
            teamAndCategory?.let { (teamId, categoryId) ->
                val action = TeamsFragmentDirections
                    .actionTeamsFragmentToDistributionFragment(teamId, categoryId)
                findNavController().navigate(action)
                viewModel.onNavigationComplete()
            }
        }
    }

    private fun showAddTeamDialog() {
        val input = TextInputEditText(requireContext()).apply {
            hint = "Nombre del equipo"
            setPadding(32, 32, 32, 32)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nuevo equipo")
            .setView(input)
            .setPositiveButton("Crear") { _, _ ->
                val teamName = input.text?.toString()?.trim()
                if (!teamName.isNullOrEmpty()) {
                    viewModel.createTeam(teamName)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteTeamDialog(team: Team) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar equipo")
            .setMessage("¿Estás seguro de que quieres eliminar el equipo ${team.name}?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteTeam(team)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

