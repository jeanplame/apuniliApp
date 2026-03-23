package com.example.apuniliapp.ui.documents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apuniliapp.R
import com.example.apuniliapp.data.model.DocumentCategory
import com.example.apuniliapp.databinding.FragmentDocumentsBinding
import com.example.apuniliapp.ui.adapter.DocumentAdapter
import com.google.android.material.chip.Chip

class DocumentsFragment : Fragment() {

    private var _binding: FragmentDocumentsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DocumentsViewModel
    private lateinit var adapter: DocumentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDocumentsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[DocumentsViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupCategoryChips()
        setupRetryButton()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = DocumentAdapter()
        binding.rvDocuments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@DocumentsFragment.adapter
        }
    }

    private fun setupCategoryChips() {
        // Add a chip for each DocumentCategory
        DocumentCategory.entries.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category.label
                isCheckable = true
            }
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Uncheck "All" chip
                    binding.chipAll.isChecked = false
                    viewModel.filterByCategory(category)
                }
            }
            binding.chipGroupCategories.addView(chip)
        }

        // "All" chip logic
        binding.chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Uncheck all category chips
                for (i in 1 until binding.chipGroupCategories.childCount) {
                    val child = binding.chipGroupCategories.getChildAt(i)
                    if (child is Chip) child.isChecked = false
                }
                viewModel.filterByCategory(null)
            }
        }
    }

    private fun setupRetryButton() {
        binding.btnRetry.setOnClickListener {
            viewModel.loadDocuments()
        }
    }

    private fun observeData() {
        viewModel.documents.observe(viewLifecycleOwner) { documents ->
            adapter.submitList(documents)
            updateVisibility(documents.isEmpty(), isError = false)
            if (documents.isNotEmpty()) {
                binding.tvDocumentsCount.visibility = View.VISIBLE
                binding.tvDocumentsCount.text = getString(R.string.documents_count, documents.size)
            } else {
                binding.tvDocumentsCount.visibility = View.GONE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.tvErrorMessage.text = error
                updateVisibility(isEmpty = true, isError = true)
            }
        }
    }

    private fun updateVisibility(isEmpty: Boolean, isError: Boolean) {
        binding.rvDocuments.visibility = if (!isEmpty && !isError) View.VISIBLE else View.GONE
        binding.layoutEmpty.visibility = if (isEmpty && !isError) View.VISIBLE else View.GONE
        binding.layoutError.visibility = if (isError) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

