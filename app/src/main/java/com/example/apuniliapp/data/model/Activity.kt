package com.example.apuniliapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Activity(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val location: String = "",
    val category: String = "",
    @SerialName("photo_urls")
    val photoUrls: List<String> = emptyList(),
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis()
)
