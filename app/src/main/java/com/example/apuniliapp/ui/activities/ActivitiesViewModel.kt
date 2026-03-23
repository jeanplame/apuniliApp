package com.example.apuniliapp.ui.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apuniliapp.data.model.Activity
import com.example.apuniliapp.data.repository.firebase.FirebaseActivityRepository
import com.example.apuniliapp.utils.AppLogger
import kotlinx.coroutines.launch

class ActivitiesViewModel : ViewModel() {

    private val _allActivities = MutableLiveData<List<Activity>>()

    private val _searchQuery = MutableLiveData("")
    private val _selectedCategory = MutableLiveData("")

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Filtered list combining allActivities + searchQuery + selectedCategory
    val filteredActivities: LiveData<List<Activity>> = MediatorLiveData<List<Activity>>().apply {
        addSource(_allActivities) { applyFilters() }
        addSource(_searchQuery) { applyFilters() }
        addSource(_selectedCategory) { applyFilters() }
    }

    // All unique categories extracted from data
    val categories: LiveData<List<String>> = MediatorLiveData<List<String>>().apply {
        addSource(_allActivities) { list ->
            val cats = list?.map { it.category }
                ?.filter { it.isNotBlank() }
                ?.distinct()
                ?.sorted()
                ?: emptyList()
            value = cats
        }
    }

    init {
        loadActivities()
    }

    fun loadActivities() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val activitiesList = FirebaseActivityRepository.getAllActivities()
                _allActivities.value = activitiesList.sortedByDescending { it.createdAt }
                AppLogger.d("ActivitiesViewModel", "Activités chargées: ${activitiesList.size}")
            } catch (e: Exception) {
                val errorMsg = "Erreur lors du chargement des activités: ${e.message}"
                _errorMessage.value = errorMsg
                AppLogger.e(errorMsg, e, "ActivitiesViewModel")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    private fun MediatorLiveData<List<Activity>>.applyFilters() {
        val all = _allActivities.value ?: emptyList()
        val query = _searchQuery.value?.lowercase()?.trim() ?: ""
        val category = _selectedCategory.value ?: ""

        value = all.filter { activity ->
            val matchesQuery = query.isEmpty() ||
                    activity.title.lowercase().contains(query) ||
                    activity.description.lowercase().contains(query) ||
                    activity.location.lowercase().contains(query)
            val matchesCategory = category.isEmpty() || activity.category == category
            matchesQuery && matchesCategory
        }
    }

    fun addActivity(activity: Activity) {
        viewModelScope.launch {
            try {
                FirebaseActivityRepository.addActivity(activity)
                loadActivities()
                AppLogger.d("ActivitiesViewModel", "Activité ajoutée")
            } catch (e: Exception) {
                val errorMsg = "Erreur lors de l'ajout d'une activité"
                _errorMessage.value = errorMsg
                AppLogger.e(errorMsg, e, "ActivitiesViewModel")
            }
        }
    }

    fun deleteActivity(id: String) {
        viewModelScope.launch {
            try {
                FirebaseActivityRepository.deleteActivity(id)
                loadActivities()
                AppLogger.d("ActivitiesViewModel", "Activité supprimée: $id")
            } catch (e: Exception) {
                val errorMsg = "Erreur lors de la suppression de l'activité"
                _errorMessage.value = errorMsg
                AppLogger.e(errorMsg, e, "ActivitiesViewModel")
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
