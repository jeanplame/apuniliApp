package com.example.apuniliapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Représentation d'un bénéfice disponible pour les membres
 */
@Serializable
data class MemberBenefit(
    val id: String = "",
    @SerialName("benefit_name")
    val benefitName: String = "",
    val description: String = "",
    @SerialName("icon_url")
    val iconUrl: String = "",
    val icon: MemberBenefitIcon = MemberBenefitIcon.LIBRARY,
    val isAccessible: Boolean = true
)

@Serializable
enum class MemberBenefitIcon {
    GALLERY,           // Galerie photos/vidéos
    DOCUMENT,          // Documents officiels
    EVENT,             // Événements et activités
    PRIORITY,          // Contact prioritaire
    CERTIFICATE,       // Certificat d'adhésion
    NETWORK,           // Réseau de membres
    SUPPORT,           // Support prioritaire
    LIBRARY            // Bibliothèque de ressources
}

/**
 * Énumération des bénéfices prédéfinis
 */
object PredefinedBenefits {
    val GALLERY = MemberBenefit(
        id = "gallery",
        benefitName = "Galerie Complète",
        description = "Accès à toutes les photos et vidéos des activités et événements",
        iconUrl = "",
        icon = MemberBenefitIcon.GALLERY,
        isAccessible = true
    )

    val DOCUMENTS = MemberBenefit(
        id = "documents",
        benefitName = "Documents Officiels",
        description = "Consultation et téléchargement des statuts, ROI et documents réglementaires",
        iconUrl = "",
        icon = MemberBenefitIcon.DOCUMENT,
        isAccessible = true
    )

    val EVENTS = MemberBenefit(
        id = "events",
        benefitName = "Événements et Activités",
        description = "Participation aux événements et consultation des activités de l'association",
        iconUrl = "",
        icon = MemberBenefitIcon.EVENT,
        isAccessible = true
    )

    val CERTIFICATE = MemberBenefit(
        id = "certificate",
        benefitName = "Certificat d'Adhésion",
        description = "Certificat numérique attestant votre adhésion à l'association",
        iconUrl = "",
        icon = MemberBenefitIcon.CERTIFICATE,
        isAccessible = true
    )

    val NETWORK = MemberBenefit(
        id = "network",
        benefitName = "Réseau de Membres",
        description = "Connexion avec les autres membres de l'association",
        iconUrl = "",
        icon = MemberBenefitIcon.NETWORK,
        isAccessible = true
    )

    val PRIORITY_CONTACT = MemberBenefit(
        id = "priority_contact",
        benefitName = "Contact Prioritaire",
        description = "Support et contact prioritaire avec l'administration",
        iconUrl = "",
        icon = MemberBenefitIcon.PRIORITY,
        isAccessible = true
    )

    fun getAllBenefits() = listOf(
        GALLERY,
        DOCUMENTS,
        EVENTS,
        CERTIFICATE,
        NETWORK,
        PRIORITY_CONTACT
    )
}

