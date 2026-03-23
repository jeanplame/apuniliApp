package com.example.apuniliapp.ui.admin

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apuniliapp.R
import com.example.apuniliapp.config.SupabaseClient
import com.example.apuniliapp.data.model.AuditAction
import com.example.apuniliapp.data.model.GalleryItem
import com.example.apuniliapp.data.repository.firebase.FirebaseAuditLogRepository
import com.example.apuniliapp.data.repository.firebase.FirebaseGalleryRepository
import com.example.apuniliapp.databinding.FragmentAdminGalleryBinding
import com.example.apuniliapp.utils.AdminAccessGuard
import com.example.apuniliapp.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class AdminGalleryFragment : Fragment() {

    private var _binding: FragmentAdminGalleryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AdminGalleryAdapter
    private var currentFilter: String? = null
    private var allItems: List<GalleryItem> = emptyList()
    private var searchJob: Job? = null

    // Media upload
    private var selectedMediaUri: Uri? = null
    private var selectedMediaName: String = ""
    private var currentDialogView: View? = null

    private val mediaPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedMediaUri = uri
            updateDialogMediaPreview(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!AdminAccessGuard.checkAdminAccess(requireContext(), findNavController(), binding.root)) {
            return
        }

        setupTabs()
        setupRecyclerView()
        setupSearch()
        setupFab()
        loadGalleryItems()
    }

    private fun setupTabs() {
        binding.tabLayoutGallery.addTab(binding.tabLayoutGallery.newTab().setText("Tous"))
        binding.tabLayoutGallery.addTab(binding.tabLayoutGallery.newTab().setText(R.string.gallery_photos))
        binding.tabLayoutGallery.addTab(binding.tabLayoutGallery.newTab().setText(R.string.gallery_videos))

        binding.tabLayoutGallery.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentFilter = when (tab?.position) {
                    1 -> "image"
                    2 -> "video"
                    else -> null
                }
                applyFilters()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        adapter = AdminGalleryAdapter(
            onEdit = { item -> showAddEditDialog(item) },
            onDelete = { item -> confirmDeleteItem(item) }
        )
        binding.rvGalleryItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AdminGalleryFragment.adapter
        }
    }

    private fun setupSearch() {
        binding.etSearchGallery.addTextChangedListener { editable ->
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(300)
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        val query = binding.etSearchGallery.text?.toString()?.trim()?.lowercase() ?: ""
        var filtered = allItems
        if (currentFilter != null) {
            filtered = filtered.filter { it.type == currentFilter }
        }
        if (query.isNotBlank()) {
            filtered = filtered.filter { item ->
                item.title.lowercase().contains(query) ||
                item.description.lowercase().contains(query) ||
                item.category.lowercase().contains(query)
            }
        }
        adapter.submitList(filtered)
        updateEmptyState(filtered)
        updateCounter(filtered.size)
    }

    private fun setupFab() {
        binding.fabAddGallery.setOnClickListener {
            showAddEditDialog(null)
        }
    }

    private fun updateDialogMediaPreview(uri: Uri) {
        val dialogView = currentDialogView ?: return

        // Get file name
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) selectedMediaName = it.getString(nameIndex)
            }
        }

        val cardPreview = dialogView.findViewById<MaterialCardView>(R.id.card_media_preview)
        val ivPreview = dialogView.findViewById<ImageView>(R.id.iv_media_preview)
        val layoutMediaInfo = dialogView.findViewById<LinearLayout>(R.id.layout_media_info)
        val tvMediaName = dialogView.findViewById<TextView>(R.id.tv_media_name)
        val btnPickMedia = dialogView.findViewById<MaterialButton>(R.id.btn_pick_media)

        cardPreview.visibility = View.VISIBLE
        ivPreview.setImageURI(uri)
        layoutMediaInfo.visibility = View.VISIBLE
        tvMediaName.text = selectedMediaName
        btnPickMedia.text = "Changer le média"

        // Auto-detect type from mime type
        val mimeType = requireContext().contentResolver.getType(uri) ?: ""
        val dropdownType = dialogView.findViewById<AutoCompleteTextView>(R.id.dropdown_gallery_type)
        if (mimeType.startsWith("video")) {
            dropdownType.setText("Vidéo", false)
        } else {
            dropdownType.setText("Photo", false)
        }
    }

    private fun showAddEditDialog(existingItem: GalleryItem?) {
        val isEdit = existingItem != null
        selectedMediaUri = null
        selectedMediaName = ""

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_gallery_form, null)
        currentDialogView = dialogView

        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.et_gallery_title)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.et_gallery_description)
        val dropdownType = dialogView.findViewById<AutoCompleteTextView>(R.id.dropdown_gallery_type)
        val etCategory = dialogView.findViewById<TextInputEditText>(R.id.et_gallery_category)
        val btnPickMedia = dialogView.findViewById<MaterialButton>(R.id.btn_pick_media)
        val cardPreview = dialogView.findViewById<MaterialCardView>(R.id.card_media_preview)
        val ivPreview = dialogView.findViewById<ImageView>(R.id.iv_media_preview)
        val btnClearMedia = dialogView.findViewById<ImageView>(R.id.btn_clear_media)
        val layoutMediaInfo = dialogView.findViewById<LinearLayout>(R.id.layout_media_info)

        val types = listOf("Photo", "Vidéo")
        dropdownType.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types))

        // Pick media
        btnPickMedia.setOnClickListener {
            val typeText = dropdownType.text.toString()
            val mimeType = if (typeText == "Vidéo") "video/*" else "image/*"
            mediaPickerLauncher.launch(mimeType)
        }

        // Clear media
        btnClearMedia.setOnClickListener {
            selectedMediaUri = null
            selectedMediaName = ""
            cardPreview.visibility = View.GONE
            layoutMediaInfo.visibility = View.GONE
            btnPickMedia.text = "Sélectionner une image ou vidéo"
        }

        // Pre-fill for edit
        if (isEdit) {
            etTitle.setText(existingItem!!.title)
            etDescription.setText(existingItem.description)
            dropdownType.setText(if (existingItem.type == "video") "Vidéo" else "Photo", false)
            etCategory.setText(existingItem.category)
            if (existingItem.imageUrl.isNotBlank() || existingItem.videoUrl.isNotBlank()) {
                btnPickMedia.text = "Remplacer le média"
            }
        }

        val dialogTitle = if (isEdit) getString(R.string.admin_gallery_edit) else getString(R.string.admin_gallery_add)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
            .create()
            .apply {
                setOnShowListener {
                    val positiveBtn = getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                    positiveBtn.setOnClickListener {
                        val title = etTitle.text.toString().trim()
                        val description = etDescription.text.toString().trim()
                        val typeStr = dropdownType.text.toString().trim()
                        val category = etCategory.text.toString().trim()

                        if (title.isBlank()) {
                            Snackbar.make(binding.root, R.string.admin_publish_fill_fields, Snackbar.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        if (selectedMediaUri == null && !isEdit) {
                            Snackbar.make(binding.root, "Veuillez sélectionner un média", Snackbar.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        val type = if (typeStr == "Vidéo") "video" else "image"

                        positiveBtn.isEnabled = false
                        val progressUpload = dialogView.findViewById<LinearProgressIndicator>(R.id.progress_upload)
                        progressUpload.visibility = View.VISIBLE

                        val sessionManager = SessionManager(requireContext())
                        val admin = sessionManager.getLoggedInUser()

                        lifecycleScope.launch {
                            try {
                                // Upload media if new one selected
                                val mediaUrl = if (selectedMediaUri != null) {
                                    uploadMedia(selectedMediaUri!!, "gallery", type)
                                } else {
                                    null
                                }

                                if (mediaUrl.isNullOrBlank() && selectedMediaUri != null) {
                                    if (isAdded) {
                                        Snackbar.make(binding.root, "Échec de l'upload du média", Snackbar.LENGTH_LONG).show()
                                        positiveBtn.isEnabled = true
                                        progressUpload.visibility = View.GONE
                                    }
                                    return@launch
                                }

                                if (isEdit) {
                                    val updatedItem = GalleryItem(
                                        id = existingItem!!.id,
                                        title = title,
                                        description = description,
                                        type = type,
                                        imageUrl = when {
                                            type == "image" && mediaUrl != null -> mediaUrl
                                            type == "image" -> existingItem.imageUrl
                                            else -> existingItem.imageUrl
                                        },
                                        videoUrl = when {
                                            type == "video" && mediaUrl != null -> mediaUrl
                                            type == "video" -> existingItem.videoUrl
                                            else -> existingItem.videoUrl
                                        },
                                        category = category,
                                        albumId = existingItem.albumId,
                                        createdAt = existingItem.createdAt
                                    )
                                    FirebaseGalleryRepository.updateGalleryItem(existingItem.id, updatedItem)
                                    FirebaseAuditLogRepository.logAction(
                                        userId = admin?.id ?: "",
                                        userName = admin?.displayName ?: "Admin",
                                        action = AuditAction.GALLERY_EDITED,
                                        details = "Média modifié: $title ($type)"
                                    )
                                    if (isAdded) {
                                        Snackbar.make(binding.root, "Média modifié avec succès", Snackbar.LENGTH_SHORT).show()
                                    }
                                } else {
                                    val item = GalleryItem(
                                        title = title,
                                        description = description,
                                        type = type,
                                        imageUrl = if (type == "image") (mediaUrl ?: "") else "",
                                        videoUrl = if (type == "video") (mediaUrl ?: "") else "",
                                        category = category
                                    )
                                    FirebaseGalleryRepository.addGalleryItem(item)
                                    FirebaseAuditLogRepository.logAction(
                                        userId = admin?.id ?: "",
                                        userName = admin?.displayName ?: "Admin",
                                        action = AuditAction.GALLERY_ADDED,
                                        details = "Média ajouté: $title ($type)"
                                    )
                                    if (isAdded) {
                                        Snackbar.make(binding.root, R.string.admin_gallery_saved, Snackbar.LENGTH_SHORT).show()
                                    }
                                }
                                if (isAdded) loadGalleryItems()
                                dismiss()
                            } catch (e: Exception) {
                                if (isAdded) {
                                    positiveBtn.isEnabled = true
                                    progressUpload.visibility = View.GONE
                                    Snackbar.make(binding.root, "Erreur: ${e.message}", Snackbar.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }
                show()
            }
    }

    private suspend fun uploadMedia(uri: Uri, folder: String, type: String): String {
        return try {
            val bucket = SupabaseClient.client.storage.from("media")
            val ext = if (type == "video") {
                selectedMediaName.substringAfterLast('.', "mp4")
            } else {
                selectedMediaName.substringAfterLast('.', "jpg")
            }
            val filename = "${UUID.randomUUID()}.$ext"
            val path = "$folder/$filename"

            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: return ""
            inputStream.close()

            bucket.upload(path, bytes)
            val publicUrl = bucket.publicUrl(path)
            Log.d("AdminGallery", "Média uploadé: $publicUrl")
            publicUrl
        } catch (e: Exception) {
            Log.e("AdminGallery", "Erreur upload: ${e.message}")
            ""
        }
    }

    private fun confirmDeleteItem(item: GalleryItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.admin_gallery_delete_confirm)
            .setMessage("\"${item.title}\" sera supprimé définitivement.")
            .setPositiveButton(R.string.delete) { _, _ ->
                val sessionManager = SessionManager(requireContext())
                val admin = sessionManager.getLoggedInUser()
                lifecycleScope.launch {
                    try {
                        binding.progressBar.visibility = View.VISIBLE
                        FirebaseGalleryRepository.deleteGalleryItem(item.id)
                        FirebaseAuditLogRepository.logAction(
                            userId = admin?.id ?: "",
                            userName = admin?.displayName ?: "Admin",
                            action = AuditAction.GALLERY_DELETED,
                            details = "Média supprimé: ${item.title}"
                        )
                        if (isAdded) {
                            Snackbar.make(binding.root, R.string.admin_gallery_deleted, Snackbar.LENGTH_SHORT).show()
                            loadGalleryItems()
                        }
                    } catch (e: Exception) {
                        if (isAdded) {
                            binding.progressBar.visibility = View.GONE
                            Snackbar.make(binding.root, "Erreur: ${e.message}", Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun loadGalleryItems() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val items = FirebaseGalleryRepository.getAllGalleryItems()
                if (isAdded) {
                    allItems = items
                    binding.progressBar.visibility = View.GONE
                    applyFilters()
                }
            } catch (e: Exception) {
                if (isAdded) {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, "Erreur de chargement", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateEmptyState(list: List<GalleryItem>) {
        if (list.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvGalleryItems.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvGalleryItems.visibility = View.VISIBLE
        }
    }

    private fun updateCounter(count: Int) {
        binding.tvGalleryCount.text = getString(R.string.admin_gallery_count, count)
        binding.tvGalleryCount.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        currentDialogView = null
        _binding = null
    }
}
