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
import com.example.apuniliapp.data.model.Document
import com.example.apuniliapp.data.model.DocumentCategory
import com.example.apuniliapp.data.repository.firebase.FirebaseAuditLogRepository
import com.example.apuniliapp.data.repository.firebase.FirebaseDocumentRepository
import com.example.apuniliapp.databinding.FragmentAdminDocumentsBinding
import com.example.apuniliapp.utils.AdminAccessGuard
import com.example.apuniliapp.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class AdminDocumentsFragment : Fragment() {

    private var _binding: FragmentAdminDocumentsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AdminDocumentAdapter
    private var allDocuments: List<Document> = emptyList()
    private var searchJob: Job? = null

    // File upload
    private var selectedFileUri: Uri? = null
    private var selectedFileName: String = ""
    private var selectedFileSize: Long = 0L
    private var currentDialogView: View? = null

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedFileUri = uri
            updateDialogFileInfo(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDocumentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!AdminAccessGuard.checkAdminAccess(requireContext(), findNavController(), binding.root)) {
            return
        }

        setupRecyclerView()
        setupSearch()
        setupFab()
        loadDocuments()
    }

    private fun setupRecyclerView() {
        adapter = AdminDocumentAdapter(
            onEdit = { document -> showDocumentDialog(document) },
            onDelete = { document -> confirmDeleteDocument(document) }
        )
        binding.rvAdminDocuments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AdminDocumentsFragment.adapter
        }
    }

    private fun setupSearch() {
        binding.etSearchDocuments.addTextChangedListener { editable ->
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(300)
                filterDocuments(editable.toString().trim())
            }
        }
    }

    private fun filterDocuments(query: String) {
        val filtered = if (query.isBlank()) {
            allDocuments
        } else {
            val q = query.lowercase()
            allDocuments.filter { doc ->
                doc.title.lowercase().contains(q) ||
                doc.description.lowercase().contains(q) ||
                doc.category.label.lowercase().contains(q) ||
                doc.type.lowercase().contains(q)
            }
        }
        adapter.submitList(filtered)
        updateEmptyState(filtered)
        updateCounter(filtered.size)
    }

    private fun setupFab() {
        binding.fabAddDocument.setOnClickListener {
            showDocumentDialog(null)
        }
    }

    private fun updateDialogFileInfo(uri: Uri) {
        val dialogView = currentDialogView ?: return
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex >= 0) selectedFileName = it.getString(nameIndex)
                if (sizeIndex >= 0) selectedFileSize = it.getLong(sizeIndex)
            }
        }

        val layoutFileInfo = dialogView.findViewById<LinearLayout>(R.id.layout_file_info)
        val tvFileName = dialogView.findViewById<TextView>(R.id.tv_file_name)
        val btnPickFile = dialogView.findViewById<MaterialButton>(R.id.btn_pick_file)
        val etSize = dialogView.findViewById<TextInputEditText>(R.id.et_dialog_doc_size)

        layoutFileInfo.visibility = View.VISIBLE
        tvFileName.text = selectedFileName
        btnPickFile.text = "Changer de fichier"

        // Auto-fill size
        if (selectedFileSize > 0) {
            val sizeMB = selectedFileSize / (1024.0 * 1024.0)
            val sizeStr = if (sizeMB >= 1) String.format("%.1f MB", sizeMB)
            else String.format("%.0f KB", selectedFileSize / 1024.0)
            etSize.setText(sizeStr)
        }

        // Auto-detect type from extension
        val ext = selectedFileName.substringAfterLast('.', "").uppercase()
        val dropdownType = dialogView.findViewById<AutoCompleteTextView>(R.id.dropdown_dialog_doc_type)
        val detectedType = when (ext) {
            "PDF" -> "PDF"
            "DOC", "DOCX" -> "DOC"
            "XLS", "XLSX" -> "XLS"
            "PPT", "PPTX" -> "PPT"
            "JPG", "JPEG", "PNG", "WEBP" -> "IMG"
            else -> ext
        }
        if (detectedType.isNotBlank()) {
            dropdownType.setText(detectedType, false)
        }
    }

    private fun showDocumentDialog(existingDocument: Document?) {
        val isEdit = existingDocument != null
        selectedFileUri = null
        selectedFileName = ""
        selectedFileSize = 0L

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_document_form, null)
        currentDialogView = dialogView

        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.et_dialog_doc_title)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.et_dialog_doc_description)
        val etSize = dialogView.findViewById<TextInputEditText>(R.id.et_dialog_doc_size)
        val dropdownType = dialogView.findViewById<AutoCompleteTextView>(R.id.dropdown_dialog_doc_type)
        val dropdownCategory = dialogView.findViewById<AutoCompleteTextView>(R.id.dropdown_dialog_doc_category)
        val btnPickFile = dialogView.findViewById<MaterialButton>(R.id.btn_pick_file)
        val layoutFileInfo = dialogView.findViewById<LinearLayout>(R.id.layout_file_info)
        val tvFileName = dialogView.findViewById<TextView>(R.id.tv_file_name)
        val btnClearFile = dialogView.findViewById<ImageView>(R.id.btn_clear_file)

        val types = listOf("PDF", "DOC", "DOCX", "XLS", "XLSX", "PPT", "IMG")
        dropdownType.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types))

        val categories = DocumentCategory.entries.map { it.label }
        dropdownCategory.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories))

        // Pick file
        btnPickFile.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }

        // Clear file
        btnClearFile.setOnClickListener {
            selectedFileUri = null
            selectedFileName = ""
            selectedFileSize = 0L
            layoutFileInfo.visibility = View.GONE
            btnPickFile.text = "Sélectionner un fichier"
            etSize.text?.clear()
        }

        // Pre-fill for edit
        if (isEdit) {
            etTitle.setText(existingDocument!!.title)
            etDescription.setText(existingDocument.description)
            etSize.setText(existingDocument.fileSize)
            dropdownType.setText(existingDocument.type, false)
            dropdownCategory.setText(existingDocument.category.label, false)
            if (existingDocument.fileUrl.isNotBlank()) {
                layoutFileInfo.visibility = View.VISIBLE
                tvFileName.text = "Fichier existant"
                btnPickFile.text = "Remplacer le fichier"
            }
        }

        val title = if (isEdit) R.string.admin_doc_edit else R.string.admin_doc_add

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton(R.string.save, null) // Set null, we'll override below
            .setNegativeButton(R.string.cancel, null)
            .create()
            .apply {
                setOnShowListener { dialog ->
                    val positiveBtn = getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                    positiveBtn.setOnClickListener {
                        val docTitle = etTitle.text.toString().trim()
                        val docDescription = etDescription.text.toString().trim()
                        val docSize = etSize.text.toString().trim()
                        val docType = dropdownType.text.toString().trim()
                        val docCategoryLabel = dropdownCategory.text.toString().trim()

                        if (docTitle.isBlank()) {
                            Snackbar.make(binding.root, "Le titre est obligatoire", Snackbar.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        if (selectedFileUri == null && !isEdit) {
                            Snackbar.make(binding.root, "Veuillez sélectionner un fichier", Snackbar.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        val category = DocumentCategory.entries.find { it.label == docCategoryLabel } ?: DocumentCategory.OTHER

                        // Disable buttons during upload
                        positiveBtn.isEnabled = false
                        val progressUpload = dialogView.findViewById<LinearProgressIndicator>(R.id.progress_upload)
                        progressUpload.visibility = View.VISIBLE

                        val sessionManager = SessionManager(requireContext())
                        val admin = sessionManager.getLoggedInUser()

                        lifecycleScope.launch {
                            try {
                                // Upload file if new one selected
                                val fileUrl = if (selectedFileUri != null) {
                                    uploadFile(selectedFileUri!!, "documents")
                                } else {
                                    existingDocument?.fileUrl ?: ""
                                }

                                if (fileUrl.isBlank() && selectedFileUri != null) {
                                    if (isAdded) {
                                        Snackbar.make(binding.root, "Échec de l'upload du fichier", Snackbar.LENGTH_LONG).show()
                                        positiveBtn.isEnabled = true
                                        progressUpload.visibility = View.GONE
                                    }
                                    return@launch
                                }

                                val document = Document(
                                    id = existingDocument?.id ?: "",
                                    title = docTitle,
                                    description = docDescription,
                                    type = docType.ifBlank { "PDF" },
                                    category = category,
                                    fileUrl = fileUrl,
                                    fileSize = docSize
                                )

                                if (isEdit) {
                                    FirebaseDocumentRepository.updateDocument(existingDocument!!.id, document)
                                    FirebaseAuditLogRepository.logAction(
                                        userId = admin?.id ?: "",
                                        userName = admin?.displayName ?: "Admin",
                                        action = AuditAction.DOCUMENT_EDITED,
                                        details = "Document modifié: $docTitle"
                                    )
                                } else {
                                    FirebaseDocumentRepository.addDocument(document)
                                    FirebaseAuditLogRepository.logAction(
                                        userId = admin?.id ?: "",
                                        userName = admin?.displayName ?: "Admin",
                                        action = AuditAction.DOCUMENT_ADDED,
                                        details = "Document ajouté: $docTitle"
                                    )
                                }
                                if (isAdded) {
                                    Snackbar.make(binding.root, R.string.admin_doc_saved, Snackbar.LENGTH_SHORT).show()
                                    loadDocuments()
                                }
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

    private suspend fun uploadFile(uri: Uri, folder: String): String {
        return try {
            val bucket = SupabaseClient.client.storage.from("media")
            val ext = selectedFileName.substringAfterLast('.', "pdf")
            val filename = "${UUID.randomUUID()}.$ext"
            val path = "$folder/$filename"

            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: return ""
            inputStream.close()

            bucket.upload(path, bytes)
            val publicUrl = bucket.publicUrl(path)
            Log.d("AdminDocuments", "Fichier uploadé: $publicUrl")
            publicUrl
        } catch (e: Exception) {
            Log.e("AdminDocuments", "Erreur upload: ${e.message}")
            ""
        }
    }

    private fun confirmDeleteDocument(document: Document) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.admin_doc_delete_confirm)
            .setMessage("\"${document.title}\" sera supprimé définitivement.")
            .setPositiveButton(R.string.delete) { _, _ ->
                val sessionManager = SessionManager(requireContext())
                val admin = sessionManager.getLoggedInUser()
                lifecycleScope.launch {
                    try {
                        binding.progressBar.visibility = View.VISIBLE
                        FirebaseDocumentRepository.deleteDocument(document.id)
                        FirebaseAuditLogRepository.logAction(
                            userId = admin?.id ?: "",
                            userName = admin?.displayName ?: "Admin",
                            action = AuditAction.DOCUMENT_DELETED,
                            details = "Document supprimé: ${document.title}"
                        )
                        if (isAdded) {
                            Snackbar.make(binding.root, R.string.admin_doc_deleted, Snackbar.LENGTH_SHORT).show()
                            loadDocuments()
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

    private fun loadDocuments() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val documents = FirebaseDocumentRepository.getAllDocuments()
                if (isAdded) {
                    allDocuments = documents
                    binding.progressBar.visibility = View.GONE
                    val query = binding.etSearchDocuments.text?.toString()?.trim() ?: ""
                    filterDocuments(query)
                }
            } catch (e: Exception) {
                if (isAdded) {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, "Erreur de chargement", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateEmptyState(list: List<Document>) {
        if (list.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvAdminDocuments.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvAdminDocuments.visibility = View.VISIBLE
        }
    }

    private fun updateCounter(count: Int) {
        binding.tvDocumentCount.text = getString(R.string.admin_doc_count, count)
        binding.tvDocumentCount.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        currentDialogView = null
        _binding = null
    }
}
