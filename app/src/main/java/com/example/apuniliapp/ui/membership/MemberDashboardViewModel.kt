package com.example.apuniliapp.ui.membership

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.apuniliapp.data.model.MemberBenefit
import com.example.apuniliapp.data.model.MemberNotification
import com.example.apuniliapp.data.model.MemberProfile
import com.example.apuniliapp.data.model.MembershipHistory
import com.example.apuniliapp.data.repository.firebase.FirebaseMemberProfileRepository
import kotlinx.coroutines.launch

/**
 * ViewModel enrichi pour le dashboard avec statistiques et notifications
 */
class MemberDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val _memberProfile = MutableLiveData<MemberProfile?>()
    val memberProfile: LiveData<MemberProfile?> = _memberProfile

    private val _memberBenefits = MutableLiveData<List<MemberBenefit>>()
    val memberBenefits: LiveData<List<MemberBenefit>> = _memberBenefits

    private val _membershipHistory = MutableLiveData<List<MembershipHistory>>()
    val membershipHistory: LiveData<List<MembershipHistory>> = _membershipHistory

    private val _memberStats = MutableLiveData<MemberStats>()
    val memberStats: LiveData<MemberStats> = _memberStats

    private val _notifications = MutableLiveData<List<MemberNotification>>()
    val notifications: LiveData<List<MemberNotification>> = _notifications

    private val _membershipDaysRemaining = MutableLiveData<Int>()
    val membershipDaysRemaining: LiveData<Int> = _membershipDaysRemaining

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    /**
     * Charge le profil complet d'un membre accepté
     */
    fun loadMemberProfile(userId: String) {
        if (userId.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val profile = FirebaseMemberProfileRepository.getMemberProfile(userId)
                _memberProfile.value = profile
                if (profile == null) {
                    _errorMessage.value = "Impossible de charger le profil"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Charge les bénéfices disponibles pour les membres
     */
    fun loadMemberBenefits() {
        viewModelScope.launch {
            try {
                val benefits = FirebaseMemberProfileRepository.getAvailableMemberBenefits()
                _memberBenefits.value = benefits
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
                _memberBenefits.value = emptyList()
            }
        }
    }

    /**
     * Charge l'historique d'adhésion d'un membre
     */
    fun loadMembershipHistory(userId: String) {
        if (userId.isBlank()) return

        viewModelScope.launch {
            try {
                val history = FirebaseMemberProfileRepository.getMembershipHistory(userId)
                _membershipHistory.value = history
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
                _membershipHistory.value = emptyList()
            }
        }
    }

    /**
     * Charge les statistiques du membre
     */
    fun loadMemberStats(userId: String) {
        if (userId.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val profile = FirebaseMemberProfileRepository.getMemberProfile(userId)
                if (profile != null) {
                    // Calculer les stats
                    val stats = MemberStats(
                        memberSinceMonths = calculateMonthsSinceMembership(profile.membershipApprovalDate),
                        totalActivities = 12, // À récupérer de la BD
                        eventsAttended = 5,   // À récupérer de la BD
                        documentsDownloaded = 3, // À récupérer de la BD
                        membershipStatus = profile.membershipStatus.name,
                        nextRenewalDate = profile.membershipExpiryDate ?: 0
                    )
                    _memberStats.value = stats
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Charge les notifications du membre
     */
    fun loadNotifications(userId: String) {
        if (userId.isBlank()) return

        viewModelScope.launch {
            try {
                // Exemple de notifications de test
                val testNotifications = listOf(
                    MemberNotification(
                        id = "1",
                        userId = userId,
                        title = "Bienvenue sur Apunili!",
                        message = "Votre adhésion a été approuvée. Profitez de tous nos avantages!",
                        type = "APPROVAL",
                        isRead = false,
                        createdAt = System.currentTimeMillis()
                    ),
                    MemberNotification(
                        id = "2",
                        userId = userId,
                        title = "Nouvel événement",
                        message = "Une activité humanitaire est prévue le 20 mars",
                        type = "EVENT",
                        isRead = false,
                        createdAt = System.currentTimeMillis() - 86400000
                    ),
                    MemberNotification(
                        id = "3",
                        userId = userId,
                        title = "Renouvellement d'adhésion",
                        message = "Votre adhésion expire dans 30 jours. Veuillez la renouveler.",
                        type = "RENEWAL",
                        isRead = true,
                        createdAt = System.currentTimeMillis() - 172800000
                    )
                )
                _notifications.value = testNotifications
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
            }
        }
    }

    /**
     * Marque une notification comme lue
     */
    fun markNotificationAsRead(notificationId: String) {
        val current = _notifications.value?.toMutableList() ?: return
        val notification = current.find { it.id == notificationId }
        if (notification != null) {
            val index = current.indexOf(notification)
            current[index] = notification.copy(isRead = true)
            _notifications.value = current
        }
    }

    /**
     * Calcule le nombre de mois depuis l'adhésion
     */
    private fun calculateMonthsSinceMembership(approvalDate: Long): Int {
        if (approvalDate <= 0) return 0
        val now = System.currentTimeMillis()
        val diff = now - approvalDate
        return (diff / (1000 * 60 * 60 * 24 * 30)).toInt()
    }

    /**
     * Calcule les jours restants avant expiration
     */
    fun calculateDaysRemaining(expiryDate: Long): Int {
        if (expiryDate <= 0) return -1
        val now = System.currentTimeMillis()
        val diff = expiryDate - now
        val days = (diff / (1000 * 60 * 60 * 24)).toInt()
        _membershipDaysRemaining.value = days
        return days
    }
}

/**
 * Statistiques du membre
 */
data class MemberStats(
    val memberSinceMonths: Int,
    val totalActivities: Int,
    val eventsAttended: Int,
    val documentsDownloaded: Int,
    val membershipStatus: String,
    val nextRenewalDate: Long
)


