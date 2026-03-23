package com.example.apuniliapp.utils

import android.content.Context
import android.util.Log
import com.example.apuniliapp.config.SupabaseClient
import com.example.apuniliapp.data.model.AuditAction
import com.example.apuniliapp.data.model.User
import com.example.apuniliapp.data.model.UserRole
import com.example.apuniliapp.data.repository.firebase.FirebaseAuditLogRepository
import com.example.apuniliapp.data.repository.firebase.FirebaseUserRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

class AuthenticationService(private val context: Context) {
    private val sessionManager = SessionManager(context)
    private val supabaseAuth = SupabaseClient.client.auth

    suspend fun login(email: String, password: String): AuthResult {
        if (sessionManager.isAccountLocked()) {
            val remaining = sessionManager.getRemainingLockoutSeconds()
            return AuthResult.Locked("Compte bloqué. Réessayez dans ${remaining}s")
        }

        return try {
            supabaseAuth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            val supabaseUser = supabaseAuth.currentUserOrNull()
            if (supabaseUser != null) {
                // ✅ VÉRIFICATION ET CRÉATION DU PROFIL SI MANQUANT
                var user = FirebaseUserRepository.getUserByUidFromServer(supabaseUser.id)
                if (user == null) {
                    val newUser = User(
                        id = supabaseUser.id,
                        email = supabaseUser.email ?: email,
                        displayName = supabaseUser.email?.substringBefore("@") ?: "Utilisateur",
                        role = UserRole.MEMBER // ✅ MEMBER par défaut
                    )
                    user = FirebaseUserRepository.createUserIfNotExists(supabaseUser.id, newUser)
                }
                
                sessionManager.saveSession(user)
                
                // ✅ DÉMARRER LE RENOUVELLEMENT AUTOMATIQUE DE SESSION
                SessionRefreshWorker.startPeriodicRefresh(context)
                Log.d(TAG, "Renouvellement de session activé à la connexion")
                
                FirebaseAuditLogRepository.logAction(
                    userId = user.id,
                    userName = user.displayName,
                    action = AuditAction.LOGIN,
                    details = "Connexion réussie"
                )
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Erreur de session Supabase")
            }
        } catch (e: Exception) {
            sessionManager.recordFailedAttempt()
            AuthResult.Error("Identifiants incorrects")
        }
    }

    // ... reste des méthodes
    suspend fun logout() {
        sessionManager.logout()
        // ✅ ARRÊTER LE RENOUVELLEMENT AUTOMATIQUE DE SESSION
        SessionRefreshWorker.stopPeriodicRefresh(context)
        Log.d(TAG, "Renouvellement de session arrêté à la déconnexion")
    }

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    fun getCurrentUser(): User? = sessionManager.getLoggedInUser()

    fun isSessionExpired(): Boolean = sessionManager.isSessionExpired()

    sealed class AuthResult {
        data class Success(val user: User) : AuthResult()
        data class Error(val message: String) : AuthResult()
        data class Locked(val message: String) : AuthResult()
    }

    companion object {
        private const val TAG = "AuthService"
    }
}
