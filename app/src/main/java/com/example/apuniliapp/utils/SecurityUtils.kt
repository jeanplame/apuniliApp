package com.example.apuniliapp.utils

import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64

/**
 * Utilitaires de sécurité pour le hachage des mots de passe
 * Utilise PBKDF2-like avec SHA-256 + salt pour une sécurité renforcée
 */
object SecurityUtils {

    private const val SALT = "ApuniliASBL_2026_Salt"

    /**
     * Hache un mot de passe avec SHA-256 + salt
     */
    fun hashPassword(password: String): String {
        val saltedPassword = "$SALT:$password"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(saltedPassword.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Vérifie un mot de passe contre un hash
     */
    fun verifyPassword(inputPassword: String, storedHash: String): Boolean {
        val inputHash = hashPassword(inputPassword)
        return inputHash == storedHash
    }

    /**
     * Génère un token de session aléatoire
     */
    fun generateSessionToken(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}

