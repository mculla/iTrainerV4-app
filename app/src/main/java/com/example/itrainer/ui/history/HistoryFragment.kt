package com.example.itrainer.ui.history


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.itrainer.data.entities.GameDistribution
import com.example.itrainer.databinding.FragmentHistoryBinding
import com.example.itrainer.ui.history.HistoryAdapter
import com.example.itrainer.ui.history.HistoryViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.itrainer.R
import androidx.core.os.bundleOf
import com.example.itrainer.data.models.GameDistributionWithTeam


class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModel.Factory(requireActivity().application)
    }

    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        viewModel.loadDistributions()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(
            onDistributionClick = { distribution ->
                showDistributionDetails(distribution)
            },
            onDistributionLongClick = { distribution ->
                showDeleteDialog(distribution)
                true
            }
        )

        binding.historyRecyclerView.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViewModel() {
        viewModel.distributions.observe(viewLifecycleOwner) { distributions ->
            historyAdapter.submitList(distributions)
            binding.emptyView.visibility =
                if (distributions.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showDistributionDetails(distributionWithTeam: GameDistributionWithTeam) {
        findNavController().navigate(
            R.id.action_historyFragment_to_distributionDetailsFragment,
            bundleOf("distributionId" to distributionWithTeam.distribution.id)
        )
    }

    private fun showDeleteDialog(distributionWithTeam: GameDistributionWithTeam) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar distribución")
            .setMessage("¿Estás seguro de que quieres eliminar esta distribución?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteDistribution(distributionWithTeam.distribution)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}