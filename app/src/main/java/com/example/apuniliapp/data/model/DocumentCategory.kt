package com.example.apuniliapp.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class DocumentCategory(val label: String) {
    STATUTES("Statuts"),
    ROI("Règlement d'Ordre Intérieur"),
    NOTARIAL("Acte notarié"),
    REPORT("Rapport"),
    OTHER("Autre")
}
