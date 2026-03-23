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
import com.example.apuniliapp.databinding.FragmentRegisterBinding
import com.example.apuniliapp.utils.GoogleSignInHelper
import com.example.apuniliapp.utils.SessionManager
import com.google.android.material.snackbar.Snackbar
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val supabaseAuth = SupabaseClient.client.auth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        // ══════ Configurer Google Sign-In ══════
        setupGoogleSignIn()

        // ══════ Bouton Inscription Email ══════
        binding.btnRegister.setOnClickListener {
            val name = binding.etRegisterName.text.toString().trim()
            val email = binding.etRegisterEmail.text.toString().trim()
            val password = binding.etRegisterPassword.text.toString().trim()
            val confirmPassword = binding.etRegisterConfirmPassword.text.toString().trim()

            // Validations
            when {
                name.isBlank() -> {
                    showError(getString(R.string.auth_name_required))
                    return@setOnClickListener
                }
                email.isBlank() -> {
                    showError("L'email est requis")
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    showError("Le mot de passe doit contenir au moins 6 caractères")
                    return@setOnClickListener
                }
                password != confirmPassword -> {
                    showError(getString(R.string.auth_password_mismatch))
                    return@setOnClickListener
                }
            }

            showLoading()
            registerWithEmail(name, email, password)
        }

        // ══════ Retour vers connexion ══════
        binding.btnGoLogin.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    // ═══════════════════════════════════════════
    // Google Sign-In
    // ═══════════════════════════════════════════

    private fun setupGoogleSignIn() {
        if (GoogleSignInHelper.isConfigured()) {
            binding.btnGoogleRegister.isEnabled = true
            binding.btnGoogleRegister.visibility = View.VISIBLE
            binding.btnGoogleRegister.setOnClickListener {
                showLoading()
                performGoogleSignIn()
            }
        } else {
            Log.w("RegisterFragment", "Google Sign-In non configuré (Client ID manquant)")
            binding.btnGoogleRegister.isEnabled = false
            binding.btnGoogleRegister.visibility = View.GONE
        }
    }

    private fun performGoogleSignIn() {
        lifecycleScope.launch {
            val result = GoogleSignInHelper.signIn(requireContext())
            when (result) {
                is GoogleSignInHelper.GoogleSignInResult.Success -> {
                    registerWithGoogleToken(
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

    private fun registerWithGoogleToken(idToken: String, displayName: String?, email: String?) {
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
                    handleSuccessfulRegistration(
                        userId = supabaseUser.id,
                        email = finalEmail,
                        displayName = finalDisplayName,
                        logDetail = "Inscription via Google"
                    )
                } else {
                    hideLoading()
                    showError("Erreur d'inscription avec Google")
                }
            } catch (e: Exception) {
                Log.e("RegisterFragment", "Erreur Google→Supabase: ${e.message}", e)
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
    // Inscription Email/Password via Supabase
    // ═══════════════════════════════════════════

    private fun registerWithEmail(name: String, email: String, password: String) {
        lifecycleScope.launch {
            try {
                supabaseAuth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    this.data = buildJsonObject {
                        put("display_name", name)
                    }
                }

                val supabaseUser = supabaseAuth.currentUserOrNull()
                if (supabaseUser != null) {
                    handleSuccessfulRegistration(
                        userId = supabaseUser.id,
                        email = supabaseUser.email ?: email,
                        displayName = name,
                        logDetail = "Inscription par email"
                    )
                } else {
                    hideLoading()
                    Snackbar.make(
                        binding.root,
                        "Compte créé ! Vérifiez votre email pour confirmer l'inscription.",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                if (isAdded) {
                    val errorMsg = when {
                        e.message?.contains("already registered") == true ||
                        e.message?.contains("email address is already") == true ->
                            "Un compte existe déjà avec cet email"
                        e.message?.contains("badly formatted") == true ||
                        e.message?.contains("invalid") == true ->
                            "Format d'email invalide"
                        e.message?.contains("weak password") == true ||
                        e.message?.contains("at least") == true ->
                            "Mot de passe trop faible (min. 6 caractères)"
                        e.message?.contains("network") == true ->
                            "Erreur réseau. Vérifiez votre connexion."
                        else -> "Erreur lors de l'inscription: ${e.localizedMessage}"
                    }
                    hideLoading()
                    showError(errorMsg)
                }
            }
        }
    }

    // ═══════════════════════════════════════════
    // Post-inscription (commun Email & Google)
    // ═══════════════════════════════════════════

    private suspend fun handleSuccessfulRegistration(
        userId: String,
        email: String,
        displayName: String,
        logDetail: String
    ) {
        var userProfile = FirebaseUserRepository.getUserByUidFromServer(userId)

        if (userProfile == null) {
            userProfile = User(
                id = userId,
                email = email,
                displayName = displayName,
                role = UserRole.MEMBER  // ✅ Nouvel utilisateur = MEMBER
            )
            userProfile = FirebaseUserRepository.createUserIfNotExists(userId, userProfile)
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
            hideLoading()
            Snackbar.make(binding.root, "Compte créé avec succès ! Bienvenue ${userProfile.displayName}", Snackbar.LENGTH_SHORT).show()
            navigateAfterRegistration(userProfile.role)
        }
    }

    private fun navigateAfterRegistration(role: UserRole) {
        when (role) {
            UserRole.ADMIN -> {
                try { findNavController().navigate(R.id.action_register_to_admin_dashboard) } catch (_: Exception) {
                    try { findNavController().navigate(R.id.nav_admin_dashboard) } catch (_: Exception) {}
                }
            }
            else -> {
                try { findNavController().navigate(R.id.action_register_to_home) } catch (_: Exception) {
                    try { findNavController().navigate(R.id.nav_home) } catch (_: Exception) {}
                }
            }
        }
    }

    // ═══════════════════════════════════════════
    // UI helpers
    // ═══════════════════════════════════════════

    private fun showLoading() {
        binding.progressRegister.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Inscription..."
        try { binding.btnGoogleRegister.isEnabled = false } catch (_: Exception) {}
    }

    private fun hideLoading() {
        if (!isAdded) return
        binding.progressRegister.visibility = View.GONE
        binding.btnRegister.isEnabled = true
        binding.btnRegister.text = getString(R.string.auth_create_account)
        try {
            if (GoogleSignInHelper.isConfigured()) {
                binding.btnGoogleRegister.isEnabled = true
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
