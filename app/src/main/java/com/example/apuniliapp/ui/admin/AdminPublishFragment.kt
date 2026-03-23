package com.example.apuniliapp.ui.admin

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apuniliapp.R
import com.example.apuniliapp.config.SupabaseClient
import com.example.apuniliapp.data.model.Activity
import com.example.apuniliapp.data.model.AuditAction
import com.example.apuniliapp.data.model.Event
import com.example.apuniliapp.data.repository.firebase.FirebaseActivityRepository
import com.example.apuniliapp.data.repository.firebase.FirebaseAuditLogRepository
import com.example.apuniliapp.data.repository.firebase.FirebaseEventRepository
import com.example.apuniliapp.databinding.FragmentAdminPublishBinding
import com.example.apuniliapp.utils.AdminAccessGuard
import com.example.apuniliapp.utils.SessionManager
import com.google.android.material.snackbar.Snackbar
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AdminPublishFragment : Fragment() {

    private var _binding: FragmentAdminPublishBinding? = null
    private val binding get() = _binding!!
    private var isActivityMode = true

    // Mode édition
    private var isEditMode = false
    private var editId: String? = null

    private val selectedImages = mutableListOf<Uri>()
    private lateinit var imageAdapter: ImageThumbnailAdapter

    // Lanceur pour sélectionner plusieurs images
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImages.addAll(uris)
            updateImagesUI()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminPublishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!AdminAccessGuard.checkAdminAccess(requireContext(), findNavController(), binding.root)) {
            return
        }

        // Vérifier si on est en mode édition (arguments passés par AdminManagePublicationsFragment)
        editId = arguments?.getString("editId")
        val argIsActivity = arguments?.getBoolean("isActivity", true) ?: true
        isEditMode = editId != null

        setupToggle()
        setupCategoryDropdown()
        setupDatePicker()
        setupImagePicker()
        setupPublishButton()

        // En mode édition, charger les données existantes
        if (isEditMode) {
            isActivityMode = argIsActivity
            loadExistingData()
        }
    }

    private fun setupToggle() {
        binding.toggleGroupType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isActivityMode = checkedId == R.id.btn_type_activity
                binding.tilCategory.visibility = if (isActivityMode) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupCategoryDropdown() {
        val categories = listOf(
            getString(R.string.admin_category_social),
            getString(R.string.admin_category_education),
            getString(R.string.admin_category_health),
            getString(R.string.admin_category_environment),
            getString(R.string.admin_category_general)
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.dropdownCategory.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        binding.etPublishDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val cal = Calendar.getInstance().apply { set(year, month, day) }
                    val format = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH)
                    binding.etPublishDate.setText(format.format(cal.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    // ═══════════════════════════════════════════
    // Gestion des images
    // ═══════════════════════════════════════════

    private fun setupImagePicker() {
        imageAdapter = ImageThumbnailAdapter(selectedImages) { position ->
            selectedImages.removeAt(position)
            imageAdapter.notifyItemRemoved(position)
            imageAdapter.notifyItemRangeChanged(position, selectedImages.size)
            updateImagesUI()
        }

        binding.rvSelectedImages.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = imageAdapter
        }

        binding.btnAddImages.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    private fun updateImagesUI() {
        val count = selectedImages.size
        if (count > 0) {
            binding.rvSelectedImages.visibility = View.VISIBLE
            binding.tvImagesCount.text = "$count image${if (count > 1) "s" else ""} sélectionnée${if (count > 1) "s" else ""}"
        } else {
            binding.rvSelectedImages.visibility = View.GONE
            binding.tvImagesCount.text = "Aucune image sélectionnée"
        }
        imageAdapter.notifyDataSetChanged()
    }

    /**
     * Upload les images sélectionnées vers Supabase Storage.
     * Retourne la liste des URLs publiques.
     */
    private suspend fun uploadImages(folder: String): List<String> {
        if (selectedImages.isEmpty()) return emptyList()

        val bucket = SupabaseClient.client.storage.from("media")
        val urls = mutableListOf<String>()

        for ((index, uri) in selectedImages.withIndex()) {
            try {
                val filename = "${UUID.randomUUID()}.jpg"
                val path = "$folder/$filename"

                // Lire les bytes du fichier
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: continue
                inputStream.close()

                // Upload vers Supabase Storage
                bucket.upload(path, bytes)

                // Récupérer l'URL publique
                val publicUrl = bucket.publicUrl(path)
                urls.add(publicUrl)

                Log.d("AdminPublish", "Image ${index + 1}/${selectedImages.size} uploadée: $publicUrl")
            } catch (e: Exception) {
                Log.e("AdminPublish", "Erreur upload image ${index + 1}: ${e.message}")
            }
        }
        return urls
    }

    // ═══════════════════════════════════════════
    // Publication
    // ═══════════════════════════════════════════

    private fun setupPublishButton() {
        binding.btnPublish.setOnClickListener {
            val title = binding.etPublishTitle.text.toString().trim()
            val description = binding.etPublishDescription.text.toString().trim()
            val date = binding.etPublishDate.text.toString().trim()
            val location = binding.etPublishLocation.text.toString().trim()

            if (title.isBlank() || description.isBlank() || date.isBlank() || location.isBlank()) {
                Snackbar.make(binding.root, R.string.admin_publish_fill_fields, Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sessionManager = SessionManager(requireContext())
            val admin = sessionManager.getLoggedInUser()

            // Désactiver les boutons pendant la publication
            binding.btnPublish.isEnabled = false
            binding.btnAddImages.isEnabled = false
            binding.progressUpload.visibility = View.VISIBLE
            binding.btnPublish.text = if (selectedImages.isNotEmpty()) "Upload des images..." else "Publication..."

            lifecycleScope.launch {
                try {
                    // 1. Upload des images si sélectionnées
                    val folder = if (isActivityMode) "activities" else "events"
                    val photoUrls = uploadImages(folder)

                    if (isAdded) {
                        binding.btnPublish.text = "Publication..."
                    }

                    // 2. Publier l'activité ou l'événement
                    if (isActivityMode) {
                        val category = binding.dropdownCategory.text.toString().ifBlank {
                            getString(R.string.admin_category_general)
                        }
                        if (isEditMode) {
                            // Mode édition : mettre à jour l'activité existante
                            FirebaseActivityRepository.updateActivity(
                                editId!!,
                                Activity(
                                    title = title,
                                    description = description,
                                    date = date,
                                    location = location,
                                    category = category,
                                    photoUrls = photoUrls
                                )
                            )
                            FirebaseAuditLogRepository.logAction(
                                userId = admin?.id ?: "",
                                userName = admin?.displayName ?: "Admin",
                                action = AuditAction.ACTIVITY_UPDATED,
                                details = "Activité modifiée: $title (${photoUrls.size} images)"
                            )
                            if (isAdded) Snackbar.make(binding.root, getString(R.string.admin_publish_success, "Activité"), Snackbar.LENGTH_SHORT).show()
                        } else {
                            // Mode création : ajouter une nouvelle activité
                            FirebaseActivityRepository.addActivity(
                                Activity(
                                    title = title,
                                    description = description,
                                    date = date,
                                    location = location,
                                    category = category,
                                    photoUrls = photoUrls
                                )
                            )
                            FirebaseAuditLogRepository.logAction(
                                userId = admin?.id ?: "",
                                userName = admin?.displayName ?: "Admin",
                                action = AuditAction.ACTIVITY_PUBLISHED,
                                details = "Activité publiée: $title (${photoUrls.size} images)"
                            )
                            if (isAdded) Snackbar.make(binding.root, getString(R.string.admin_publish_success, "Activité"), Snackbar.LENGTH_SHORT).show()
                        }
                    } else {
                        if (isEditMode) {
                            // Mode édition : mettre à jour l'événement existant
                            FirebaseEventRepository.updateEvent(
                                editId!!,
                                Event(
                                    title = title,
                                    description = description,
                                    date = date,
                                    location = location,
                                    photoUrls = photoUrls
                                )
                            )
                            FirebaseAuditLogRepository.logAction(
                                userId = admin?.id ?: "",
                                userName = admin?.displayName ?: "Admin",
                                action = AuditAction.EVENT_UPDATED,
                                details = "Événement modifié: $title (${photoUrls.size} images)"
                            )
                            if (isAdded) Snackbar.make(binding.root, getString(R.string.admin_publish_success, "Événement"), Snackbar.LENGTH_SHORT).show()
                        } else {
                            // Mode création : ajouter un nouvel événement
                            FirebaseEventRepository.addEvent(
                                Event(
                                    title = title,
                                    description = description,
                                    date = date,
                                    location = location,
                                    photoUrls = photoUrls
                                )
                            )
                            FirebaseAuditLogRepository.logAction(
                                userId = admin?.id ?: "",
                                userName = admin?.displayName ?: "Admin",
                                action = AuditAction.EVENT_PUBLISHED,
                                details = "Événement publié: $title (${photoUrls.size} images)"
                            )
                            if (isAdded) Snackbar.make(binding.root, getString(R.string.admin_publish_success, "Événement"), Snackbar.LENGTH_SHORT).show()
                        }
                    }

                    // 3. Réinitialiser le formulaire
                    if (isAdded) {
                        binding.etPublishTitle.text?.clear()
                        binding.etPublishDescription.text?.clear()
                        binding.etPublishDate.text?.clear()
                        binding.etPublishLocation.text?.clear()
                        binding.dropdownCategory.text?.clear()
                        selectedImages.clear()
                        updateImagesUI()
                    }
                } catch (e: Exception) {
                    if (isAdded) Snackbar.make(binding.root, "Erreur: ${e.message}", Snackbar.LENGTH_LONG).show()
                } finally {
                    if (isAdded) {
                        binding.btnPublish.isEnabled = true
                        binding.btnAddImages.isEnabled = true
                        binding.progressUpload.visibility = View.GONE
                        binding.btnPublish.text = getString(R.string.admin_publish_confirm)
                    }
                }
            }
        }
    }

    // ═══════════════════════��═══════════════════
    // Mode édition : chargement des données
    // ═══════════════════════════════════════════

    private fun loadExistingData() {
        // Désactiver le toggle (on ne peut pas changer le type en édition)
        binding.toggleGroupType.isEnabled = false
        binding.btnPublish.text = "Modifier"

        // Sélectionner le bon bouton du toggle
        if (isActivityMode) {
            binding.toggleGroupType.check(R.id.btn_type_activity)
            binding.tilCategory.visibility = View.VISIBLE
        } else {
            binding.toggleGroupType.check(R.id.btn_type_event)
            binding.tilCategory.visibility = View.GONE
        }

        lifecycleScope.launch {
            try {
                if (isActivityMode) {
                    val activity = FirebaseActivityRepository.getActivityById(editId!!)
                    if (activity != null && isAdded) {
                        binding.etPublishTitle.setText(activity.title)
                        binding.etPublishDescription.setText(activity.description)
                        binding.etPublishDate.setText(activity.date)
                        binding.etPublishLocation.setText(activity.location)
                        binding.dropdownCategory.setText(activity.category, false)
                    }
                } else {
                    val event = FirebaseEventRepository.getEventById(editId!!)
                    if (event != null && isAdded) {
                        binding.etPublishTitle.setText(event.title)
                        binding.etPublishDescription.setText(event.description)
                        binding.etPublishDate.setText(event.date)
                        binding.etPublishLocation.setText(event.location)
                    }
                }
            } catch (e: Exception) {
                if (isAdded) Snackbar.make(binding.root, "Erreur de chargement: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}