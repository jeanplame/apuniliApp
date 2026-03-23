package com.example.apuniliapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamMember(
    val id: String = "",
    val firstname: String = "",
    val lastname: String = "",
    val position: String = "",
    val email: String = "",
    val phone: String = "",
    @SerialName("photo_url")
    val photoUrl: String = "",
    val bio: String = "",
    val department: String = "",
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis()
)
