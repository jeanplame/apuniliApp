package com.example.apuniliapp.data.repository.firebase

import android.util.Log
import com.example.apuniliapp.config.SupabaseClient
import com.example.apuniliapp.data.model.MemberBenefit
import com.example.apuniliapp.data.model.MemberProfile
import com.example.apuniliapp.data.model.MembershipAdherenceStatus
import com.example.apuniliapp.data.model.MembershipHistory
import com.example.apuniliapp.data.model.PredefinedBenefits
import com.example.apuniliapp.data.model.User
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Repository pour les profils complets des membres
 * Agrège les données User + MembershipRequest + infos d'adhésion
 */
object FirebaseMemberProfileRepository {

    private val client = SupabaseClient.client
    private const val TAG = "MemberProfileRepository"

    /**
     * Récupère le profil complet d'un membre (User + Membership + Historique)
     */
    suspend fun getMemberProfile(userId: String): MemberProfile? {
        return try {
            // 1. Récupérer l'utilisateur
            val user = FirebaseUserRepository.getUserByUid(userId) ?: return null

            // 2. Récupérer la demande d'adhésion
            val membershipRequest = client.from("membership_requests").select {
                filter { eq("user_id", userId) }
                order("created_at", Order.DESCENDING)
                limit(1)
            }.decodeList<com.example.apuniliapp.data.model.MembershipRequest>().firstOrNull()

            // 3. Construire le profil complet
            MemberProfile(
                userId = user.id,
                email = user.email,
                displayName = user.displayName,
                photoUrl = user.photoUrl,
                firstname = membershipRequest?.firstname ?: "",
                lastname = membershipRequest?.lastname ?: "",
                postname = membershipRequest?.postname ?: "",
                gender = membershipRequest?.gender ?: "",
                birthdate = membershipRequest?.birthdate ?: "",
                address = membershipRequest?.address ?: "",
                phone = membershipRequest?.phone ?: "",
                profession = membershipRequest?.profession ?: "",
                idCardUrl = membershipRequest?.idCardUrl ?: "",
                membershipStatus = user.membershipStatus,
                membershipRequestDate = membershipRequest?.createdAt ?: 0,
                // Ces champs seront remplis depuis la BD si implémentés
                membershipApprovalDate = membershipRequest?.reviewedAt ?: 0,
                subscriptionStatus = if (user.membershipStatus == MembershipAdherenceStatus.ACCEPTED) "ACTIVE" else "INACTIVE",
                reviewedBy = membershipRequest?.reviewedBy ?: "",
                adminComment = membershipRequest?.adminComment ?: ""
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erreur getMemberProfile: ${e.message}")
            null
        }
    }

    /**
     * Récupère l'historique d'adhésion d'un membre
     */
    suspend fun getMembershipHistory(userId: String): List<MembershipHistory> {
        return try {
            client.from("membership_history").select {
                filter { eq("user_id", userId) }
                order("created_at", Order.DESCENDING)
            }.decodeList<MembershipHistory>()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur getMembershipHistory: ${e.message}")
            emptyList()
        }
    }

    /**
     * Ajoute un événement à l'historique d'adhésion
     */
    suspend fun addMembershipHistoryEvent(
        userId: String,
        eventType: String,
        description: String = "",
        createdBy: String = "SYSTEM"
    ) {
        try {
            val data = buildJsonObject {
                put("user_id", userId)
                put("event_type", eventType)
                put("description", description)
                put("created_by", createdBy)
                put("created_at", System.currentTimeMillis())
            }
            client.from("membership_history").insert(data)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur addMembershipHistoryEvent: ${e.message}")
        }
    }

    /**
     * Vérifie si l'adhésion d'un membre est active
     */
    suspend fun isMembershipActive(userId: String): Boolean {
        return try {
            val user = FirebaseUserRepository.getUserByUid(userId) ?: return false
            user.membershipStatus == MembershipAdherenceStatus.ACCEPTED
        } catch (e: Exception) {
            Log.e(TAG, "Erreur isMembershipActive: ${e.message}")
            false
        }
    }

    /**
     * Met à jour la date d'approbation de l'adhésion
     */
    suspend fun updateMembershipApprovalDate(userId: String, approvalDate: Long) {
        try {
            // Mettre à jour dans la table users (si colonne ajoutée)
            FirebaseUserRepository.updateUserField(userId, "membership_approval_date", approvalDate)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur updateMembershipApprovalDate: ${e.message}")
        }
    }

    /**
     * Met à jour le certificat URL après génération
     */
    suspend fun updateCertificateUrl(userId: String, certificateUrl: String) {
        try {
            FirebaseUserRepository.updateUserField(userId, "certificate_url", certificateUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur updateCertificateUrl: ${e.message}")
        }
    }

    /**
     * Met à jour le statut de cotisation
     */
    suspend fun updateSubscriptionStatus(userId: String, status: String) {
        try {
            FirebaseUserRepository.updateUserField(userId, "subscription_status", status)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur updateSubscriptionStatus: ${e.message}")
        }
    }

    /**
     * Récupère les bénéfices disponibles pour les membres
     */
    suspend fun getAvailableMemberBenefits(): List<MemberBenefit> {
        return try {
            // Pour la phase 1, retourner les bénéfices prédéfinis
            // Dans une phase future, charger depuis Supabase
            PredefinedBenefits.getAllBenefits()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur getAvailableMemberBenefits: ${e.message}")
            PredefinedBenefits.getAllBenefits()
        }
    }

    /**
     * Vérifie l'accès à un contenu spécifique pour un membre
     */
    suspend fun canAccessContent(userId: String, contentType: String): Boolean {
        return try {
            val isActiveMember = isMembershipActive(userId)
            // Pour la phase 1, tous les membres actifs ont accès à tout
            // Dans une phase future, implémentation de système de permissions fines
            isActiveMember
        } catch (e: Exception) {
            Log.e(TAG, "Erreur canAccessContent: ${e.message}")
            false
        }
    }

    /**
     * Récupère les contenus accessibles pour un membre (aperçu rapide)
     */
    suspend fun getMemberAccessibleContent(userId: String): Map<String, Int> {
        return try {
            val isActiveMember = isMembershipActive(userId)
            if (!isActiveMember) return emptyMap()

            mapOf(
                "gallery" to countGalleryItems(),
                "documents" to countAccessibleDocuments(),
                "events" to countUpcomingEvents(),
                "activities" to countActivities()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erreur getMemberAccessibleContent: ${e.message}")
            emptyMap()
        }
    }

    // Méthodes utilitaires pour compter les contenus
    private suspend fun countGalleryItems(): Int {
        return try {
            client.from("gallery").select { limit(1) }.decodeList<Any>().size
        } catch (e: Exception) { 0 }
    }

    private suspend fun countAccessibleDocuments(): Int {
        return try {
            client.from("documents").select { limit(1) }.decodeList<Any>().size
        } catch (e: Exception) { 0 }
    }

    private suspend fun countUpcomingEvents(): Int {
        return try {
            client.from("events").select { limit(1) }.decodeList<Any>().size
        } catch (e: Exception) { 0 }
    }

    private suspend fun countActivities(): Int {
        return try {
            client.from("activities").select { limit(1) }.decodeList<Any>().size
        } catch (e: Exception) { 0 }
    }
}

