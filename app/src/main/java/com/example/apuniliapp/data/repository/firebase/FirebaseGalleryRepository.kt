package com.example.apuniliapp.data.repository.firebase

import com.example.apuniliapp.config.SupabaseClient
import com.example.apuniliapp.data.model.GalleryItem
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

object FirebaseGalleryRepository {

    private val client = SupabaseClient.client
    private val json = Json { encodeDefaults = true }

    suspend fun getAllGalleryItems(): List<GalleryItem> {
        return try {
            client.from("gallery").select {
                order("created_at", Order.DESCENDING)
            }.decodeList<GalleryItem>()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getItemsByType(type: String): List<GalleryItem> {
        return try {
            client.from("gallery").select {
                filter { eq("type", type) }
                order("created_at", Order.DESCENDING)
            }.decodeList<GalleryItem>()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun addGalleryItem(item: GalleryItem): String? {
        return try {
            val data = item.toInsertJson()
            val result = client.from("gallery").insert(data) {
                select()
            }.decodeSingle<GalleryItem>()
            result.id
        } catch (e: Exception) { null }
    }

    suspend fun updateGalleryItem(id: String, item: GalleryItem) {
        try {
            client.from("gallery").update(item.toInsertJson()) {
                filter { eq("id", id) }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun deleteGalleryItem(id: String) {
        try {
            client.from("gallery").delete {
                filter { eq("id", id) }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun GalleryItem.toInsertJson(): JsonObject {
        val element = json.encodeToJsonElement(this).jsonObject
        return JsonObject(element.filterKeys { it != "id" })
    }
}
