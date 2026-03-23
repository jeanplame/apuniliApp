package com.example.apuniliapp.utils

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

/**
 * Helper pour l'authentification Google via Credential Manager.
 */
object GoogleSignInHelper {

    private const val TAG = "GoogleSignInHelper"

    // 🔑 IMPORTANT : Ce Client ID doit être de type "Web Application" dans la console Google Cloud
    // même pour une application Android.
    const val GOOGLE_WEB_CLIENT_ID = "730318398565-3fvt73p6lloeo7s2q9mnd1dhkv6qvqau.apps.googleusercontent.com"

    sealed class GoogleSignInResult {
        data class Success(val idToken: String, val displayName: String?, val email: String?) : GoogleSignInResult()
        data class Error(val message: String) : GoogleSignInResult()
        object Cancelled : GoogleSignInResult()
    }

    fun isConfigured(): Boolean {
        // On vérifie simplement que ce n'est pas vide et que ça ressemble à un client ID
        return GOOGLE_WEB_CLIENT_ID.isNotBlank() && GOOGLE_WEB_CLIENT_ID.contains(".apps.googleusercontent.com")
    }

    suspend fun signIn(context: Context): GoogleSignInResult {
        if (!isConfigured()) {
            return GoogleSignInResult.Error("Google Web Client ID non configuré.")
        }

        return try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(GOOGLE_WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                // On retire le nonce pour simplifier la première connexion réussie
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            handleSignInResult(result)
        } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
            GoogleSignInResult.Cancelled
        } catch (e: androidx.credentials.exceptions.NoCredentialException) {
            GoogleSignInResult.Error("Aucun compte Google trouvé. Veuillez en ajouter un dans les paramètres.")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur Google Sign-In: ${e.message}", e)
            GoogleSignInResult.Error("Erreur : ${e.localizedMessage ?: "Inconnue"}")
        }
    }

    private fun handleSignInResult(result: GetCredentialResponse): GoogleSignInResult {
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            return try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                GoogleSignInResult.Success(
                    idToken = googleIdTokenCredential.idToken,
                    displayName = googleIdTokenCredential.displayName,
                    email = googleIdTokenCredential.id
                )
            } catch (e: GoogleIdTokenParsingException) {
                GoogleSignInResult.Error("Erreur de parsing du token Google")
            }
        }
        return GoogleSignInResult.Error("Type d'authentification non supporté")
    }

    suspend fun signOut(context: Context) {
        try {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.w(TAG, "Erreur lors du clearing du credential state: ${e.message}")
        }
    }
}
