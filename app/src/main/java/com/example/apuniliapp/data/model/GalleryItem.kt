package com.example.apuniliapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GalleryItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    @SerialName("image_url")
    val imageUrl: String = "",
    @SerialName("video_url")
    val videoUrl: String = "",
    val type: String = "image", // "image" ou "video"
    val category: String = "",
    @SerialName("album_id")
    val albumId: String = "",
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis()
)
