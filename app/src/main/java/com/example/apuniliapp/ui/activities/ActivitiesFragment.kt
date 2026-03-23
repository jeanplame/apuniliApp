package com.example.apuniliapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.apuniliapp.R
import com.example.apuniliapp.databinding.FragmentActivitiesBinding
import com.example.apuniliapp.ui.adapter.ActivityListAdapter
import com.google.android.material.chip.Chip

class ActivitiesFragment : Fragment() {

    private var _binding: FragmentActivitiesBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ActivitiesViewModel
    private lateinit var listAdapter: ActivityListAdapter
    private var hasAnimated = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivitiesBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ActivitiesViewModel::class.java]

        setupAdapter()
        setupSearch()
        setupSwipeRefresh()
        observeData()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!hasAnimated) {
            animateEntrance()
            hasAnimated = true
        }
    }

    private fun setupAdapter() {
        listAdapter = ActivityListAdapter(
            onItemClick = { activity ->
                val bundle = Bundle().apply {
                    putString("activityId", activity.id)
                }
                findNavController().navigate(R.id.nav_activity_detail, bundle)
            },
            onShareClick = { activity ->
                shareActivity(activity)
            }
        )
        binding.rvActivities.adapter = listAdapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(
            R.color.md_theme_light_primary,
            R.color.md_theme_light_secondary,
            R.color.md_theme_light_tertiary
        )
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadActivities()
        }
    }

    private fun setupCategoryChips(categories: List<String>) {
        val chipGroup = binding.chipGroupFilter
        // Remove all except the "All" chip
        val chipAll = binding.chipAll
        chipGroup.removeAllViews()
        chipGroup.addView(chipAll)

        chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setCategoryFilter("")
        }

        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true
                setChipBackgroundColorResource(R.color.md_theme_light_primaryContainer)
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) viewModel.setCategoryFilter(category)
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun observeData() {
        viewModel.filteredActivities.observe(viewLifecycleOwner) { activities ->
            val isSearchActive = !binding.etSearch.text.isNullOrBlank()

            if (activities.isEmpty()) {
                binding.emptyState.isVisible = true
                binding.swipeRefresh.isVisible = false
                binding.tvEmptySubtitle.isVisible = isSearchActive
            } else {
                binding.emptyState.isVisible = false
                binding.swipeRefresh.isVisible = true
                listAdapter.submitList(activities)
            }

            // Update count
            val count = activities.size
            binding.tvActivitiesCount.text = if (count <= 1) {
                getString(R.string.activities_count_single, count)
            } else {
                getString(R.string.activities_count, count)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
        }

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            setupCategoryChips(categories)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                com.google.android.material.snackbar.Snackbar
                    .make(binding.root, it, com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry) { viewModel.loadActivities() }
                    .show()
                viewModel.clearError()
            }
        }
    }

    private fun shareActivity(activity: com.example.apuniliapp.data.model.Activity) {
        val shareText = getString(
            R.string.activities_share_text,
            activity.title,
            activity.description,
            activity.date,
            activity.location
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, activity.title)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.activities_share)))
    }

    private fun animateEntrance() {
        val views = listOf(
            binding.tilSearch,
            binding.rvActivities
        )
        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 40f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay((index * 100).toLong())
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
