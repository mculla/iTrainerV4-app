// app/src/main/java/com/example/itrainer/ui/players/PlayersFragment.kt
package com.example.itrainer.ui.players

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.itrainer.R
import com.example.itrainer.databinding.FragmentPlayersBinding
import com.example.itrainer.databinding.DialogAddPlayerBinding
import com.example.itrainer.data.entities.Player
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlayersFragment : Fragment() {
    private var _binding: FragmentPlayersBinding? = null
    private val binding get() = _binding!!

    private val args: PlayersFragmentArgs by navArgs()
    private val viewModel: PlayersViewModel by viewModels {
        PlayersViewModel.Factory(requireActivity().application, args.teamId)
    }

    private lateinit var playersAdapter: PlayersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        setupStatsButton()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        playersAdapter = PlayersAdapter(
            onPlayerClick = { player ->
                showEditPlayerDialog(player)
            },
            onPlayerLongClick = { player ->
                showDeletePlayerDialog(player)
                true
            }
        )

        binding.playersRecyclerView.apply {
            adapter = playersAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupFab() {
        binding.addPlayerFab.setOnClickListener {
            showAddPlayerDialog()
        }
    }

    private fun setupStatsButton() {
        binding.statsButton.setOnClickListener {
            val bundle = bundleOf("teamId" to args.teamId)
            findNavController().navigate(R.id.playerStatsFragment, bundle)
        }
    }

    private fun observeViewModel() {
        viewModel.players.observe(viewLifecycleOwner) { players ->
            playersAdapter.submitList(players)
            binding.emptyView.visibility = if (players.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.team.observe(viewLifecycleOwner) { team ->
            binding.teamTitle.text = "Jugadores - ${team.name}"
        }
    }

    private fun showAddPlayerDialog() {
        val dialogBinding = DialogAddPlayerBinding.inflate(layoutInflater)
        var isNumberValid = false
        var isNameValid = false

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nuevo jugador")
            .setView(dialogBinding.root)
            .setPositiveButton("Crear", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialogBinding.playerNumber.addTextChangedListener {
            isNumberValid = !it.isNullOrEmpty() && it.toString().toIntOrNull() != null
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = isNameValid && isNumberValid
        }

        dialogBinding.playerName.addTextChangedListener {
            isNameValid = !it.isNullOrEmpty()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = isNameValid && isNumberValid
        }

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.isEnabled = false

            positiveButton.setOnClickListener {
                val name = dialogBinding.playerName.text.toString().trim()
                val number = dialogBinding.playerNumber.text.toString().toInt()
                viewModel.createPlayer(name, number)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showEditPlayerDialog(player: Player) {
        val dialogBinding = DialogAddPlayerBinding.inflate(layoutInflater)
        dialogBinding.playerName.setText(player.name)
        dialogBinding.playerNumber.setText(player.number.toString())

        var isNumberValid = true
        var isNameValid = true

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Editar jugador")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialogBinding.playerNumber.addTextChangedListener {
            isNumberValid = !it.isNullOrEmpty() && it.toString().toIntOrNull() != null
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = isNameValid && isNumberValid
        }

        dialogBinding.playerName.addTextChangedListener {
            isNameValid = !it.isNullOrEmpty()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = isNameValid && isNumberValid
        }

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            positiveButton.setOnClickListener {
                val name = dialogBinding.playerName.text.toString().trim()
                val number = dialogBinding.playerNumber.text.toString().toInt()
                viewModel.updatePlayer(player, name, number)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showDeletePlayerDialog(player: Player) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar jugador")
            .setMessage("¿Estás seguro de que quieres eliminar a ${player.name}?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deletePlayer(player)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}