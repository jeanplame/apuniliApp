package com.example.apuniliapp.ui.membership

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.apuniliapp.R
import com.example.apuniliapp.data.model.MembershipAdherenceStatus
import com.example.apuniliapp.data.model.MembershipRequest
import com.example.apuniliapp.data.model.MembershipStatus
import com.example.apuniliapp.data.model.UserRole
import com.example.apuniliapp.data.repository.firebase.FirebaseMembershipRepository
import com.example.apuniliapp.databinding.FragmentMembershipBinding
import com.example.apuniliapp.utils.SessionManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MembershipFragment : Fragment() {

    private var _binding: FragmentMembershipBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val viewModel: MembershipViewModel by viewModels()
    
    // Flag pour éviter les navigations infinies lors de onResume
    private var hasNavigatedToMemberSpace = false

    companion object {
        private const val KEY_HAS_NAVIGATED = "has_navigated_to_member_space"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMembershipBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Restaurer le flag de navigation depuis le bundle sauvegardé
        hasNavigatedToMemberSpace = savedInstanceState?.getBoolean(KEY_HAS_NAVIGATED, false) ?: false

        sessionManager = SessionManager(requireContext())
        val role = sessionManager.getCurrentRole()
        val currentUser = sessionManager.getLoggedInUser()

        // Rediriger les admins vers la gestion des adhésions
        if (role == UserRole.ADMIN) {
            try {
                findNavController().navigate(
                    R.id.nav_admin_membership,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.nav_membership, true)
                        .build()
                )
            } catch (_: Exception) {
                Snackbar.make(binding.root, getString(R.string.membership_admin_redirect), Snackbar.LENGTH_LONG).show()
            }
            return
        }

        // Les visiteurs doivent se connecter
        if (!sessionManager.isLoggedIn()) {
            setupVisitorMode()
            return
        }

        // Si on a déjà navigué vers l'espace membre, ne pas re-naviguer
        if (hasNavigatedToMemberSpace) return

        // L'utilisateur est authentifié (MEMBER)
        if (currentUser != null) {
            handleMembershipStatus(currentUser.membershipStatus, currentUser.id)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_HAS_NAVIGATED, hasNavigatedToMemberSpace)
    }

    override fun onResume() {
        super.onResume()
        // ✅ NE JAMAIS re-naviguer dans onResume — cela causait la boucle infinie
        // La navigation est gérée uniquement dans onViewCreated
    }

    /**
     * Gère l'affichage selon le statut d'adhésion, sans dupliquer la logique.
     */
    private fun handleMembershipStatus(status: MembershipAdherenceStatus, userId: String) {
        when (status) {
            MembershipAdherenceStatus.NOT_SUBMITTED -> {
                setupFormSubmission()
                observeViewModel()
            }
            MembershipAdherenceStatus.PENDING -> {
                setupStatusDisplay("⏳ Demande en attente",
                    "Votre demande d'adhésion est en cours d'évaluation par les administrateurs.")
            }
            MembershipAdherenceStatus.ACCEPTED -> {
                setupMemberSpace()
            }
            MembershipAdherenceStatus.REJECTED -> {
                viewModel.getMembershipRequestByUserId(userId)
                observeRejectionStatus()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.membershipRequest.observe(viewLifecycleOwner) { request ->
            if (request != null) {
                // L'utilisateur a déjà une demande
                setupStatusDisplay(request)
            } else {
                // Première demande d'adhésion
                setupFormSubmission()
            }
        }

        viewModel.userMembershipStatus.observe(viewLifecycleOwner) { status ->
            // Géré dans setupStatusDisplay

        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrBlank()) {
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                // Réactiver le bouton en cas d'erreur
                binding.btnSubmit.isEnabled = true
                binding.btnSubmit.text = "Envoyer ma demande d'adhésion"
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnSubmit.isEnabled = !isLoading
        }

        viewModel.requestSubmitted.observe(viewLifecycleOwner) { submitted ->
            if (submitted == true) {
                Snackbar.make(binding.root, "Demande envoyée avec succès ! L'administrateur va l'étudier.", Snackbar.LENGTH_LONG).show()
                // Recharger le statut après 1 seconde pour afficher la demande envoyée
                binding.root.postDelayed({
                    if (isAdded) {
                        val currentUser = sessionManager.getLoggedInUser()
                        if (currentUser != null) {
                            viewModel.getMembershipRequestByUserId(currentUser.id)
                        }
                    }
                }, 500)
            }
        }
    }

    private fun setupStatusDisplay(title: String, message: String) {
        // Masquer les champs du formulaire ET la description
        binding.formFields.visibility = View.GONE
        binding.tvDescription.visibility = View.GONE
        binding.btnSubmit.visibility = View.GONE
        binding.cbStatutes.visibility = View.GONE
        binding.cbRoi.visibility = View.GONE
        binding.cbValues.visibility = View.GONE
        binding.btnUploadPhoto.visibility = View.GONE
        binding.btnUploadId.visibility = View.GONE

        // Afficher le statut
        binding.tvMembershipStatus.visibility = View.VISIBLE
        binding.tvStatusMessage.visibility = View.VISIBLE

        // Déterminer la couleur selon le statut
        val color = when {
            title.contains("attente", ignoreCase = true) -> requireContext().getColor(android.R.color.holo_orange_dark)
            title.contains("approuvée", ignoreCase = true) -> requireContext().getColor(android.R.color.holo_green_dark)
            title.contains("rejetée", ignoreCase = true) -> requireContext().getColor(android.R.color.holo_red_dark)
            else -> requireContext().getColor(android.R.color.holo_blue_dark)
        }

        binding.tvMembershipStatus.setTextColor(color)
        binding.tvMembershipStatus.text = title
        binding.tvStatusMessage.text = message
    }

    private fun setupStatusDisplay(request: MembershipRequest) {
        // Masquer les champs du formulaire ET la description
        binding.formFields.visibility = View.GONE
        binding.tvDescription.visibility = View.GONE
        binding.btnSubmit.visibility = View.GONE
        binding.cbStatutes.visibility = View.GONE
        binding.cbRoi.visibility = View.GONE
        binding.cbValues.visibility = View.GONE
        binding.btnUploadPhoto.visibility = View.GONE
        binding.btnUploadId.visibility = View.GONE

        // Afficher le statut
        binding.tvMembershipStatus.visibility = View.VISIBLE
        binding.tvStatusMessage.visibility = View.VISIBLE

        // Afficher selon le vrai statut de la demande
        when (request.status) {
            MembershipStatus.PENDING -> {
                binding.tvMembershipStatus.setTextColor(requireContext().getColor(android.R.color.holo_orange_dark))
                binding.tvMembershipStatus.text = "⏳ Demande en attente"
                binding.tvStatusMessage.text = "Votre demande d'adhésion est en cours d'évaluation par les administrateurs."
                binding.btnSubmit.visibility = View.GONE
            }
            MembershipStatus.ACCEPTED -> {
                binding.tvMembershipStatus.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
                binding.tvMembershipStatus.text = "✅ Demande approuvée"
                binding.tvStatusMessage.text = "Félicitations ! Vous êtes maintenant membre de l'ASBL Apunili."
                binding.btnSubmit.visibility = View.GONE
            }
            MembershipStatus.REJECTED -> {
                binding.tvMembershipStatus.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
                binding.tvMembershipStatus.text = "❌ Demande rejetée"
                
                val messageText = if (request.adminComment.isNotBlank()) {
                    "Votre demande a été rejetée.\n\nCommentaire : ${request.adminComment}"
                } else {
                    "Votre demande a été rejetée."
                }
                binding.tvStatusMessage.text = messageText

                // Bouton pour soumettre une nouvelle demande
                binding.btnSubmit.visibility = View.VISIBLE
                binding.btnSubmit.text = "Soumettre une nouvelle demande"
                binding.btnSubmit.setOnClickListener {
                    setupFormSubmission()
                }
            }
        }
    }

    private fun observeRejectionStatus() {
        viewModel.membershipRequest.observe(viewLifecycleOwner) { request ->
            if (request != null) {
                setupStatusDisplay(request)
            }
        }
    }

    private fun setupMemberSpace() {
        // Éviter la double navigation
        if (hasNavigatedToMemberSpace) return
        
        try {
            // Vérifier qu'on n'est pas déjà sur le dashboard
            val currentDest = findNavController().currentDestination?.id
            if (currentDest == R.id.nav_member_dashboard) {
                hasNavigatedToMemberSpace = true
                return
            }
            
            findNavController().navigate(
                R.id.action_membership_to_member_dashboard,
                null,
                NavOptions.Builder()
                    .setPopUpTo(R.id.nav_membership, true) // inclusive=true : retire MembershipFragment du back stack
                    .setLaunchSingleTop(true)
                    .build()
            )
            hasNavigatedToMemberSpace = true
        } catch (e: Exception) {
            // Fallback: afficher l'affichage basique si la navigation échoue
            displayLegacyMemberSpace()
        }
    }

    private fun displayLegacyMemberSpace() {
        // Masquer complètement le formulaire
        binding.formFields.visibility = View.GONE
        binding.tvDescription.visibility = View.GONE
        binding.btnSubmit.visibility = View.GONE
        binding.cbStatutes.visibility = View.GONE
        binding.cbRoi.visibility = View.GONE
        binding.cbValues.visibility = View.GONE
        binding.btnUploadPhoto.visibility = View.GONE
        binding.btnUploadId.visibility = View.GONE

        // Afficher l'espace membre avec les infos
        binding.tvMembershipStatus.visibility = View.VISIBLE
        binding.tvStatusMessage.visibility = View.VISIBLE

        val currentUser = sessionManager.getLoggedInUser()
        
        binding.tvMembershipStatus.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
        binding.tvMembershipStatus.text = "✅ Bienvenue ${currentUser?.displayName ?: "Membre"} !"
        
        val memberInfo = """
            Statut: Membre actif de l'ASBL Apunili
            Email: ${currentUser?.email}
            Inscrit depuis: ${android.text.format.DateFormat.format("dd/MM/yyyy", System.currentTimeMillis())}
            
            En tant que membre, vous avez accès à:
            • Consulter les activités et événements
            • Télécharger les documents officiels
            • Accéder à la galerie de photos et vidéos
            • Suivre les mises à jour de l'association
        """.trimIndent()
        
        binding.tvStatusMessage.text = memberInfo
        binding.btnSubmit.visibility = View.VISIBLE
        binding.btnSubmit.text = "Voir mon profil"
        binding.btnSubmit.setOnClickListener {
            Snackbar.make(binding.root, "Profil détaillé du membre", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setupVisitorMode() {
        binding.btnSubmit.isEnabled = false
        binding.btnSubmit.text = getString(R.string.membership_login_required)
        binding.btnSubmit.alpha = 0.5f
        setFormEnabled(false)

        binding.btnSubmit.setOnClickListener {
            Snackbar.make(binding.root, getString(R.string.membership_login_required_message), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.nav_login_action)) {
                    try { findNavController().navigate(R.id.nav_login) } catch (_: Exception) { }
                }
                .show()
        }
    }

    private fun setupFormSubmission() {
        // Rendre le formulaire visible
        binding.formFields.visibility = View.VISIBLE
        binding.tvDescription.visibility = View.VISIBLE
        binding.btnSubmit.visibility = View.VISIBLE
        binding.cbStatutes.visibility = View.VISIBLE
        binding.cbRoi.visibility = View.VISIBLE
        binding.cbValues.visibility = View.VISIBLE
        binding.btnUploadPhoto.visibility = View.VISIBLE
        binding.btnUploadId.visibility = View.VISIBLE
        binding.tvMembershipStatus.visibility = View.GONE
        binding.tvStatusMessage.visibility = View.GONE

        setFormEnabled(true)
        binding.btnSubmit.isEnabled = true
        binding.btnSubmit.alpha = 1f
        binding.btnSubmit.text = "Envoyer ma demande d'adhésion"

        // Pré-remplir avec les données de l'utilisateur
        val currentUser = sessionManager.getLoggedInUser()
        if (currentUser != null) {
            binding.etEmail.setText(currentUser.email)
            binding.etLastname.setText(currentUser.displayName)
        }

        setupDatePicker()

        binding.btnSubmit.setOnClickListener {
            val lastname = binding.etLastname.text.toString().trim()
            val firstname = binding.etFirstname.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val userId = sessionManager.getLoggedInUser()?.id ?: ""

            if (lastname.isBlank() || firstname.isBlank() || email.isBlank()) {
                Snackbar.make(binding.root, "Veuillez remplir tous les champs obligatoires", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Vérifier que le genre est sélectionné
            val gender = when (binding.rgGender.checkedRadioButtonId) {
                R.id.rb_male -> "Masculin"
                R.id.rb_female -> "Féminin"
                else -> ""
            }
            if (gender.isBlank()) {
                Snackbar.make(binding.root, "Veuillez sélectionner votre sexe", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Vérifier la date de naissance
            val birthdate = binding.etBirthdate.text.toString().trim()
            if (birthdate.isBlank()) {
                Snackbar.make(binding.root, "Veuillez indiquer votre date de naissance", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (!binding.cbStatutes.isChecked || !binding.cbRoi.isChecked || !binding.cbValues.isChecked) {
                Snackbar.make(binding.root, "Veuillez accepter toutes les conditions", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Désactiver le bouton pour éviter les demandes multiples
            binding.btnSubmit.isEnabled = false
            binding.btnSubmit.text = "Envoi en cours..."

            val request = MembershipRequest(
                userId = userId,
                lastname = lastname,
                postname = binding.etPostname.text.toString().trim(),
                firstname = firstname,
                gender = gender,
                birthdate = birthdate,
                address = binding.etAddress.text.toString().trim(),
                phone = binding.etPhone.text.toString().trim(),
                email = email,
                profession = binding.etProfession.text.toString().trim(),
                acceptedStatutes = binding.cbStatutes.isChecked,
                acceptedROI = binding.cbRoi.isChecked,
                acceptedValues = binding.cbValues.isChecked
            )

            viewModel.submitMembershipRequest(request)
        }
    }

    private fun setFormEnabled(enabled: Boolean) {
        binding.etLastname.isEnabled = enabled
        binding.etPostname.isEnabled = enabled
        binding.etFirstname.isEnabled = enabled
        binding.etBirthdate.isEnabled = enabled
        binding.etAddress.isEnabled = enabled
        binding.etPhone.isEnabled = enabled
        binding.etEmail.isEnabled = enabled
        binding.etProfession.isEnabled = enabled
        // Genre : activer/désactiver tous les boutons radio
        binding.rgGender.isEnabled = enabled
        binding.rbMale.isEnabled = enabled
        binding.rbFemale.isEnabled = enabled
        binding.cbStatutes.isEnabled = enabled
        binding.cbRoi.isEnabled = enabled
        binding.cbValues.isEnabled = enabled
        binding.btnUploadPhoto.isEnabled = enabled
        binding.btnUploadId.isEnabled = enabled
        val alpha = if (enabled) 1f else 0.6f
        binding.btnUploadPhoto.alpha = alpha
        binding.btnUploadId.alpha = alpha
    }

    private fun setupDatePicker() {
        binding.etBirthdate.setOnClickListener {
            if (!binding.etBirthdate.isEnabled) return@setOnClickListener
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Date de naissance")
                .build()
            datePicker.addOnPositiveButtonClickListener { selection ->
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
                binding.etBirthdate.setText(sdf.format(Date(selection)))
            }
            datePicker.show(parentFragmentManager, "birthdate_picker")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
