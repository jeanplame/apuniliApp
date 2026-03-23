package com.example.apuniliapp.utils

import android.content.Context
import androidx.navigation.NavController
import com.example.apuniliapp.R
import com.example.apuniliapp.data.model.UserRole
import com.google.android.material.snackbar.Snackbar
import android.view.View

/**
 * Garde d'accès admin - vérifie que l'utilisateur est un admin authentifié
 */
object AdminAccessGuard {

    /**
     * Vérifie l'accès admin. Retourne true si autorisé, false sinon.
     * Si non autorisé, redirige automatiquement vers le login.
     */
    fun checkAdminAccess(
        context: Context,
        navController: NavController,
        rootView: View? = null
    ): Boolean {
        val sessionManager = SessionManager(context)

        // Pas connecté
        if (!sessionManager.isLoggedIn()) {
            showMessage(rootView, "Veuillez vous connecter")
            navigateToLogin(navController)
            return false
        }

        // Session expirée
        if (sessionManager.isSessionExpired()) {
            sessionManager.logout()
            showMessage(rootView, "Session expirée. Veuillez vous reconnecter")
            navigateToLogin(navController)
            return false
        }

        // Vérifier le rôle
        val user = sessionManager.getLoggedInUser()
        if (user == null || user.role != UserRole.ADMIN) {
            showMessage(rootView, "Accès non autorisé")
            navigateToLogin(navController)
            return false
        }

        return true
    }

    private fun navigateToLogin(navController: NavController) {
        try {
            navController.navigate(R.id.nav_login)
        } catch (_: Exception) { }
    }

    private fun showMessage(rootView: View?, message: String) {
        rootView?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        }
    }
}

