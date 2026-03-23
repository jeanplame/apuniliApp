package com.example.apuniliapp.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.apuniliapp.MainActivity
import com.example.apuniliapp.R
import com.example.apuniliapp.data.model.AuditAction
import com.example.apuniliapp.data.model.UserRole
import com.example.apuniliapp.data.repository.firebase.FirebaseAuditLogRepository
import com.example.apuniliapp.data.repository.firebase.FirebaseDocumentRepository
import com.example.apuniliapp.data.repository.firebase.FirebaseGalleryRepository
import com.example.apuniliapp.data.repository.firebase.FirebaseMembershipRepository
import com.example.apuniliapp.data.repository.firebase.FirebaseActivityRepository
import com.example.apuniliapp.data.repository.firebase.FirebaseEventRepository
import com.example.apuniliapp.data.repository.firebase.FirebaseStructureRepository
import com.example.apuniliapp.databinding.FragmentAdminDashboardBinding
import com.example.apuniliapp.utils.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionManager = SessionManager(requireContext())
        val user = sessionManager.getLoggedInUser()

        // Vérifier la session
        if (!sessionManager.isLoggedIn() || sessionManager.isSessionExpired() || user == null) {
            try { findNavController().navigate(R.id.nav_login) } catch (_: Exception) {}
            return
        }

        // Vérifier que c'est un admin
        if (user.role != UserRole.ADMIN) {
            Snackbar.make(binding.root, "Accès non autorisé", Snackbar.LENGTH_SHORT).show()
            try { findNavController().navigate(R.id.nav_home) } catch (_: Exception) {}
            return
        }

        // Afficher le message de bienvenue
        binding.tvWelcome.text = getString(R.string.admin_dashboard_welcome, user.displayName)

        // Charger les stats depuis Firebase (asynchrone)
        updateStats()

        // Configurer tous les clics
        setupCardClickListeners()
        setupLogout(sessionManager)
    }

    override fun onResume() {
        super.onResume()
        // Rafraîchir les stats à chaque retour au dashboard
        if (_binding != null && isAdded) {
            updateStats()
        }
    }

    private fun updateStats() {
        lifecycleScope.launch {
            try {
                val pending = FirebaseMembershipRepository.getPendingMemberships().size
                val approved = FirebaseMembershipRepository.getApprovedMemberships().size
                val activities = FirebaseActivityRepository.getAllActivities().size
                val events = FirebaseEventRepository.getAllEvents().size
                val documents = FirebaseDocumentRepository.getAllDocuments().size
                val gallery = FirebaseGalleryRepository.getAllGalleryItems().size
                val team = FirebaseStructureRepository.getAllTeamMembers().size

                if (isAdded && _binding != null) {
                    binding.tvStatPending.text = pending.toString()
                    binding.tvStatMembers.text = approved.toString()
                    binding.tvStatActivities.text = activities.toString()
                    binding.tvStatEvents.text = events.toString()
                    binding.tvStatDocuments.text = documents.toString()
                    binding.tvStatGallery.text = gallery.toString()
                    binding.tvStatTeam.text = team.toString()
                }
            } catch (_: Exception) {}
        }
    }

    private fun setupCardClickListeners() {
        binding.cardManageMemberships.setOnClickListener { navigateSafe(R.id.action_dashboard_to_membership) }
        binding.cardManagePublications.setOnClickListener { navigateSafe(R.id.action_dashboard_to_manage_publications) }
        binding.cardPublishContent.setOnClickListener { navigateSafe(R.id.action_dashboard_to_publish) }
        binding.cardManageDocs.setOnClickListener { navigateSafe(R.id.action_dashboard_to_documents) }
        binding.cardViewLogs.setOnClickListener { navigateSafe(R.id.action_dashboard_to_logs) }
        binding.cardManageInfo.setOnClickListener { navigateSafe(R.id.action_dashboard_to_info) }
        binding.cardManageStructure.setOnClickListener { navigateSafe(R.id.action_dashboard_to_structure) }
        binding.cardManageGallery.setOnClickListener { navigateSafe(R.id.action_dashboard_to_gallery) }
    }

    private fun setupLogout(sessionManager: SessionManager) {
        binding.btnLogout.setOnClickListener {
            // Confirmation avant déconnexion
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Déconnexion")
                .setMessage("Êtes-vous sûr de vouloir vous déconnecter du tableau de bord administrateur ?")
                .setNegativeButton("Annuler", null)
                .setPositiveButton("Déconnecter") { _, _ ->
                    performLogout(sessionManager)
                }
                .show()
        }
    }

    private fun performLogout(sessionManager: SessionManager) {
        lifecycleScope.launch {
            FirebaseAuditLogRepository.logAction(
                userId = sessionManager.getLoggedInUser()?.id ?: "",
                userName = sessionManager.getLoggedInUser()?.displayName ?: "",
                action = AuditAction.LOGOUT,
                details = "Déconnexion depuis le tableau de bord"
            )
        }
        sessionManager.logout()

        // Mettre à jour la navigation dans MainActivity
        (activity as? MainActivity)?.updateNavigationForRole()
        activity?.invalidateOptionsMenu()

        Snackbar.make(binding.root, "Déconnexion réussie", Snackbar.LENGTH_SHORT).show()
        try {
            findNavController().navigate(R.id.nav_home, null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.mobile_navigation, true)
                    .build()
            )
        } catch (_: Exception) {}
    }

    private fun navigateSafe(destinationId: Int) {
        try { findNavController().navigate(destinationId) }
        catch (e: Exception) { Snackbar.make(binding.root, "Navigation impossible", Snackbar.LENGTH_SHORT).show() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}