package com.example.apuniliapp.data.repository.firebase

import com.example.apuniliapp.config.SupabaseClient
import com.example.apuniliapp.data.model.Document
import com.example.apuniliapp.data.model.DocumentCategory
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

object FirebaseDocumentRepository {

    private val client = SupabaseClient.client
    private val json = Json { encodeDefaults = true }

    suspend fun getAllDocuments(): List<Document> {
        return try {
            client.from("documents").select {
                order("created_at", Order.DESCENDING)
            }.decodeList<Document>()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getDocumentById(id: String): Document? {
        return try {
            client.from("documents").select {
                filter { eq("id", id) }
                limit(1)
            }.decodeList<Document>().firstOrNull()
        } catch (e: Exception) { null }
    }

    suspend fun addDocument(document: Document): String? {
        return try {
            val data = document.toInsertJson()
            val result = client.from("documents").insert(data) {
                select()
            }.decodeSingle<Document>()
            result.id
        } catch (e: Exception) { null }
    }

    suspend fun updateDocument(id: String, document: Document) {
        try {
            client.from("documents").update(document.toInsertJson()) {
                filter { eq("id", id) }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun deleteDocument(id: String) {
        try {
            client.from("documents").delete {
                filter { eq("id", id) }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun Document.toInsertJson(): JsonObject {
        val element = json.encodeToJsonElement(this).jsonObject
        return JsonObject(element.filterKeys { it != "id" })
    }
}
