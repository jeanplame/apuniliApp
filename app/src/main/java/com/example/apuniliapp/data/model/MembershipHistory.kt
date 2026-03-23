package com.example.apuniliapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Enregistrement d'un événement dans l'historique d'adhésion
 */
@Serializable
data class MembershipHistory(
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("event_type")
    val eventType: String = "", // SUBMITTED, APPROVED, REJECTED, RENEWED, SUSPENDED, EXPIRED
    val description: String = "",
    @SerialName("created_by")
    val createdBy: String = "", // Qui a créé l'événement (admin ou système)
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
enum class MembershipEventType {
    SUBMITTED,      // Demande soumise
    APPROVED,       // Approuvée
    REJECTED,       // Rejetée
    RENEWED,        // Renouvellée
    SUSPENDED,      // Suspendue
    EXPIRED,        // Expirée
    REVOKED         // Révoquée
}

