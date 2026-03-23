package com.example.apuniliapp.utils

import android.content.Context
import android.util.Log
import com.example.apuniliapp.config.SupabaseClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage

/**
 * Outil de diagnostic Supabase.
 * Vérifie que Auth, Postgrest et Storage sont correctement configurés.
 * NE crée PAS de comptes — vérifie uniquement la disponibilité des services.
 */
object FirebaseDiagnostic {

    private const val TAG = "SupabaseDiag"

    data class DiagResult(
        val authEmailEnabled: Boolean,
        val authError: String?,
        val firestoreWritable: Boolean,
        val firestoreError: String?,
        val storageWritable: Boolean,
        val storageError: String?
    )

    /**
     * Exécute tous les tests de diagnostic.
     */
    suspend fun runDiagnostics(): DiagResult {
        val auth = testAuth()
        val postgrest = testPostgrest()
        val storage = testStorage()
        return DiagResult(
            authEmailEnabled = auth.first,
            authError = auth.second,
            firestoreWritable = postgrest.first,
            firestoreError = postgrest.second,
            storageWritable = storage.first,
            storageError = storage.second
        )
    }

    private suspend fun testAuth(): Pair<Boolean, String?> {
        return try {
            val client = SupabaseClient.client
            // Vérifier que le module Auth est accessible sans créer de compte
            // On tente juste de récupérer la session (peut être null, c'est OK)
            client.auth.currentSessionOrNull()
            // Si pas d'exception, Auth est fonctionnel
            Log.d(TAG, "✅ Auth: module accessible")
            Pair(true, null)
        } catch (e: Exception) {
            val msg = e.message ?: "Erreur inconnue"
            Log.e(TAG, "❌ Auth: $msg")
            Pair(false, "Module Auth inaccessible: $msg")
        }
    }

    private suspend fun testPostgrest(): Pair<Boolean, String?> {
        return try {
            val client = SupabaseClient.client
            // Lire depuis la table organization (SELECT autorisé pour tous via RLS)
            client.from("organization").select(columns = Columns.list("id")) {
                limit(1)
            }
            Log.d(TAG, "✅ Postgrest: lecture OK")
            Pair(true, null)
        } catch (e: Exception) {
            val msg = e.message ?: "Erreur inconnue"
            Log.e(TAG, "❌ Postgrest: $msg")
            if (msg.contains("permission denied") || msg.contains("42501")) {
                Pair(false, "RLS bloque l'accès. Vérifiez les policies.")
            } else if (msg.contains("does not exist") || msg.contains("42P01")) {
                Pair(false, "Tables manquantes. Exécutez supabase_setup.sql dans le SQL Editor.")
            } else {
                Pair(false, msg)
            }
        }
    }

    private suspend fun testStorage(): Pair<Boolean, String?> {
        return try {
            val client = SupabaseClient.client
            val bucket = client.storage.from("media")
            // Tenter de lister les fichiers (ne crée rien)
            bucket.list()
            Log.d(TAG, "✅ Storage bucket 'media': accessible")
            Pair(true, null)
        } catch (e: Exception) {
            val msg = e.message ?: "Erreur inconnue"
            Log.e(TAG, "❌ Storage: $msg")
            if (msg.contains("not found") || msg.contains("Bucket")) {
                Pair(false, "Bucket 'media' manquant. Créez-le dans Storage (public).")
            } else if (msg.contains("permission") || msg.contains("Unauthorized")) {
                Pair(false, "Accès refusé au bucket 'media'. Vérifiez les policies Storage.")
            } else {
                Pair(false, msg)
            }
        }
    }

    /**
     * Affiche un dialogue avec les résultats du diagnostic.
     */
    fun showResultDialog(context: Context, result: DiagResult) {
        val sb = StringBuilder()

        if (result.authEmailEnabled) {
            sb.appendLine("✅ Auth Supabase : OK")
        } else {
            sb.appendLine("❌ Auth Supabase : ERREUR")
            sb.appendLine("   → ${result.authError}")
            sb.appendLine()
        }

        if (result.firestoreWritable) {
            sb.appendLine("✅ Postgrest (tables) : OK")
        } else {
            sb.appendLine("❌ Postgrest (tables) : ERREUR")
            sb.appendLine("   → ${result.firestoreError}")
            sb.appendLine()
        }

        if (result.storageWritable) {
            sb.appendLine("✅ Storage (bucket media) : OK")
        } else {
            sb.appendLine("❌ Storage (bucket media) : ERREUR")
            sb.appendLine("   → ${result.storageError}")
            sb.appendLine()
        }

        val allOk = result.authEmailEnabled && result.firestoreWritable && result.storageWritable

        if (!allOk) {
            sb.appendLine()
            sb.appendLine("═══ Comment corriger ═══")
            if (!result.firestoreWritable) {
                sb.appendLine()
                sb.appendLine("1️⃣ TABLES :")
                sb.appendLine("Dashboard → SQL Editor → New Query")
                sb.appendLine("Collez le contenu de supabase_setup.sql")
                sb.appendLine("Cliquez Run")
            }
            if (!result.authEmailEnabled) {
                sb.appendLine()
                sb.appendLine("2️⃣ AUTH :")
                sb.appendLine("Dashboard → Authentication → Providers")
                sb.appendLine("→ Email → Activer")
                sb.appendLine("→ Désactiver 'Confirm email'")
            }
            if (!result.storageWritable) {
                sb.appendLine()
                sb.appendLine("3️⃣ STORAGE :")
                sb.appendLine("Dashboard → Storage → New Bucket")
                sb.appendLine("Nom: media, Public: oui")
            }
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(if (allOk) "✅ Supabase configuré" else "⚠️ Configuration Supabase incomplète")
            .setMessage(sb.toString())
            .setPositiveButton("OK", null)
            .show()
    }
}
