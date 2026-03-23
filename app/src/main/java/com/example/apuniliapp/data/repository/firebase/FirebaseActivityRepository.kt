package com.example.apuniliapp.data.repository.firebase

import com.example.apuniliapp.config.SupabaseClient
import com.example.apuniliapp.data.model.Activity
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

/**
 * Repository des activités connecté à Supabase Postgrest.
 * Table "activities"
 */
object FirebaseActivityRepository {

    private val client = SupabaseClient.client
    private val json = Json { encodeDefaults = true }

    suspend fun getAllActivities(): List<Activity> {
        return try {
            client.from("activities").select {
                order("created_at", Order.DESCENDING)
            }.decodeList<Activity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getActivityById(id: String): Activity? {
        return try {
            client.from("activities").select {
                filter { eq("id", id) }
                limit(1)
            }.decodeList<Activity>().firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addActivity(activity: Activity): String? {
        return try {
            val data = activity.toInsertJson()
            val result = client.from("activities").insert(data) {
                select()
            }.decodeSingle<Activity>()
            result.id
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateActivity(id: String, activity: Activity) {
        try {
            val data = activity.toInsertJson()
            client.from("activities").update(data) {
                filter { eq("id", id) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteActivity(id: String) {
        try {
            client.from("activities").delete {
                filter { eq("id", id) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun Activity.toInsertJson(): JsonObject {
        val element = json.encodeToJsonElement(this).jsonObject
        return JsonObject(element.filterKeys { it != "id" })
    }
}
