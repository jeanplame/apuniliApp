package com.example.apuniliapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MembershipRequest(
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    val lastname: String = "",
    val postname: String = "",
    val firstname: String = "",
    val gender: String = "",
    val birthdate: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val profession: String = "",
    @SerialName("photo_url")
    val photoUrl: String = "",
    @SerialName("id_card_url")
    val idCardUrl: String = "",
    @SerialName("accepted_statutes")
    val acceptedStatutes: Boolean = false,
    @SerialName("accepted_roi")
    val acceptedROI: Boolean = false,
    @SerialName("accepted_values")
    val acceptedValues: Boolean = false,
    val status: MembershipStatus = MembershipStatus.PENDING,
    @SerialName("admin_comment")
    val adminComment: String = "",
    @SerialName("reviewed_at")
    val reviewedAt: Long = 0,
    @SerialName("reviewed_by")
    val reviewedBy: String = "",
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis()
)
