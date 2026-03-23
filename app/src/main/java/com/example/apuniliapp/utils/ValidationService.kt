package com.example.apuniliapp.utils

object ValidationService {
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() &&
               android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidPhone(phone: String): Boolean {
        return phone.isNotBlank() && phone.length >= 10
    }

    fun isValidName(name: String): Boolean {
        return name.isNotBlank() && name.length >= 2
    }

    fun isValidFullName(firstName: String, lastName: String): Boolean {
        return isValidName(firstName) && isValidName(lastName)
    }

    fun validateMembershipForm(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        profession: String,
        acceptedTerms: Boolean
    ): ValidationResult {
        return when {
            !isValidName(firstName) -> ValidationResult.Error("Prénom invalide")
            !isValidName(lastName) -> ValidationResult.Error("Nom invalide")
            !isValidEmail(email) -> ValidationResult.Error("Email invalide")
            !isValidPhone(phone) -> ValidationResult.Error("Téléphone invalide")
            profession.isBlank() -> ValidationResult.Error("Profession requise")
            !acceptedTerms -> ValidationResult.Error("Vous devez accepter les conditions")
            else -> ValidationResult.Success
        }
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}

