package com.example.apuniliapp.ui.membership

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.apuniliapp.R
import com.example.apuniliapp.data.model.AuditAction
import com.example.apuniliapp.data.repository.firebase.FirebaseAuditLogRepository
import com.example.apuniliapp.databinding.FragmentMemberSettingsBinding
import com.example.apuniliapp.utils.SessionManager
import com.example.apuniliapp.utils.SessionRefreshWorker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Fragment pour gérer les paramètres du compte membre
 */
class MemberSettingsFragment : Fragment() {

    private var _binding: FragmentMemberSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemberSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // Afficher le nom d'utilisateur
        val currentUser = sessionManager.getLoggedInUser()
        binding.tvUserName.text = currentUser?.displayName ?: "Utilisateur"
        binding.tvUserEmail.text = currentUser?.email ?: ""
    }

    private fun setupListeners() {
        // Notifications
        binding.swNotifications.setOnCheckedChangeListener { _, isChecked ->
            Snackbar.make(binding.root, if (isChecked) "Notifications activées" else "Notifications désactivées", Snackbar.LENGTH_SHORT).show()
        }

        binding.swEmailNotifications.setOnCheckedChangeListener { _, isChecked ->
            Snackbar.make(binding.root, if (isChecked) "Emails activés" else "Emails désactivés", Snackbar.LENGTH_SHORT).show()
        }

        // Changement de mot de passe
        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        // Confidentialité
        binding.btnPrivacyPolicy.setOnClickListener {
            Snackbar.make(binding.root, "Politique de confidentialité", Snackbar.LENGTH_SHORT).show()
        }

        // Conditions d'utilisation
        binding.btnTermsOfService.setOnClickListener {
            Snackbar.make(binding.root, "Conditions d'utilisation", Snackbar.LENGTH_SHORT).show()
        }

        // À propos
        binding.btnAboutApp.setOnClickListener {
            showAboutDialog()
        }

        // Déconnexion
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }

        // Retour
        binding.toolbarSettings.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun showChangePasswordDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Changer le mot de passe")
            .setMessage("Fonction en développement\nVous pouvez réinitialiser votre mot de passe via l'écran de connexion")
            .setPositiveButton("Compris") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("À propos de Apunili")
            .setMessage("""
                Apunili ASBL
                Version: 1.0
                
                Gestion des adhésions et communication
                pour les associations communautaires.
                
                © 2026 Tous droits réservés
            """.trimIndent())
            .setPositiveButton("Fermer") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Déconnexion")
            .setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")
            .setNegativeButton("Annuler") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Déconnecter") { dialog, _ ->
                dialog.dismiss()
                performLogout()
            }
            .show()
    }

    private fun performLogout() {
        // Log d'audit avant la déconnexion
        val user = sessionManager.getLoggedInUser()
        lifecycleScope.launch {
            try {
                FirebaseAuditLogRepository.logAction(
                    userId = user?.id ?: "",
                    userName = user?.displayName ?: "",
                    action = AuditAction.LOGOUT,
                    details = "Déconnexion depuis l'espace membre"
                )
            } catch (_: Exception) {}
        }

        sessionManager.logout()
        
        // Arrêter le renouvellement automatique de session
        SessionRefreshWorker.stopPeriodicRefresh(requireContext())
        
        // Mettre à jour la navigation de MainActivity
        (activity as? com.example.apuniliapp.MainActivity)?.apply {
            updateNavigationForRole()
            invalidateOptionsMenu()
        }
        
        Snackbar.make(binding.root, "Déconnexion réussie", Snackbar.LENGTH_SHORT).show()
        
        // Naviguer vers login avec nettoyage complet de la pile
        try {
            findNavController().navigate(
                R.id.action_member_settings_to_login
            )
        } catch (_: Exception) {
            try {
                findNavController().navigate(
                    R.id.nav_home,
                    null,
                    androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(R.id.mobile_navigation, true)
                        .build()
                )
            } catch (_: Exception) {
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
