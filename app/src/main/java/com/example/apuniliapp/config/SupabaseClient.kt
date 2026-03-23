package com.example.apuniliapp.config

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

/**
 * Client Supabase singleton pour toute l'application.
 */
object SupabaseClient {

    const val SUPABASE_URL = "https://vatydmnvdxhllofoxszl.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZhdHlkbW52ZHhobGxvZm94c3psIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzMzOTIzMjMsImV4cCI6MjA4ODk2ODMyM30.Xo-yb0sTt_OGq5-N3zFy7EebLr2zPgosMoECu6ig86o"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Auth) {
            scheme = "apunili"
            host = "login"
        }
        install(Postgrest) {
            // Configuration for Postgrest (defaultHeaders removed in v3.x)
        }
        install(Storage) {
            // Configuration for storage
        }
    }
}
