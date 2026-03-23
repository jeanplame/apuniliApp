package com.example.apuniliapp.ui.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apuniliapp.databinding.FragmentEventsBinding
import com.example.apuniliapp.ui.adapter.EventAdapter

class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EventsViewModel
    private lateinit var adapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[EventsViewModel::class.java]

        setupRecyclerView()
        observeData()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = EventAdapter()
        binding.rvEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@EventsFragment.adapter
        }
    }

    private fun observeData() {
        viewModel.allEvents.observe(viewLifecycleOwner) { events ->
            adapter.submitList(events)
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