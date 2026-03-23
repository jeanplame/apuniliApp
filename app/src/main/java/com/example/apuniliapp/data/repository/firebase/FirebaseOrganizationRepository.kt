package com.example.apuniliapp.data.repository.firebase

import com.example.apuniliapp.config.SupabaseClient
import com.example.apuniliapp.data.model.OrganizationInfo
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

object FirebaseOrganizationRepository {

    private val client = SupabaseClient.client
    private val json = Json { encodeDefaults = true }

    suspend fun getOrganizationInfo(): OrganizationInfo {
        return try {
            val results = client.from("organization").select {
                limit(1)
            }.decodeList<OrganizationInfo>()
            results.firstOrNull() ?: OrganizationInfo()
        } catch (e: Exception) {
            OrganizationInfo()
        }
    }

    suspend fun updateOrganizationInfo(info: OrganizationInfo) {
        try {
            val data = json.encodeToJsonElement(
                info.copy(lastModified = System.currentTimeMillis())
            ).jsonObject

            // Upsert : met à jour si existe, crée sinon
            client.from("organization").upsert(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
