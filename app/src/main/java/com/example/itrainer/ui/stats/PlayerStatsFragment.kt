// app/src/main/java/com/example/itrainer/ui/stats/PlayerStatsFragment.kt
package com.example.itrainer.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.itrainer.databinding.FragmentPlayerStatsBinding

class PlayerStatsFragment : Fragment() {
    private var _binding: FragmentPlayerStatsBinding? = null
    private val binding get() = _binding!!

    private val args: PlayerStatsFragmentArgs by navArgs()
    private val viewModel: PlayerStatsViewModel by viewModels {
        PlayerStatsViewModel.Factory(requireActivity().application, args.teamId)
    }

    private lateinit var statsAdapter: PlayerStatsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        statsAdapter = PlayerStatsAdapter()
        binding.statsRecyclerView.apply {
            adapter = statsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViewModel() {
        viewModel.playerStats.observe(viewLifecycleOwner) { stats ->
            statsAdapter.submitList(stats)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}