package com.example.apuniliapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrganizationInfo(
    val id: String = "1",
    val name: String = "",
    val mission: String = "",
    val vision: String = "",
    val objectives: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    val history: String = "",
    @SerialName("logo_url")
    val logoUrl: String = "",
    @SerialName("founded_date")
    val foundedDate: String = "",
    @SerialName("last_modified")
    val lastModified: Long = System.currentTimeMillis()
)
