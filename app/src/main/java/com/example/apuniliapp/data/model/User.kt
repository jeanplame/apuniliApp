package com.example.apuniliapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val email: String = "",
    @SerialName("display_name")
    val displayName: String = "",
    @SerialName("photo_url")
    val photoUrl: String = "",
    @SerialName("password_hash")
    val passwordHash: String = "",
    val role: UserRole = UserRole.MEMBER,
    @SerialName("membership_status")
    val membershipStatus: MembershipAdherenceStatus = MembershipAdherenceStatus.NOT_SUBMITTED,
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
enum class UserRole {
    VISITOR,
    MEMBER,
    ADMIN
}

@Serializable
enum class MembershipAdherenceStatus {
    NOT_SUBMITTED,  // N'a pas encore rempli la fiche d'adhésion
    PENDING,        // A rempli la fiche, en attente d'approbation
    ACCEPTED,       // Demande approuvée
    REJECTED        // Demande rejetée
}


