package com.example.apuniliapp.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.apuniliapp.MainActivity
import com.example.apuniliapp.R
import com.example.apuniliapp.config.SupabaseClient
import com.example.apuniliapp.data.model.AuditAction
import com.example.apuniliapp.data.model.User
import com.example.apuniliapp.data.model.UserRole
import com.example.apuniliapp.data.repository.firebase.FirebaseAuditLogRepository
import com.example.apuniliapp.data.repository.firebase.FirebaseUserRepository
import com.example.apuniliapp.databinding.FragmentLoginBinding
import com.example.apuniliapp.utils.GoogleSignInHelper
import com.example.apuniliapp.utils.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val supabaseAuth = SupabaseClient.client.auth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        // Si déjà connecté, récupérer le profil frais et rediriger
        if (sessionManager.isLoggedIn() && !sessionManager.isSessionExpired()) {
            lifecycleScope.launch {
                val supabaseUser = supabaseAuth.currentUserOrNull()
                if (supabaseUser != null) {
                    val freshProfile = FirebaseUserRepository.getUserByUidFromServer(supabaseUser.id)
                    if (freshProfile != null) {
                        sessionManager.saveSession(freshProfile)
                        (activity as? MainActivity)?.updateNavigationForRole()
                        if (isAdded) navigateAfterLogin(freshProfile.role)
                    } else {
                        val cachedUser = sessionManager.getLoggedInUser()
                        if (cachedUser != null && isAdded) navigateAfterLogin(cachedUser.role)
                    }
                }
            }
            return
        }

        // ══════ Configurer Google Sign-In ══════
        setupGoogleSignIn()

        // ══════ Bouton Connexion Email ══════
        binding.btnLogin.setOnClickListener {
            val email = binding.etLoginEmail.text.toString().trim()
            val password = binding.etLoginPassword.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                showError("Veuillez remplir tous les champs")
                return@setOnClickListener
            }

            if (sessionManager.isAccountLocked()) {
                val remaining = sessionManager.getRemainingLockoutSeconds()
                showError("Compte bloqué. Réessayez dans ${remaining}s")
                return@setOnClickListener
            }

            showLoading()
            loginWithEmail(email, password)
        }

        // ══════ Mot de passe oublié ══════
        binding.btnForgotPassword.setOnClickListener {
            showResetPasswordDialog()
        }

        // ══════ Aller vers inscription ══════
        binding.btnGoRegister.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_login_to_register)
            } catch (_: Exception) {
                try { findNavController().navigate(R.id.nav_register) } catch (_: Exception) {}
            }
        }
    }

    // ═══════════════════════════════════════════
    // Google Sign-In
    // ═══════════════════════════════════════════

    private fun setupGoogleSignIn() {
        if (GoogleSignInHelper.isConfigured()) {
            binding.btnGoogleSignIn.isEnabled = true
            binding.btnGoogleSignIn.visibility = View.VISIBLE
            binding.btnGoogleSignIn.setOnClickListener {
                showLoading()
                performGoogleSignIn()
            }
        } else {
            Log.w("LoginFragment", "Google Sign-In non configuré (Client ID manquant)")
            binding.btnGoogleSignIn.isEnabled = false
            binding.btnGoogleSignIn.visibility = View.GONE
        }
    }

    private fun performGoogleSignIn() {
        lifecycleScope.launch {
            val result = GoogleSignInHelper.signIn(requireContext())
            when (result) {
                is GoogleSignInHelper.GoogleSignInResult.Success -> {
                    loginWithGoogleToken(
                        idToken = result.idToken,
                        displayName = result.displayName,
                        email = result.email
                    )
                }
                is GoogleSignInHelper.GoogleSignInResult.Cancelled -> {
                    hideLoading()
                }
                is GoogleSignInHelper.GoogleSignInResult.Error -> {
                    hideLoading()
                    showError(result.message)
                }
            }
        }
    }

    private fun loginWithGoogleToken(idToken: String, displayName: String?, email: String?) {
        lifecycleScope.launch {
            try {
                supabaseAuth.signInWith(IDToken) {
                    this.idToken = idToken
                    this.provider = Google
                }
                val supabaseUser = supabaseAuth.currentUserOrNull()
                if (supabaseUser != null) {
                    val finalDisplayName = displayName
                        ?: supabaseUser.userMetadata?.get("full_name")?.toString()?.trim('"')
                        ?: supabaseUser.email?.substringBefore("@")
                        ?: "Utilisateur"
                    val finalEmail = supabaseUser.email ?: email ?: ""
                    handleSuccessfulLogin(
                        userId = supabaseUser.id,
                        email = finalEmail,
                        displayName = finalDisplayName,
                        logDetail = "Connexion via Google"
                    )
                } else {
                    hideLoading()
                    showError("Erreur de connexion avec Google")
                }
            } catch (e: Exception) {
                Log.e("LoginFragment", "Erreur Google→Supabase: ${e.message}", e)
                hideLoading()
                val errorMsg = when {
                    e.message?.contains("invalid") == true ->
                        "Token Google invalide. Réessayez."
                    e.message?.contains("provider") == true ->
                        "Google non activé dans Supabase. Contactez l'administrateur."
                    e.message?.contains("network") == true ->
                        "Erreur réseau. Vérifiez votre connexion."
                    else ->
                        "Erreur Google Sign-In: ${e.localizedMessage}"
                }
                showError(errorMsg)
            }
        }
    }

    // ═══════════════════════════════════════════
    // Email/Password Login via Supabase
    // ═══════════════════════════════════════════

    private fun loginWithEmail(email: String, password: String) {
        lifecycleScope.launch {
            try {
                supabaseAuth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                val supabaseUser = supabaseAuth.currentUserOrNull()
                if (supabaseUser != null) {
                    handleSuccessfulLogin(
                        userId = supabaseUser.id,
                        email = supabaseUser.email ?: email,
                        displayName = null,
                        logDetail = "Connexion par email réussie"
                    )
                } else {
                    hideLoading()
                    showError("Erreur de connexion")
                }
            } catch (e: Exception) {
                if (isAdded) {
                    sessionManager.recordFailedAttempt()
                    val attempts = sessionManager.getFailedAttemptCount()
                    val errorMsg = when {
                        e.message?.contains("Invalid login credentials") == true ||
                        e.message?.contains("password is invalid") == true ||
                        e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true ->
                            "Identifiants incorrects ($attempts/5)"
                        e.message?.contains("Email not confirmed") == true ->
                            "Email non confirmé. Vérifiez votre boîte mail."
                        e.message?.contains("no user record") == true ->
                            "Aucun compte trouvé avec cet email"
                        e.message?.contains("network") == true ->
                            "Erreur réseau. Vérifiez votre connexion."
                        attempts >= 5 ->
                            "Trop de tentatives. Compte bloqué temporairement."
                        else -> "Erreur de connexion ($attempts/5)"
                    }
                    hideLoading()
                    showError(errorMsg)
                }
            }
        }
    }

    // ═══════════════════════════════════════════
    // Traitement post-connexion (commun Email & Google)
    // ═══════════════════════════════════════════

    private suspend fun handleSuccessfulLogin(
        userId: String,
        email: String,
        displayName: String?,
        logDetail: String
    ) {
        var userProfile = FirebaseUserRepository.getUserByUidFromServer(userId)

        if (userProfile == null) {
            val profileByEmail = FirebaseUserRepository.getUserByEmail(email)
            if (profileByEmail != null) {
                userProfile = profileByEmail.copy(id = userId)
                FirebaseUserRepository.saveUser(userId, userProfile)
            } else {
                val finalName = displayName ?: email.substringBefore("@")
                // ✅ Nouvel utilisateur = MEMBER
                val newUser = User(
                    id = userId,
                    email = email,
                    displayName = finalName,
                    role = UserRole.MEMBER
                )
                userProfile = FirebaseUserRepository.createUserIfNotExists(userId, newUser)
            }
        }

        sessionManager.saveSession(userProfile)
        (activity as? MainActivity)?.updateNavigationForRole()
        activity?.invalidateOptionsMenu()

        try {
            FirebaseAuditLogRepository.logAction(
                userId = userProfile.id,
                userName = userProfile.displayName,
                action = AuditAction.LOGIN,
                details = logDetail
            )
        } catch (_: Exception) {}

        if (isAdded) {
            val welcomeMsg = if (userProfile.role == UserRole.ADMIN) {
                "Bienvenue ${userProfile.displayName} (Admin)"
            } else {
                "Bienvenue ${userProfile.displayName}"
            }
            hideLoading()
            Snackbar.make(binding.root, welcomeMsg, Snackbar.LENGTH_SHORT).show()
            navigateAfterLogin(userProfile.role)
        }
    }

    // ═══════════════════════════════════════════
    // Réinitialisation du mot de passe
    // ═══════════════════════════════════════════

    private fun showResetPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reset_password, null)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.et_reset_email)

        val currentEmail = binding.etLoginEmail.text.toString().trim()
        if (currentEmail.isNotBlank()) {
            etEmail.setText(currentEmail)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.auth_reset_password_title))
            .setMessage(getString(R.string.auth_reset_password_message))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.auth_reset_password_send)) { _, _ ->
                val email = etEmail.text.toString().trim()
                if (email.isNotBlank()) {
                    sendPasswordResetEmail(email)
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun sendPasswordResetEmail(email: String) {
        lifecycleScope.launch {
            try {
                supabaseAuth.resetPasswordForEmail(email)
                if (isAdded) {
                    Snackbar.make(
                        binding.root,
                        getString(R.string.auth_reset_password_sent),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                if (isAdded) {
                    showError("Erreur: ${e.localizedMessage}")
                }
            }
        }
    }

    // ═══════════════════════════════════════════
    // Navigation & UI helpers
    // ═══════════════════════════════════════════

    private fun navigateAfterLogin(role: UserRole) {
        when (role) {
            UserRole.ADMIN -> {
                try { findNavController().navigate(R.id.action_login_to_admin_dashboard) } catch (_: Exception) {
                    try { findNavController().navigate(R.id.nav_admin_dashboard) } catch (_: Exception) {}
                }
            }
            else -> {
                try { findNavController().navigate(R.id.action_login_to_home) } catch (_: Exception) {
                    try { findNavController().navigate(R.id.nav_home) } catch (_: Exception) {}
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressLogin.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Connexion..."
        try { binding.btnGoogleSignIn.isEnabled = false } catch (_: Exception) {}
    }

    private fun hideLoading() {
        if (!isAdded) return
        binding.progressLogin.visibility = View.GONE
        binding.btnLogin.isEnabled = true
        binding.btnLogin.text = getString(R.string.auth_login)
        try {
            if (GoogleSignInHelper.isConfigured()) {
                binding.btnGoogleSignIn.isEnabled = true
            }
        } catch (_: Exception) {}
    }

    private fun showError(message: String) {
        if (isAdded) {
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
