package com.example.apuniliapp.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.apuniliapp.R
import com.example.apuniliapp.databinding.FragmentGalleryBinding
import com.example.apuniliapp.ui.adapter.GalleryAdapter
import com.google.android.material.tabs.TabLayout

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: GalleryViewModel
    private lateinit var adapter: GalleryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[GalleryViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupTabs()
        setupRetryButton()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = GalleryAdapter()
        binding.rvGallery.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@GalleryFragment.adapter
        }
    }

    private fun setupTabs() {
        binding.tabLayoutGallery.addTab(binding.tabLayoutGallery.newTab().setText(R.string.gallery_all))
        binding.tabLayoutGallery.addTab(binding.tabLayoutGallery.newTab().setText(R.string.gallery_photos))
        binding.tabLayoutGallery.addTab(binding.tabLayoutGallery.newTab().setText(R.string.gallery_videos))

        binding.tabLayoutGallery.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.filterByType("all")
                    1 -> viewModel.filterByType("image")
                    2 -> viewModel.filterByType("video")
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRetryButton() {
        binding.btnRetry.setOnClickListener {
            viewModel.loadGalleryItems()
        }
    }

    private fun observeData() {
        // Observe filtered items based on tab selection
        viewModel.selectedType.observe(viewLifecycleOwner) { type ->
            val items = when (type) {
                "image" -> viewModel.photos.value ?: emptyList()
                "video" -> viewModel.videos.value ?: emptyList()
                else -> viewModel.allItems.value ?: emptyList()
            }
            adapter.submitList(items)
            updateVisibility(items.isEmpty(), isError = false)
            updateCounter(items.size)
        }

        viewModel.allItems.observe(viewLifecycleOwner) { items ->
            val type = viewModel.selectedType.value ?: "all"
            val filtered = when (type) {
                "image" -> viewModel.photos.value ?: emptyList()
                "video" -> viewModel.videos.value ?: emptyList()
                else -> items
            }
            adapter.submitList(filtered)
            updateVisibility(filtered.isEmpty(), isError = false)
            updateCounter(filtered.size)
        }

        viewModel.photos.observe(viewLifecycleOwner) { photos ->
            if (viewModel.selectedType.value == "image") {
                adapter.submitList(photos)
                updateVisibility(photos.isEmpty(), isError = false)
                updateCounter(photos.size)
            }
        }

        viewModel.videos.observe(viewLifecycleOwner) { videos ->
            if (viewModel.selectedType.value == "video") {
                adapter.submitList(videos)
                updateVisibility(videos.isEmpty(), isError = false)
                updateCounter(videos.size)
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
        binding.rvGallery.visibility = if (!isEmpty && !isError) View.VISIBLE else View.GONE
        binding.layoutEmpty.visibility = if (isEmpty && !isError) View.VISIBLE else View.GONE
        binding.layoutError.visibility = if (isError) View.VISIBLE else View.GONE
    }

    private fun updateCounter(count: Int) {
        if (count > 0) {
            binding.tvGalleryCount.visibility = View.VISIBLE
            binding.tvGalleryCount.text = getString(R.string.gallery_count, count)
        } else {
            binding.tvGalleryCount.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
