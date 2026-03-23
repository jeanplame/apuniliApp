package com.example.apuniliapp.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

/**
 * Service de renouvellement de session Supabase en arrière-plan.
 * Renouvelle la session toutes les 15 minutes pour éviter l'expiration automatique.
 */
class SessionRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val sessionManager = SessionManager(applicationContext)
            
            // Vérifier si l'utilisateur est connecté
            if (!sessionManager.isLoggedIn()) {
                Log.d(TAG, "Utilisateur non connecté, skip refresh")
                return Result.success()
            }
            
            // Renouveler la session si nécessaire
            val refreshed = sessionManager.refreshSessionIfNeeded()
            
            if (refreshed) {
                Log.d(TAG, "Session rafraîchie avec succès")
                Result.success()
            } else {
                Log.w(TAG, "Échec du rafraîchissement, réessai")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur dans SessionRefreshWorker: ${e.message}")
            Result.retry()
        }
    }
    
    companion object {
        private const val TAG = "SessionRefreshWorker"
        private const val WORK_NAME = "session_refresh"
        
        /**
         * Enregistre le travail périodique de renouvellement de session.
         * À appeler une seule fois au démarrage de l'app.
         */
        fun startPeriodicRefresh(context: Context) {
            try {
                val refreshRequest = PeriodicWorkRequestBuilder<SessionRefreshWorker>(
                    15, // Renouveler toutes les 15 minutes
                    TimeUnit.MINUTES
                ).build()
                
                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                    refreshRequest
                )
                
                Log.d(TAG, "Travail de renouvellement de session enregistré")
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de l'enregistrement du travail: ${e.message}")
            }
        }
        
        /**
         * Arrête le travail périodique de renouvellement de session.
         * À appeler lors de la déconnexion.
         */
        fun stopPeriodicRefresh(context: Context) {
            try {
                WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
                Log.d(TAG, "Travail de renouvellement de session arrêté")
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de l'arrêt du travail: ${e.message}")
            }
        }
    }
}


