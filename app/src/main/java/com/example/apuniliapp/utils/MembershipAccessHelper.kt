package com.example.apuniliapp.utils

import com.example.apuniliapp.data.model.MembershipAdherenceStatus
import com.example.apuniliapp.data.model.User

/**
 * Helper pour vérifier les permissions d'accès aux contenus
 * basé sur le statut d'adhésion et le rôle de l'utilisateur
 */
object MembershipAccessHelper {

    /**
     * Vérifie si un utilisateur peut accéder à la galerie
     */
    fun canAccessGallery(user: User?): Boolean {
        if (user == null) return false
        // Les visiteurs non-authentifiés ne peuvent pas accéder
        // Les membres acceptés peuvent accéder
        return user.membershipStatus == MembershipAdherenceStatus.ACCEPTED
    }

    /**
     * Vérifie si un utilisateur peut accéder aux documents
     */
    fun canAccessDocuments(user: User?): Boolean {
        if (user == null) return false
        // Les membres acceptés peuvent accéder
        return user.membershipStatus == MembershipAdherenceStatus.ACCEPTED
    }

    /**
     * Vérifie si un utilisateur peut accéder aux événements
     */
    fun canAccessEvents(user: User?): Boolean {
        if (user == null) return false
        // Les membres acceptés peuvent accéder
        return user.membershipStatus == MembershipAdherenceStatus.ACCEPTED
    }

    /**
     * Vérifie si un utilisateur peut télécharger le certificat
     */
    fun canDownloadCertificate(user: User?): Boolean {
        if (user == null) return false
        return user.membershipStatus == MembershipAdherenceStatus.ACCEPTED
    }

    /**
     * Vérifie si un utilisateur peut voir les activités
     */
    fun canAccessActivities(user: User?): Boolean {
        if (user == null) return false
        // Les membres acceptés peuvent accéder
        return user.membershipStatus == MembershipAdherenceStatus.ACCEPTED
    }

    /**
     * Récupère un message d'erreur d'accès refusé
     */
    fun getAccessDeniedMessage(contentType: String): String {
        return when (contentType.lowercase()) {
            "gallery" -> "Vous devez être membre accepté pour accéder à la galerie"
            "documents" -> "Vous devez être membre accepté pour consulter les documents"
            "events" -> "Vous devez être membre accepté pour participer aux événements"
            "activities" -> "Vous devez être membre accepté pour voir les activités"
            "certificate" -> "Vous devez être membre accepté pour télécharger votre certificat"
            else -> "Accès refusé. Vous devez être membre accepté."
        }
    }

    /**
     * Vérifie si l'adhésion est en cours de traitement
     */
    fun isMembershipPending(user: User?): Boolean {
        if (user == null) return false
        return user.membershipStatus == MembershipAdherenceStatus.PENDING
    }

    /**
     * Vérifie si l'adhésion a été rejetée
     */
    fun isMembershipRejected(user: User?): Boolean {
        if (user == null) return false
        return user.membershipStatus == MembershipAdherenceStatus.REJECTED
    }

    /**
     * Vérifie si l'adhésion a été acceptée
     */
    fun isMembershipAccepted(user: User?): Boolean {
        if (user == null) return false
        return user.membershipStatus == MembershipAdherenceStatus.ACCEPTED
    }

    /**
     * Récupère le texte du statut d'adhésion en français
     */
    fun getMembershipStatusText(user: User?): String {
        if (user == null) return "Non authentifié"
        return when (user.membershipStatus) {
            MembershipAdherenceStatus.NOT_SUBMITTED -> "Demande non soumise"
            MembershipAdherenceStatus.PENDING -> "En attente d'approbation"
            MembershipAdherenceStatus.ACCEPTED -> "Membre actif"
            MembershipAdherenceStatus.REJECTED -> "Demande rejetée"
        }
    }

    /**
     * Récupère l'emoji correspondant au statut
     */
    fun getMembershipStatusEmoji(user: User?): String {
        if (user == null) return "❓"
        return when (user.membershipStatus) {
            MembershipAdherenceStatus.NOT_SUBMITTED -> "📝"
            MembershipAdherenceStatus.PENDING -> "⏳"
            MembershipAdherenceStatus.ACCEPTED -> "✅"
            MembershipAdherenceStatus.REJECTED -> "❌"
        }
    }
}

