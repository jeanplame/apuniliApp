package com.example.apuniliapp.config

/**
 * Configuration centralisée de l'application
 */
object AppConfig {

    // API Configuration
    const val API_BASE_URL = "https://api.apunili.com/"
    const val API_TIMEOUT = 30
    const val API_RETRY_ENABLED = true
    const val API_MAX_RETRIES = 3

    // Feature Flags
    const val ENABLE_ANALYTICS = true
    const val ENABLE_CRASH_REPORTING = true
    const val ENABLE_OFFLINE_MODE = true
    const val ENABLE_DARK_MODE = false

    // Authentication
    const val SESSION_TIMEOUT = 86400 // 24 heures en secondes
    const val PASSWORD_MIN_LENGTH = 6

    // UI Configuration
    const val ANIMATION_DURATION = 300
    const val SNACKBAR_DURATION = 2000

    // Pagination
    const val PAGE_SIZE = 20
    const val PRELOAD_SIZE = 5

    // File Configuration
    const val MAX_FILE_SIZE = 10485760 // 10 MB

    // Storage Configuration
    const val CACHE_DURATION = 86400000 // 24 heures

    // Test Data
    const val ENABLE_TEST_DATA = true

    // Limits
    const val MAX_MEMBERSHIP_REQUESTS = 100
    const val MAX_GALLERY_ITEMS = 1000
    const val MAX_DOCUMENTS = 200
}

