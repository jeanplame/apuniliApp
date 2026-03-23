package com.example.apuniliapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuditLog(
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("user_name")
    val userName: String = "",
    val action: AuditAction = AuditAction.LOGIN,
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun formattedDate(): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}
