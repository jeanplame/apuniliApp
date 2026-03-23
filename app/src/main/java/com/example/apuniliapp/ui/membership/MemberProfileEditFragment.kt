package com.example.apuniliapp.ui.membership

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.apuniliapp.R
import com.example.apuniliapp.data.repository.firebase.FirebaseUserRepository
import com.example.apuniliapp.databinding.FragmentMemberProfileEditBinding
import com.example.apuniliapp.utils.SessionManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment pour éditer le profil du membre
 * Permet de modifier:
 * - Nom, Prénom, Postnom
 * - Adresse, Téléphone
 * - Profession, Date de naissance
 * - Photo de profil
 */
class MemberProfileEditFragment : Fragment() {

    private var _binding: FragmentMemberProfileEditBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val viewModel: MembershipViewModel by viewModels()
    private var currentMembershipRequestId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemberProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        val currentUser = sessionManager.getLoggedInUser()

        setupUI()
        setupListeners()
        observeProfile()

        if (currentUser != null) {
            // Charger le profil complet (User + MembershipRequest)
            viewModel.loadMemberProfile(currentUser.id)
            // Aussi charger la demande pour avoir l'ID
            viewModel.getMembershipRequestByUserId(currentUser.id)
        }
    }

    private fun setupUI() {
        binding.toolbarEditProfile.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeProfile() {
        // Observer le profil complet pour pré-remplir les champs
        viewModel.memberProfile.observe(viewLifecycleOwner) { profile ->
            if (profile != null && _binding != null) {
                binding.etFirstname.setText(profile.firstname)
                binding.etLastname.setText(profile.lastname)
                binding.etPostname.setText(profile.postname)
                binding.etEmail.setText(profile.email)
                binding.etEmail.isEnabled = false
                binding.etPhone.setText(profile.phone)
                binding.etAddress.setText(profile.address)
                binding.etBirthdate.setText(profile.birthdate)
                binding.etProfession.setText(profile.profession)
            }
        }

        // Observer la demande d'adhésion pour récupérer l'ID
        viewModel.membershipRequest.observe(viewLifecycleOwner) { request ->
            if (request != null) {
                currentMembershipRequestId = request.id
            }
        }
    }

    private fun setupListeners() {
        // Bouton sauvegarder
        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }

        // Bouton annuler
        binding.btnCancelEdit.setOnClickListener {
            findNavController().popBackStack()
        }

        // Date picker
        binding.etBirthdate.setOnClickListener {
            showBirthdatePicker()
        }

        // Upload photo
        binding.btnUploadPhoto.setOnClickListener {
            Snackbar.make(binding.root, "Upload photo en développement", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun showBirthdatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Date de naissance")
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
            binding.etBirthdate.setText(sdf.format(Date(selection)))
        }

        datePicker.show(parentFragmentManager, "birthdate_picker")
    }

    private fun saveProfile() {
        val firstname = binding.etFirstname.text.toString().trim()
        val lastname = binding.etLastname.text.toString().trim()
        val postname = binding.etPostname.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val profession = binding.etProfession.text.toString().trim()
        val birthdate = binding.etBirthdate.text.toString().trim()

        if (firstname.isBlank() || lastname.isBlank()) {
            Snackbar.make(binding.root, "Veuillez remplir tous les champs obligatoires", Snackbar.LENGTH_LONG).show()
            return
        }

        val currentUser = sessionManager.getLoggedInUser() ?: return
        binding.btnSaveProfile.isEnabled = false
        binding.btnSaveProfile.text = "Sauvegarde..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1. Mettre à jour le display_name dans la table users
                val newDisplayName = "$firstname $lastname"
                FirebaseUserRepository.updateUserField(currentUser.id, "display_name", newDisplayName)

                // 2. Mettre à jour les champs dans la demande d'adhésion si elle existe
                val requestId = currentMembershipRequestId
                if (!requestId.isNullOrBlank()) {
                    com.example.apuniliapp.config.SupabaseClient.client
                        .from("membership_requests")
                        .update(buildJsonObject {
                            put("firstname", firstname)
                            put("lastname", lastname)
                            put("postname", postname)
                            put("phone", phone)
                            put("address", address)
                            put("profession", profession)
                            put("birthdate", birthdate)
                        }) {
                            filter { eq("id", requestId) }
                        }
                }

                // 3. Mettre à jour la session locale
                val updatedUser = currentUser.copy(displayName = newDisplayName)
                sessionManager.saveSession(updatedUser)

                if (isAdded && _binding != null) {
                    Snackbar.make(binding.root, "Profil mis à jour avec succès", Snackbar.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            } catch (e: Exception) {
                if (isAdded && _binding != null) {
                    Snackbar.make(binding.root, "Erreur: ${e.message}", Snackbar.LENGTH_LONG).show()
                }
            } finally {
                if (_binding != null) {
                    binding.btnSaveProfile.isEnabled = true
                    binding.btnSaveProfile.text = "Enregistrer les modifications"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

