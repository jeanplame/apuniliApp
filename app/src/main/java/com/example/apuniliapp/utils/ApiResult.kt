package com.example.apuniliapp.utils

/**
 * Classe pour gérer les erreurs API et réseau
 */
sealed class ApiResult<T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error<T>(val exception: ApiException) : ApiResult<T>()
    class Loading<T> : ApiResult<T>()
}

/**
 * Exceptions personnalisées pour l'API
 */
sealed class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkException(message: String, cause: Throwable? = null) : ApiException(message, cause)
    class ParseException(message: String, cause: Throwable? = null) : ApiException(message, cause)
    class AuthException(message: String, cause: Throwable? = null) : ApiException(message, cause)
    class ServerException(val code: Int, message: String) : ApiException("Erreur serveur: $code - $message")
    class UnknownException(message: String, cause: Throwable? = null) : ApiException(message, cause)

    companion object {
        fun fromThrowable(throwable: Throwable): ApiException {
            return when (throwable) {
                is ApiException -> throwable
                else -> UnknownException(throwable.message ?: "Erreur inconnue", throwable)
            }
        }
    }
}

