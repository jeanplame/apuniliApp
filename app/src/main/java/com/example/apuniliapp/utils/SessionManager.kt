package com.example.apuniliapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.apuniliapp.config.SupabaseClient
import com.example.apuniliapp.data.model.User
import com.example.apuniliapp.data.model.UserRole
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SessionManager(context: Context) {

    private val appContext: Context = context.applicationContext
    private val sharedPreferences: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val supabaseAuth = SupabaseClient.client.auth

    companion object {
        private const val PREFS_NAME = "apunili_session"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_MEMBERSHIP_STATUS = "user_membership_status"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LOGIN_TIME = "login_time"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LAST_FAILED_TIME = "last_failed_time"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRES_AT = "token_expires_at"
        private const val MAX_FAILED_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MS = 5 * 60 * 1000L // 5 minutes
        private const val SESSION_DURATION_MS = 24 * 60 * 60 * 1000L // 24 heures
        private const val TAG = "SessionManager"
    }

    /**
     * Sauvegarde la session localement (cache du profil).
     * Appelée après connexion Supabase Auth + lecture du profil.
     * Persiste aussi les tokens pour la reconnexion automatique.
     */
    fun saveSession(user: User) {
        // Token local valide 24h — le SessionRefreshWorker renouvelle toutes les 15 min
        val tokenExpiresAt = System.currentTimeMillis() + SESSION_DURATION_MS
        
        sharedPreferences.edit()
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER_EMAIL, user.email)
            .putString(KEY_USER_NAME, user.displayName)
            .putString(KEY_USER_ROLE, user.role.name)
            .putString(KEY_USER_MEMBERSHIP_STATUS, user.membershipStatus.name)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
            .putLong(KEY_TOKEN_EXPIRES_AT, tokenExpiresAt)
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .apply()
        
        Log.d(TAG, "Session sauvegardée pour ${user.email}, expire dans 24h")
    }

    /**
     * Retourne l'utilisateur connecté depuis le cache local.
     * Se base sur isLoggedIn() qui ne dépend que des SharedPreferences.
     */
    fun getLoggedInUser(): User? {
        if (!isLoggedIn()) return null

        val id = sharedPreferences.getString(KEY_USER_ID, null) ?: return null
        val email = sharedPreferences.getString(KEY_USER_EMAIL, null) ?: return null
        val name = sharedPreferences.getString(KEY_USER_NAME, null) ?: return null
        val roleStr = sharedPreferences.getString(KEY_USER_ROLE, null) ?: return null
        val membershipStatusStr = sharedPreferences.getString(KEY_USER_MEMBERSHIP_STATUS, "NOT_SUBMITTED") ?: "NOT_SUBMITTED"

        val role = try {
            UserRole.valueOf(roleStr)
        } catch (e: IllegalArgumentException) {
            UserRole.VISITOR
        }

        val membershipStatus = try {
            com.example.apuniliapp.data.model.MembershipAdherenceStatus.valueOf(membershipStatusStr)
        } catch (e: IllegalArgumentException) {
            com.example.apuniliapp.data.model.MembershipAdherenceStatus.NOT_SUBMITTED
        }

        return User(id = id, email = email, displayName = name, role = role, membershipStatus = membershipStatus)
    }

    /**
     * Vérifie si l'utilisateur est connecté.
     * Se base UNIQUEMENT sur les données locales (SharedPreferences) pour éviter
     * les faux négatifs quand Supabase Auth perd sa session en mémoire (app en background).
     * Le renouvellement réel du token Supabase est géré par SessionRefreshWorker.
     */
    fun isLoggedIn(): Boolean {
        val localLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!localLoggedIn) return false
        
        // Vérifier si la session locale n'est pas expirée
        val tokenExpiresAt = sharedPreferences.getLong(KEY_TOKEN_EXPIRES_AT, 0)
        if (tokenExpiresAt > System.currentTimeMillis()) {
            return true
        }
        
        // Session locale expirée (après 24h sans refresh), nettoyer
        Log.d(TAG, "Session locale expirée (24h), nettoyage")
        clearLocalSession()
        return false
    }


    /**
     * Renouvelle la session Supabase si elle est proche de l'expiration.
     * Prolonge aussi le token local de 24h à chaque renouvellement réussi.
     */
    suspend fun refreshSessionIfNeeded(): Boolean {
        return try {
            val tokenExpiresAt = sharedPreferences.getLong(KEY_TOKEN_EXPIRES_AT, 0)
            val timeUntilExpiry = tokenExpiresAt - System.currentTimeMillis()
            
            // Renouveler si expiration dans les 2 heures
            if (timeUntilExpiry < (2 * 60 * 60 * 1000)) {
                Log.d(TAG, "Renouvellement de session requis (expire dans ${timeUntilExpiry/1000}s)")
                
                try {
                    // Tenter de rafraîchir la session Supabase
                    supabaseAuth.refreshCurrentSession()
                    
                    // Prolonger le token local de 24h
                    val newExpiresAt = System.currentTimeMillis() + SESSION_DURATION_MS
                    sharedPreferences.edit()
                        .putLong(KEY_TOKEN_EXPIRES_AT, newExpiresAt)
                        .apply()
                    
                    Log.d(TAG, "Session renouvelée avec succès, prolongée de 24h")
                    return true
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur lors du renouvellement Supabase: ${e.message}")
                    // Même si Supabase échoue, prolonger le token local
                    // pour éviter la déconnexion brutale
                    val newExpiresAt = System.currentTimeMillis() + (1 * 60 * 60 * 1000) // 1h de grâce
                    sharedPreferences.edit()
                        .putLong(KEY_TOKEN_EXPIRES_AT, newExpiresAt)
                        .apply()
                    return true
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur refreshSessionIfNeeded: ${e.message}")
            false
        }
    }

    /**
     * Vérifie si la session est expirée.
     * Ne provoque PAS de nettoyage de session — utilisé uniquement pour l'affichage.
     */
    fun isSessionExpired(): Boolean {
        val localLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!localLoggedIn) return true
        
        val tokenExpiresAt = sharedPreferences.getLong(KEY_TOKEN_EXPIRES_AT, 0)
        return tokenExpiresAt <= System.currentTimeMillis()
    }

    fun isAccountLocked(): Boolean {
        val failedAttempts = sharedPreferences.getInt(KEY_FAILED_ATTEMPTS, 0)
        if (failedAttempts < MAX_FAILED_ATTEMPTS) return false

        val lastFailedTime = sharedPreferences.getLong(KEY_LAST_FAILED_TIME, 0)
        val elapsed = System.currentTimeMillis() - lastFailedTime
        if (elapsed > LOCKOUT_DURATION_MS) {
            sharedPreferences.edit().putInt(KEY_FAILED_ATTEMPTS, 0).apply()
            return false
        }
        return true
    }

    fun getRemainingLockoutSeconds(): Int {
        val lastFailedTime = sharedPreferences.getLong(KEY_LAST_FAILED_TIME, 0)
        val elapsed = System.currentTimeMillis() - lastFailedTime
        val remaining = LOCKOUT_DURATION_MS - elapsed
        return if (remaining > 0) (remaining / 1000).toInt() else 0
    }

    fun recordFailedAttempt() {
        val current = sharedPreferences.getInt(KEY_FAILED_ATTEMPTS, 0)
        sharedPreferences.edit()
            .putInt(KEY_FAILED_ATTEMPTS, current + 1)
            .putLong(KEY_LAST_FAILED_TIME, System.currentTimeMillis())
            .apply()
    }

    fun getFailedAttemptCount(): Int {
        return sharedPreferences.getInt(KEY_FAILED_ATTEMPTS, 0)
    }

    /**
     * Déconnexion : Supabase Auth + Google Credential + nettoyage du cache local
     */
    fun logout() {
        // Nettoyer la session locale immédiatement
        clearLocalSession()
        // Déconnexion Supabase + Google en arrière-plan (non-bloquant)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                supabaseAuth.signOut()
            } catch (_: Exception) {}
            try {
                GoogleSignInHelper.signOut(appContext)
            } catch (_: Exception) {}
        }
    }

    private fun clearLocalSession() {
        val failedAttempts = sharedPreferences.getInt(KEY_FAILED_ATTEMPTS, 0)
        val lastFailed = sharedPreferences.getLong(KEY_LAST_FAILED_TIME, 0)
        sharedPreferences.edit().clear()
            .putInt(KEY_FAILED_ATTEMPTS, failedAttempts)
            .putLong(KEY_LAST_FAILED_TIME, lastFailed)
            .apply()
    }

    fun getLoginTime(): Long {
        return sharedPreferences.getLong(KEY_LOGIN_TIME, 0)
    }

    fun getCurrentRole(): UserRole {
        if (!isLoggedIn()) return UserRole.VISITOR
        return getLoggedInUser()?.role ?: UserRole.VISITOR
    }

    fun isAdmin(): Boolean = getCurrentRole() == UserRole.ADMIN
    fun isMember(): Boolean = getCurrentRole() == UserRole.MEMBER
    fun isVisitor(): Boolean = getCurrentRole() == UserRole.VISITOR
}
