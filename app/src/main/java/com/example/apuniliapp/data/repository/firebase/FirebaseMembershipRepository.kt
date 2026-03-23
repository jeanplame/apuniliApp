package com.example.apuniliapp.data.repository.firebase

import com.example.apuniliapp.config.SupabaseClient
import com.example.apuniliapp.data.model.MembershipAdherenceStatus
import com.example.apuniliapp.data.model.MembershipRequest
import com.example.apuniliapp.data.model.MembershipStatus
import com.example.apuniliapp.data.model.UserRole
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object FirebaseMembershipRepository {

    private val client = SupabaseClient.client

    suspend fun getAllMemberships(): List<MembershipRequest> {
        return try {
            client.from("membership_requests").select {
                order("created_at", Order.DESCENDING)
            }.decodeList<MembershipRequest>()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getPendingMemberships(): List<MembershipRequest> {
        return try {
            client.from("membership_requests").select {
                filter { eq("status", MembershipStatus.PENDING.name) }
                order("created_at", Order.DESCENDING)
            }.decodeList<MembershipRequest>()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getApprovedMemberships(): List<MembershipRequest> {
        return try {
            client.from("membership_requests").select {
                filter { eq("status", MembershipStatus.ACCEPTED.name) }
                order("created_at", Order.DESCENDING)
            }.decodeList<MembershipRequest>()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getMembershipById(id: String): MembershipRequest? {
        return try {
            client.from("membership_requests").select {
                filter { eq("id", id) }
                limit(1)
            }.decodeList<MembershipRequest>().firstOrNull()
        } catch (e: Exception) { null }
    }

    suspend fun addMembershipRequest(request: MembershipRequest): String? {
        return try {
            val data = buildJsonObject {
                put("user_id", request.userId)
                put("lastname", request.lastname)
                put("postname", request.postname)
                put("firstname", request.firstname)
                put("gender", request.gender)
                put("email", request.email)
                put("birthdate", request.birthdate)
                put("address", request.address)
                put("phone", request.phone)
                put("profession", request.profession)
                put("photo_url", request.photoUrl)
                put("id_card_url", request.idCardUrl)
                put("accepted_statutes", request.acceptedStatutes)
                put("accepted_roi", request.acceptedROI)
                put("accepted_values", request.acceptedValues)
                put("status", MembershipStatus.PENDING.name)
                put("created_at", System.currentTimeMillis())
            }
            val result = client.from("membership_requests").insert(data) {
                select()
            }.decodeSingle<MembershipRequest>()
            result.id
        } catch (e: Exception) { 
            e.printStackTrace()
            null 
        }
    }

    suspend fun approveMembership(request: MembershipRequest, comment: String = "", reviewerName: String = "") {
        try {
            // 1. Mettre à jour la demande
            client.from("membership_requests").update(buildJsonObject {
                put("status", MembershipStatus.ACCEPTED.name)
                put("admin_comment", comment)
                put("reviewed_at", System.currentTimeMillis())
                put("reviewed_by", reviewerName)
            }) {
                filter { eq("id", request.id) }
            }

            // 2. Mettre à jour le rôle ET le statut d'adhésion de l'utilisateur
            if (request.userId.isNotBlank()) {
                FirebaseUserRepository.updateUserRole(request.userId, UserRole.MEMBER)
                FirebaseUserRepository.updateUserMembershipStatus(
                    request.userId,
                    MembershipAdherenceStatus.ACCEPTED
                )
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun rejectMembership(id: String, userId: String = "", comment: String = "", reviewerName: String = "") {
        try {
            client.from("membership_requests").update(buildJsonObject {
                put("status", MembershipStatus.REJECTED.name)
                put("admin_comment", comment)
                put("reviewed_at", System.currentTimeMillis())
                put("reviewed_by", reviewerName)
            }) {
                filter { eq("id", id) }
            }

            // Mettre à jour le statut d'adhésion de l'utilisateur
            if (userId.isNotBlank()) {
                FirebaseUserRepository.updateUserMembershipStatus(
                    userId,
                    MembershipAdherenceStatus.REJECTED
                )
            }
        } catch (e: Exception) { e.printStackTrace() }
    }
}
