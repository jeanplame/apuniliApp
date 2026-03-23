package com.example.apuniliapp.data.repository.firebase

import com.example.apuniliapp.config.SupabaseClient
import com.example.apuniliapp.data.model.TeamMember
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

object FirebaseStructureRepository {

    private val client = SupabaseClient.client
    private val json = Json { encodeDefaults = true }

    suspend fun getAllTeamMembers(): List<TeamMember> {
        return try {
            client.from("team_members").select {
                order("created_at", Order.DESCENDING)
            }.decodeList<TeamMember>()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun addTeamMember(member: TeamMember): String? {
        return try {
            val data = member.toInsertJson()
            val result = client.from("team_members").insert(data) {
                select()
            }.decodeSingle<TeamMember>()
            result.id
        } catch (e: Exception) { null }
    }

    suspend fun updateTeamMember(id: String, member: TeamMember) {
        try {
            client.from("team_members").update(member.toInsertJson()) {
                filter { eq("id", id) }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun deleteTeamMember(id: String) {
        try {
            client.from("team_members").delete {
                filter { eq("id", id) }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun TeamMember.toInsertJson(): JsonObject {
        val element = json.encodeToJsonElement(this).jsonObject
        return JsonObject(element.filterKeys { it != "id" })
    }
}
