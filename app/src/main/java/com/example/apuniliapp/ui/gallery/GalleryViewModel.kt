package com.example.apuniliapp.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apuniliapp.data.model.GalleryItem
import com.example.apuniliapp.data.repository.firebase.FirebaseGalleryRepository
import com.example.apuniliapp.utils.AppLogger
import kotlinx.coroutines.launch

class GalleryViewModel : ViewModel() {
    private val _allItems = MutableLiveData<List<GalleryItem>>()
    val allItems: LiveData<List<GalleryItem>> = _allItems

    private val _photos = MutableLiveData<List<GalleryItem>>()
    val photos: LiveData<List<GalleryItem>> = _photos

    private val _videos = MutableLiveData<List<GalleryItem>>()
    val videos: LiveData<List<GalleryItem>> = _videos

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _selectedType = MutableLiveData("all")
    val selectedType: LiveData<String> = _selectedType

    init {
        loadGalleryItems()
    }

    fun loadGalleryItems() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val all = FirebaseGalleryRepository.getAllGalleryItems()
                _allItems.value = all
                _photos.value = all.filter { it.type == "image" }
                _videos.value = all.filter { it.type == "video" }
                AppLogger.d("GalleryViewModel", "Galerie chargée: ${all.size}")
            } catch (e: Exception) {
                val errorMsg = "Erreur lors du chargement: ${e.message}"
                _errorMessage.value = errorMsg
                AppLogger.e(errorMsg, e, "GalleryViewModel")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterByType(type: String) {
        _selectedType.value = type
    }

    fun addItem(item: GalleryItem) {
        viewModelScope.launch {
            try {
                FirebaseGalleryRepository.addGalleryItem(item)
                loadGalleryItems()
            } catch (e: Exception) {
                val errorMsg = "Erreur lors de l'ajout"
                _errorMessage.value = errorMsg
                AppLogger.e(errorMsg, e, "GalleryViewModel")
            }
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            try {
                FirebaseGalleryRepository.deleteGalleryItem(id)
                loadGalleryItems()
            } catch (e: Exception) {
                val errorMsg = "Erreur lors de la suppression"
                _errorMessage.value = errorMsg
                AppLogger.e(errorMsg, e, "GalleryViewModel")
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
