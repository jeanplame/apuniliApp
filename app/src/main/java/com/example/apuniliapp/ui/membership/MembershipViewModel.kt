package com.example.apuniliapp.ui.membership

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.apuniliapp.data.model.MemberBenefit
import com.example.apuniliapp.data.model.MemberProfile
import com.example.apuniliapp.data.model.MembershipAdherenceStatus
import com.example.apuniliapp.data.model.MembershipHistory
import com.example.apuniliapp.data.model.MembershipRequest
import com.example.apuniliapp.data.repository.firebase.FirebaseMemberProfileRepository
import com.example.apuniliapp.data.repository.firebase.FirebaseMembershipRepository
import com.example.apuniliapp.data.repository.firebase.FirebaseUserRepository
import com.example.apuniliapp.utils.SessionManager
import kotlinx.coroutines.launch

class MembershipViewModel(application: Application) : AndroidViewModel(application) {

    private val _membershipRequest = MutableLiveData<MembershipRequest?>()
    val membershipRequest: LiveData<MembershipRequest?> = _membershipRequest

    private val _memberProfile = MutableLiveData<MemberProfile?>()
    val memberProfile: LiveData<MemberProfile?> = _memberProfile

    private val _membershipHistory = MutableLiveData<List<MembershipHistory>>()
    val membershipHistory: LiveData<List<MembershipHistory>> = _membershipHistory

    private val _memberBenefits = MutableLiveData<List<MemberBenefit>>()
    val memberBenefits: LiveData<List<MemberBenefit>> = _memberBenefits

    private val _userMembershipStatus = MutableLiveData<MembershipAdherenceStatus?>()
    val userMembershipStatus: LiveData<MembershipAdherenceStatus?> = _userMembershipStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _requestSubmitted = MutableLiveData<Boolean>()
    val requestSubmitted: LiveData<Boolean> = _requestSubmitted

    private val _isMembershipActive = MutableLiveData<Boolean>()
    val isMembershipActive: LiveData<Boolean> = _isMembershipActive

    fun getMembershipRequestByUserId(userId: String) {
        if (userId.isBlank()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val requests = FirebaseMembershipRepository.getAllMemberships()
                val userRequest = requests.firstOrNull { it.userId == userId }
                _membershipRequest.value = userRequest
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitMembershipRequest(request: MembershipRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Vérifier qu'il n'y a pas déjà une demande PENDING
                val existingRequests = FirebaseMembershipRepository.getAllMemberships()
                val pendingRequest = existingRequests.firstOrNull { 
                    it.userId == request.userId && it.status.name == "PENDING"
                }
                
                if (pendingRequest != null) {
                    _errorMessage.value = "Vous avez déjà une demande en attente. Veuillez attendre l'approbation."
                    _isLoading.value = false
                    return@launch
                }
                
                val id = FirebaseMembershipRepository.addMembershipRequest(request)
                if (id != null) {
                    _membershipRequest.value = request.copy(id = id)
                    
                    // Mettre à jour le membershipStatus du User en PENDING
                    val user = FirebaseUserRepository.getUserByUidFromServer(request.userId)
                    if (user != null) {
                        FirebaseUserRepository.updateUserMembershipStatus(
                            request.userId, 
                            MembershipAdherenceStatus.PENDING
                        )
                        
                        // ✅ IMPORTANT: Recharger l'utilisateur depuis la BD pour mettre à jour la session
                        val updatedUser = FirebaseUserRepository.getUserByUidFromServer(request.userId)
                        if (updatedUser != null) {
                            val sessionManager = SessionManager(getApplication<Application>().applicationContext)
                            sessionManager.saveSession(updatedUser)
                        }
                    }
                    
                    _requestSubmitted.value = true
                } else {
                    _errorMessage.value = "Erreur lors de l'envoi de la demande"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

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
                
                if (profile != null) {
                    _isMembershipActive.value = profile.membershipStatus == MembershipAdherenceStatus.ACCEPTED
                } else {
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
     * Charge l'historique d'adhésion d'un membre
     */
    fun loadMembershipHistory(userId: String) {
        if (userId.isBlank()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val history = FirebaseMemberProfileRepository.getMembershipHistory(userId)
                _membershipHistory.value = history
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
                _membershipHistory.value = emptyList()
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
            _isLoading.value = true
            try {
                val benefits = FirebaseMemberProfileRepository.getAvailableMemberBenefits()
                _memberBenefits.value = benefits
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
                _memberBenefits.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Vérifie si l'adhésion d'un membre est active
     */
    fun checkMembershipStatus(userId: String) {
        if (userId.isBlank()) return
        
        viewModelScope.launch {
            try {
                val isActive = FirebaseMemberProfileRepository.isMembershipActive(userId)
                _isMembershipActive.value = isActive
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
                _isMembershipActive.value = false
            }
        }
    }
}
