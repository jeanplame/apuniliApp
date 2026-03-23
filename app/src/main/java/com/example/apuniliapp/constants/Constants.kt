package com.example.apuniliapp.constants

object Constants {

    // SharedPreferences Keys
    object SharedPrefKeys {
        const val USER_ID = "user_id"
        const val USER_EMAIL = "user_email"
        const val USER_NAME = "user_name"
        const val USER_ROLE = "user_role"
        const val IS_LOGGED_IN = "is_logged_in"
        const val LOGIN_TIME = "login_time"
        const val THEME_MODE = "theme_mode"
        const val LANGUAGE = "language"
    }

    // Intent Extras
    object IntentExtras {
        const val USER_ID = "user_id"
        const val ACTIVITY_ID = "activity_id"
        const val EVENT_ID = "event_id"
        const val DOCUMENT_ID = "document_id"
        const val GALLERY_ID = "gallery_id"
    }

    // Bundle Keys
    object BundleKeys {
        const val USER_DATA = "user_data"
        const val MESSAGE = "message"
        const val ERROR = "error"
    }

    // Error Messages
    object ErrorMessages {
        const val NETWORK_ERROR = "Erreur réseau. Veuillez vérifier votre connexion."
        const val GENERIC_ERROR = "Une erreur est survenue. Veuillez réessayer."
        const val INVALID_EMAIL = "Adresse email invalide"
        const val INVALID_PASSWORD = "Mot de passe invalide"
        const val USER_NOT_FOUND = "Utilisateur non trouvé"
        const val UNAUTHORIZED = "Non autorisé"
        const val SERVER_ERROR = "Erreur serveur"
        const val TIMEOUT = "Délai d'attente dépassé"
    }

    // Success Messages
    object SuccessMessages {
        const val LOGIN_SUCCESS = "Connexion réussie"
        const val LOGOUT_SUCCESS = "Déconnexion réussie"
        const val SAVE_SUCCESS = "Enregistrement réussi"
        const val DELETE_SUCCESS = "Suppression réussie"
        const val UPDATE_SUCCESS = "Mise à jour réussie"
    }

    // Status Codes
    object StatusCodes {
        const val SUCCESS = 200
        const val CREATED = 201
        const val BAD_REQUEST = 400
        const val UNAUTHORIZED = 401
        const val FORBIDDEN = 403
        const val NOT_FOUND = 404
        const val SERVER_ERROR = 500
    }

    // Time Constants
    object TimeConstants {
        const val ONE_SECOND = 1000L
        const val ONE_MINUTE = 60 * ONE_SECOND
        const val ONE_HOUR = 60 * ONE_MINUTE
        const val ONE_DAY = 24 * ONE_HOUR
    }

    // Log Tags
    object LogTags {
        const val NAVIGATION = "Navigation"
        const val NETWORK = "Network"
        const val AUTH = "Auth"
        const val DATABASE = "Database"
        const val UI = "UI"
        const val LIFECYCLE = "Lifecycle"
    }
}

