package com.example.apuniliapp.ui.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apuniliapp.data.model.Event
import com.example.apuniliapp.data.repository.firebase.FirebaseEventRepository
import com.example.apuniliapp.utils.AppLogger
import kotlinx.coroutines.launch

class EventsViewModel : ViewModel() {
    private val _allEvents = MutableLiveData<List<Event>>()
    val allEvents: LiveData<List<Event>> = _allEvents

    private val _upcomingEvents = MutableLiveData<List<Event>>()
    val upcomingEvents: LiveData<List<Event>> = _upcomingEvents

    private val _pastEvents = MutableLiveData<List<Event>>()
    val pastEvents: LiveData<List<Event>> = _pastEvents

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _selectedFilter = MutableLiveData("all")
    val selectedFilter: LiveData<String> = _selectedFilter

    init {
        loadEvents()
    }

    fun loadEvents() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val events = FirebaseEventRepository.getAllEvents()
                _allEvents.value = events
                // Séparer les événements à venir et passés côté client
                _upcomingEvents.value = events // Pour l'instant, tous les événements
                _pastEvents.value = emptyList()
                AppLogger.d("EventsViewModel", "Événements chargés: ${events.size}")
            } catch (e: Exception) {
                val errorMsg = "Erreur lors du chargement des événements: ${e.message}"
                _errorMessage.value = errorMsg
                AppLogger.e(errorMsg, e, "EventsViewModel")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterByType(type: String) {
        _selectedFilter.value = type
        AppLogger.d("EventsViewModel", "Filtre appliqué: $type")
    }

    fun addEvent(event: Event) {
        viewModelScope.launch {
            try {
                FirebaseEventRepository.addEvent(event)
                loadEvents()
                AppLogger.d("EventsViewModel", "Événement ajouté")
            } catch (e: Exception) {
                val errorMsg = "Erreur lors de l'ajout de l'événement"
                _errorMessage.value = errorMsg
                AppLogger.e(errorMsg, e, "EventsViewModel")
            }
        }
    }

    fun deleteEvent(id: String) {
        viewModelScope.launch {
            try {
                FirebaseEventRepository.deleteEvent(id)
                loadEvents()
                AppLogger.d("EventsViewModel", "Événement supprimé: $id")
            } catch (e: Exception) {
                val errorMsg = "Erreur lors de la suppression de l'événement"
                _errorMessage.value = errorMsg
                AppLogger.e(errorMsg, e, "EventsViewModel")
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
