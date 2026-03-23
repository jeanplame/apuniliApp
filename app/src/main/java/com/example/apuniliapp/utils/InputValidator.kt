package com.example.apuniliapp.utils

import android.util.Patterns

/**
 * Validateur pour les inputs utilisateur
 */
object InputValidator {

    /**
     * Valide une adresse email
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Valide un mot de passe (minimum 6 caractères)
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * Valide un nombre de téléphone (non vide)
     */
    fun isValidPhone(phone: String): Boolean {
        return phone.isNotBlank() && phone.length >= 7
    }

    /**
     * Valide un nom/prénom (non vide, min 2 caractères)
     */
    fun isValidName(name: String): Boolean {
        return name.trim().length >= 2
    }

    /**
     * Valide un texte non vide
     */
    fun isNotEmpty(text: String): Boolean {
        return text.trim().isNotEmpty()
    }

    /**
     * Valide une URL
     */
    fun isValidUrl(url: String): Boolean {
        return url.isNotBlank() && Patterns.WEB_URL.matcher(url).matches()
    }

    /**
     * Valide un code postal
     */
    fun isValidPostalCode(code: String): Boolean {
        return code.isNotBlank() && code.length >= 3
    }

    /**
     * Valide une adresse
     */
    fun isValidAddress(address: String): Boolean {
        return address.trim().length >= 5
    }

    /**
     * Valide un numéro de carte d'identité (format simple)
     */
    fun isValidIdNumber(id: String): Boolean {
        return id.isNotBlank() && id.length >= 3
    }

    /**
     * Valide une profession/métier
     */
    fun isValidProfession(profession: String): Boolean {
        return profession.trim().length >= 2
    }
}

/**
 * Résultats de validation
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
) {
    companion object {
        fun valid() = ValidationResult(isValid = true)
        fun invalid(message: String) = ValidationResult(isValid = false, errorMessage = message)
    }
}

/**
 * Classe pour valider des formulaires complets
 */
class FormValidator {

    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.invalid("L'email est requis")
            !InputValidator.isValidEmail(email) -> ValidationResult.invalid("Email invalide")
            else -> ValidationResult.valid()
        }
    }

    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.invalid("Le mot de passe est requis")
            !InputValidator.isValidPassword(password) -> ValidationResult.invalid("Le mot de passe doit contenir au moins 6 caractères")
            else -> ValidationResult.valid()
        }
    }

    fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.invalid("Le nom est requis")
            !InputValidator.isValidName(name) -> ValidationResult.invalid("Le nom doit contenir au moins 2 caractères")
            else -> ValidationResult.valid()
        }
    }

    fun validatePhone(phone: String): ValidationResult {
        return when {
            phone.isBlank() -> ValidationResult.invalid("Le numéro de téléphone est requis")
            !InputValidator.isValidPhone(phone) -> ValidationResult.invalid("Numéro de téléphone invalide")
            else -> ValidationResult.valid()
        }
    }

    fun validateRequired(text: String, fieldName: String): ValidationResult {
        return when {
            !InputValidator.isNotEmpty(text) -> ValidationResult.invalid("$fieldName est requis")
            else -> ValidationResult.valid()
        }
    }
}

