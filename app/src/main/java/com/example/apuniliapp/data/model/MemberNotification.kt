package com.example.apuniliapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Notification de membre - Modèle de données
 */
@Serializable
data class MemberNotification(
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "", // APPROVAL, EVENT, RENEWAL, UPDATE, SYSTEM
    @SerialName("is_read")
    val isRead: Boolean = false,
    @SerialName("action_url")
    val actionUrl: String = "",
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @SerialName("read_at")
    val readAt: Long? = null
)

