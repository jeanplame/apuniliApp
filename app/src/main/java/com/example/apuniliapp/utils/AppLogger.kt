package com.example.apuniliapp.utils

import android.util.Log

object AppLogger {
    private const val TAG = "ApuniliApp"
    private var isDebugEnabled = true

    fun setDebugEnabled(enabled: Boolean) {
        isDebugEnabled = enabled
    }

    fun d(message: String, tag: String = TAG) {
        if (isDebugEnabled) {
            Log.d(tag, message)
        }
    }

    fun i(message: String, tag: String = TAG) {
        Log.i(tag, message)
    }

    fun w(message: String, tag: String = TAG) {
        Log.w(tag, message)
    }

    fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }

    fun logEvent(eventName: String, details: Map<String, String> = emptyMap()) {
        val detailsStr = if (details.isNotEmpty()) {
            details.entries.joinToString(", ") { "${it.key}=${it.value}" }
        } else {
            ""
        }
        d("EVENT: $eventName ${if (detailsStr.isNotEmpty()) "- $detailsStr" else ""}")
    }

    fun logError(error: Throwable, context: String = "") {
        e("Error in $context: ${error.message}", error)
    }
}

