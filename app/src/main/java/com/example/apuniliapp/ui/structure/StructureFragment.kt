package com.example.apuniliapp.ui.structure

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apuniliapp.databinding.FragmentStructureBinding
import com.example.apuniliapp.ui.adapter.TeamMemberAdapter

class StructureFragment : Fragment() {

    private var _binding: FragmentStructureBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: StructureViewModel
    private lateinit var adapter: TeamMemberAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStructureBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[StructureViewModel::class.java]

        setupRecyclerView()
        observeData()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = TeamMemberAdapter()
        binding.rvStructure.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@StructureFragment.adapter
        }
    }

    private fun observeData() {
        viewModel.teamMembers.observe(viewLifecycleOwner) { members ->
            adapter.submitList(members)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Optionnel: progressBar
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

