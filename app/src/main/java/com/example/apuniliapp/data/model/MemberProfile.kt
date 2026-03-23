package com.example.apuniliapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Agrégation complète des données de profil d'un membre
 * Combine User + MembershipRequest + infos d'adhésion
 */
@Serializable
data class MemberProfile(
    @SerialName("user_id")
    val userId: String = "",
    val email: String = "",
    @SerialName("display_name")
    val displayName: String = "",
    @SerialName("photo_url")
    val photoUrl: String = "",
    
    // Infos personnelles du MembershipRequest
    val firstname: String = "",
    val lastname: String = "",
    val postname: String = "",
    val gender: String = "",
    val birthdate: String = "",
    val address: String = "",
    val phone: String = "",
    val profession: String = "",
    @SerialName("id_card_url")
    val idCardUrl: String = "",
    
    // Infos d'adhésion
    @SerialName("membership_status")
    val membershipStatus: MembershipAdherenceStatus = MembershipAdherenceStatus.NOT_SUBMITTED,
    
    // Dates clés
    @SerialName("membership_request_date")
    val membershipRequestDate: Long = 0, // Date de soumission
    @SerialName("membership_approval_date")
    val membershipApprovalDate: Long = 0, // Date d'approbation
    @SerialName("membership_expiry_date")
    val membershipExpiryDate: Long? = null, // Date d'expiration (optionnel)
    @SerialName("last_renewal_date")
    val lastRenewalDate: Long? = null,
    
    // Statut de cotisation
    @SerialName("subscription_status")
    val subscriptionStatus: String = "ACTIVE", // ACTIVE, PENDING_RENEWAL, EXPIRED, SUSPENDED
    
    // Certificat d'adhésion
    @SerialName("certificate_url")
    val certificateUrl: String = "",
    
    // Admin info
    @SerialName("reviewed_by")
    val reviewedBy: String = "",
    @SerialName("admin_comment")
    val adminComment: String = ""
)

