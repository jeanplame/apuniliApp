package com.example.apuniliapp.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class AuditAction(val label: String, val icon: String) {
    LOGIN("Connexion", "🔑"),
    LOGOUT("Déconnexion", "🚪"),
    MEMBERSHIP_APPROVED("Adhésion approuvée", "✅"),
    MEMBERSHIP_REJECTED("Adhésion refusée", "❌"),
    MEMBERSHIP_SUBMITTED("Demande d'adhésion soumise", "📝"),
    ACTIVITY_PUBLISHED("Activité publiée", "📢"),
    ACTIVITY_EDITED("Activité modifiée", "✏️"),
    ACTIVITY_UPDATED("Activité mise à jour", "📝"),
    ACTIVITY_DELETED("Activité supprimée", "🗑️"),
    EVENT_PUBLISHED("Événement publié", "📅"),
    EVENT_EDITED("Événement modifié", "✏️"),
    EVENT_UPDATED("Événement mis à jour", "📝"),
    EVENT_DELETED("Événement supprimé", "🗑️"),
    DOCUMENT_ADDED("Document ajouté", "📄"),
    DOCUMENT_EDITED("Document modifié", "✏️"),
    DOCUMENT_DELETED("Document supprimé", "🗑️"),
    MEMBER_ADDED("Membre ajouté", "👤"),
    MEMBER_EDITED("Membre modifié", "✏️"),
    MEMBER_DELETED("Membre supprimé", "🗑️"),
    GALLERY_ADDED("Média ajouté à la galerie", "🖼️"),
    GALLERY_EDITED("Média modifié dans la galerie", "✏️"),
    GALLERY_DELETED("Média supprimé de la galerie", "🗑️"),
    INFO_MODIFIED("Informations modifiées", "ℹ️"),
    STRUCTURE_MODIFIED("Structure modifiée", "🏛️"),
    DOCUMENT_VIEWED("Document consulté", "👁️")
}
