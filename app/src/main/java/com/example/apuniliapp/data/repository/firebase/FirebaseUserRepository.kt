package com.example.apuniliapp.data.repository.firebase

import android.util.Log
import com.example.apuniliapp.config.SupabaseClient
import com.example.apuniliapp.data.model.MembershipAdherenceStatus
import com.example.apuniliapp.data.model.User
import com.example.apuniliapp.data.model.UserRole
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Repository utilisateur connecté à Supabase Postgrest.
 */
object FirebaseUserRepository {

    private val client = SupabaseClient.client
    private const val TAG = "FirebaseUserRepository"

    suspend fun getUserByUid(uid: String): User? {
        return try {
            client.from("users").select {
                filter { eq("id", uid) }
                limit(1)
            }.decodeList<User>().firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur getUserByUid: ${e.message}")
            null
        }
    }

    suspend fun getUserByEmail(email: String): User? {
        return try {
            client.from("users").select {
                filter { eq("email", email.lowercase()) }
                limit(1)
            }.decodeList<User>().firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveUser(uid: String, user: User) {
        try {
            val json = buildUserJson(uid, user)
            Log.d(TAG, "Saving user: $json")
            client.from("users").upsert(json)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur saveUser: ${e.message}")
        }
    }

    suspend fun createUserIfNotExists(uid: String, user: User): User {
        return try {
            val existing = getUserByUid(uid)
            if (existing != null) {
                Log.d(TAG, "User exists, returning existing profile (Role: ${existing.role})")
                existing
            } else {
                // Utiliser le rôle fourni (normalement MEMBER), sauf si c'est l'admin par défaut
                val roleToSave = if (user.email.lowercase() == "admin@apunili.com") UserRole.ADMIN else user.role
                val finalUser = user.copy(id = uid, role = roleToSave)
                val json = buildUserJson(uid, finalUser)
                
                Log.d(TAG, "Creating new user profile: $json")
                client.from("users").insert(json)
                finalUser
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur createUserIfNotExists: ${e.message}")
            user
        }
    }

    suspend fun updateUserRole(uid: String, role: UserRole) {
        try {
            Log.d(TAG, "Updating user $uid to role $role")
            client.from("users").update(buildJsonObject {
                put("role", role.name)
            }) {
                filter { eq("id", uid) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur updateUserRole: ${e.message}")
        }
    }

    suspend fun updateUserMembershipStatus(uid: String, status: MembershipAdherenceStatus) {
        try {
            Log.d(TAG, "Updating user $uid membership_status to $status")
            client.from("users").update(buildJsonObject {
                put("membership_status", status.name)
            }) {
                filter { eq("id", uid) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur updateUserMembershipStatus: ${e.message}")
        }
    }

    suspend fun getUserByUidFromServer(uid: String): User? {
        return getUserByUid(uid)
    }

    /**
     * Met à jour un champ spécifique de l'utilisateur
     * @param uid ID de l'utilisateur
     * @param fieldName Nom du champ à mettre à jour
     * @param value Valeur (peut être String, Int, Long, Boolean, etc.)
     */
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    suspend fun updateUserField(uid: String, fieldName: String, value: Any?) {
        try {
            Log.d(TAG, "Updating user $uid field $fieldName to $value")
            client.from("users").update(buildJsonObject {
                when (value) {
                    is String -> put(fieldName, value)
                    is Int -> put(fieldName, value)
                    is Long -> put(fieldName, value)
                    is Boolean -> put(fieldName, value)
                    is Double -> put(fieldName, value)
                    null -> put(fieldName, null)
                    else -> put(fieldName, value.toString())
                }
            }) {
                filter { eq("id", uid) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur updateUserField: ${e.message}")
        }
    }

    private fun buildUserJson(uid: String, user: User): JsonObject {
        return buildJsonObject {
            put("id", uid)
            put("email", user.email.lowercase())
            put("display_name", user.displayName)
            put("photo_url", user.photoUrl)
            put("role", user.role.name)
            put("membership_status", user.membershipStatus.name) // ✅ IMPORTANT: envoyer le statut d'adhésion
            put("created_at", user.createdAt)
        }
    }
}
