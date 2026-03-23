package com.example.apuniliapp.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class MembershipStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}
