package com.example.apuniliapp.data.repository.firebase

import com.example.apuniliapp.config.SupabaseClient
import com.example.apuniliapp.data.model.Event
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

object FirebaseEventRepository {

    private val client = SupabaseClient.client
    private val json = Json { encodeDefaults = true }

    suspend fun getAllEvents(): List<Event> {
        return try {
            client.from("events").select {
                order("created_at", Order.DESCENDING)
            }.decodeList<Event>()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getEventById(id: String): Event? {
        return try {
            client.from("events").select {
                filter { eq("id", id) }
                limit(1)
            }.decodeList<Event>().firstOrNull()
        } catch (e: Exception) { null }
    }

    suspend fun addEvent(event: Event): String? {
        return try {
            val data = event.toInsertJson()
            val result = client.from("events").insert(data) {
                select()
            }.decodeSingle<Event>()
            result.id
        } catch (e: Exception) { null }
    }

    suspend fun updateEvent(id: String, event: Event) {
        try {
            client.from("events").update(event.toInsertJson()) {
                filter { eq("id", id) }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun deleteEvent(id: String) {
        try {
            client.from("events").delete {
                filter { eq("id", id) }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun Event.toInsertJson(): JsonObject {
        val element = json.encodeToJsonElement(this).jsonObject
        return JsonObject(element.filterKeys { it != "id" })
    }
}
