package com.example.apuniliapp.ui.structure

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apuniliapp.data.model.TeamMember
import com.example.apuniliapp.data.repository.firebase.FirebaseStructureRepository
import com.example.apuniliapp.utils.AppLogger
import kotlinx.coroutines.launch

class StructureViewModel : ViewModel() {
    private val _teamMembers = MutableLiveData<List<TeamMember>>()
    val teamMembers: LiveData<List<TeamMember>> = _teamMembers

    private val _leadership = MutableLiveData<List<TeamMember>>()
    val leadership: LiveData<List<TeamMember>> = _leadership

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadTeamMembers()
    }

    private fun loadTeamMembers() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val members = FirebaseStructureRepository.getAllTeamMembers()
                _teamMembers.value = members
                // Filtrer les leaders (Direction)
                _leadership.value = members.filter {
                    it.department.contains("Direction", ignoreCase = true) ||
                    it.position.contains("Directeur", ignoreCase = true) ||
                    it.position.contains("Président", ignoreCase = true)
                }
                AppLogger.d("StructureViewModel", "Équipe chargée: ${members.size}")
            } catch (e: Exception) {
                val errorMsg = "Erreur lors du chargement de l'équipe: ${e.message}"
                _errorMessage.value = errorMsg
                AppLogger.e(errorMsg, e, "StructureViewModel")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
