package com.example.apuniliapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Document(
    val id: String = "",
    val title: String = "",
    val type: String = "",
    val category: DocumentCategory = DocumentCategory.OTHER,
    val description: String = "",
    @SerialName("file_url")
    val fileUrl: String = "",
    @SerialName("file_size")
    val fileSize: String = "",
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis()
)
