package com.example.apuniliapp.data.repository.firebase

import com.example.apuniliapp.config.SupabaseClient
import com.example.apuniliapp.data.model.AuditAction
import com.example.apuniliapp.data.model.AuditLog
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object FirebaseAuditLogRepository {

    private val client = SupabaseClient.client

    suspend fun logAction(userId: String, userName: String, action: AuditAction, details: String) {
        try {
            client.from("audit_logs").insert(buildJsonObject {
                put("user_id", userId)
                put("user_name", userName)
                put("action", action.name)
                put("details", details)
                put("timestamp", System.currentTimeMillis())
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getRecentLogs(count: Int = 50): List<AuditLog> {
        return try {
            client.from("audit_logs").select {
                order("timestamp", Order.DESCENDING)
                limit(count.toLong())
            }.decodeList<AuditLog>()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getLogsByAction(action: AuditAction): List<AuditLog> {
        return try {
            client.from("audit_logs").select {
                filter { eq("action", action.name) }
                order("timestamp", Order.DESCENDING)
            }.decodeList<AuditLog>()
        } catch (e: Exception) { emptyList() }
    }
}
