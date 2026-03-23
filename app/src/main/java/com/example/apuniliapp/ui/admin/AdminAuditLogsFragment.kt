package com.example.apuniliapp.ui.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apuniliapp.R
import com.example.apuniliapp.data.model.AuditAction
import com.example.apuniliapp.data.model.AuditLog
import com.example.apuniliapp.data.repository.firebase.FirebaseAuditLogRepository
import com.example.apuniliapp.databinding.FragmentAdminAuditLogsBinding
import com.example.apuniliapp.utils.AdminAccessGuard
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdminAuditLogsFragment : Fragment() {

    private var _binding: FragmentAdminAuditLogsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AuditLogAdapter
    private var selectedFilter: AuditAction? = null
    private var allLogs: List<AuditLog> = emptyList()
    private var searchQuery: String = ""
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminAuditLogsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!AdminAccessGuard.checkAdminAccess(requireContext(), findNavController(), binding.root)) {
            return
        }

        setupFilterDropdown()
        setupSearch()
        setupRecyclerView()
        loadLogs()
    }

    private fun setupFilterDropdown() {
        val filterLabels = mutableListOf(getString(R.string.admin_logs_filter_all))
        filterLabels.addAll(AuditAction.entries.map { it.label })

        val filterAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, filterLabels)
        binding.dropdownFilter.setAdapter(filterAdapter)
        binding.dropdownFilter.setText(getString(R.string.admin_logs_filter_all), false)

        binding.dropdownFilter.setOnItemClickListener { _, _, position, _ ->
            selectedFilter = if (position == 0) null else AuditAction.entries[position - 1]
            filterAndDisplayLogs()
        }
    }

    private fun setupSearch() {
        binding.etSearchLogs.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300)
                    searchQuery = s?.toString()?.trim() ?: ""
                    filterAndDisplayLogs()
                }
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = AuditLogAdapter()

        binding.rvAuditLogs.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AdminAuditLogsFragment.adapter
        }
    }

    private fun filterAndDisplayLogs() {
        var filtered = allLogs

        // Filtre par action
        if (selectedFilter != null) {
            filtered = filtered.filter { it.action == selectedFilter }
        }

        // Filtre par texte de recherche
        if (searchQuery.isNotBlank()) {
            val query = searchQuery.lowercase()
            filtered = filtered.filter { log ->
                log.userName.lowercase().contains(query) ||
                log.details.lowercase().contains(query) ||
                log.action.label.lowercase().contains(query)
            }
        }

        if (isAdded && _binding != null) {
            adapter.submitList(filtered)
            binding.tvLogCount.text = "${filtered.size} entrée(s)"

            if (filtered.isEmpty()) {
                binding.tvEmptyLogs.visibility = View.VISIBLE
                binding.rvAuditLogs.visibility = View.GONE
            } else {
                binding.tvEmptyLogs.visibility = View.GONE
                binding.rvAuditLogs.visibility = View.VISIBLE
            }
        }
    }

    private fun loadLogs() {
        lifecycleScope.launch {
            try {
                allLogs = FirebaseAuditLogRepository.getRecentLogs(200)
                filterAndDisplayLogs()
            } catch (e: Exception) {
                if (isAdded && _binding != null) {
                    binding.tvLogCount.text = "Erreur de chargement"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        _binding = null
    }
}
