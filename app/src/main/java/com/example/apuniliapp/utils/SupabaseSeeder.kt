package com.example.apuniliapp.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.apuniliapp.config.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 * Initialise les données de démonstration dans Supabase.
 */
object SupabaseSeeder {

    private const val TAG = "SupabaseSeeder"

    suspend fun seedIfNeeded(context: Context) {
        val client = SupabaseClient.client
        ensureStorageBucket()

        // Vérifier si déjà seeded
        val alreadySeeded = try {
            val result = client.from("activities").select(columns = Columns.list("id")) {
                limit(1)
            }.decodeList<Map<String, String>>()
            result.isNotEmpty()
        } catch (_: Exception) { false }

        if (alreadySeeded) {
            ensureAdminRoles()
            return
        }

        Log.d(TAG, "Tables vides → Insertion des données de test...")
        val now = System.currentTimeMillis()

        // ════════════════════════════════════════════════════════════
        // 1. COMPTE ADMIN UNIQUEMENT
        // ════════════════════════════════════════════════════════════
        val email = "admin@apunili.com"
        val password = "admin123"
        
        try {
            try { client.auth.signUpWith(Email) { this.email = email; this.password = password } } catch (_: Exception) {}
            
            val currentUser = client.auth.currentUserOrNull()
            val uid = currentUser?.id ?: "admin_manual_id"
            
            client.from("users").upsert(buildJsonObject {
                put("id", uid)
                put("email", email.lowercase())
                put("display_name", "Administrateur Apunili")
                put("role", "ADMIN")
                put("created_at", now)
            })
        } catch (e: Exception) {
            Log.e(TAG, "Erreur admin seeder: ${e.message}")
        }

        // On ne crée plus de compte 'membre' par défaut ici pour respecter le flux d'adhésion.

        // ════════════════════════════════════════════════════════════
        // 2. AUTRES DONNÉES (Activités, Événements, etc.)
        // ════════════════════════════════════════════════════════════
        seedOtherData(now)
    }

    private suspend fun seedOtherData(now: Long) {
        val client = SupabaseClient.client
        val oneDay = 86_400_000L
        
        try {
            // Organisation
            client.from("organization").upsert(buildJsonObject {
                put("id", "1")
                put("name", "Apunili ASBL")
                put("mission", "Développement communautaire et entraide sociale.")
                put("last_modified", now)
            })

            // Activités
            client.from("activities").insert(listOf(
                buildJsonObject { put("title", "Don aux orphelins"); put("date", "15 Mar 2026"); put("category", "Social"); put("created_at", now) },
                buildJsonObject { put("title", "Formation informatique"); put("date", "20 Fév 2026"); put("category", "Éducation"); put("created_at", now - oneDay) }
            ))

            // Documents
            client.from("documents").insert(listOf(
                buildJsonObject { put("title", "Statuts de l'association"); put("category", "STATUTES"); put("type", "PDF"); put("created_at", now) },
                buildJsonObject { put("title", "ROI"); put("category", "ROI"); put("type", "PDF"); put("created_at", now - oneDay) }
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Erreur seeding data: ${e.message}")
        }
    }

    private suspend fun ensureAdminRoles() {
        try {
            val client = SupabaseClient.client
            val adminUsers = client.from("users").select {
                filter { eq("email", "admin@apunili.com") }
                limit(1)
            }.decodeList<com.example.apuniliapp.data.model.User>()

            if (adminUsers.isNotEmpty()) {
                val adminUser = adminUsers.first()
                if (adminUser.role != com.example.apuniliapp.data.model.UserRole.ADMIN) {
                    client.from("users").update(buildJsonObject { put("role", "ADMIN") }) {
                        filter { eq("id", adminUser.id) }
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private suspend fun ensureStorageBucket() {
        try {
            val client = SupabaseClient.client
            val buckets = client.storage.retrieveBuckets()
            if (buckets.none { it.id == "media" }) {
                client.storage.createBucket("media") { public = true }
            }
        } catch (_: Exception) {}
    }
}
