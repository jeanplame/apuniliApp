package com.example.apuniliapp

import android.app.Application
import android.util.Log

class ApuniliApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Capture uncaught exceptions to log them
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("ApuniliCrash", "=== UNCAUGHT EXCEPTION ===")
            Log.e("ApuniliCrash", "Thread: ${thread.name}")
            Log.e("ApuniliCrash", "Exception: ${throwable.message}", throwable)

            // Print full cause chain
            var cause = throwable.cause
            var depth = 1
            while (cause != null) {
                Log.e("ApuniliCrash", "Cause $depth: ${cause.message}", cause)
                cause = cause.cause
                depth++
            }

            // Forward to default handler
            defaultHandler?.uncaughtException(thread, throwable)
        }

        Log.d("ApuniliApp", "Application created successfully")
    }
}

