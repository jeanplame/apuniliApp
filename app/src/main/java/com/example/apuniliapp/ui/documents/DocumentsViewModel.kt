package com.example.apuniliapp.ui.documents

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apuniliapp.data.model.Document
import com.example.apuniliapp.data.model.DocumentCategory
import com.example.apuniliapp.data.repository.firebase.FirebaseDocumentRepository
import com.example.apuniliapp.utils.AppLogger
import kotlinx.coroutines.launch

class DocumentsViewModel : ViewModel() {
    private val _documents = MutableLiveData<List<Document>>()
    val documents: LiveData<List<Document>> = _documents

    private val _allDocuments = MutableLiveData<List<Document>>()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _selectedCategory = MutableLiveData<DocumentCategory?>(null)
    val selectedCategory: LiveData<DocumentCategory?> = _selectedCategory

    init {
        loadDocuments()
    }

    fun loadDocuments() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val docs = FirebaseDocumentRepository.getAllDocuments()
                _allDocuments.value = docs
                applyFilter(docs)
                AppLogger.d("DocumentsViewModel", "Documents chargés: ${docs.size}")
            } catch (e: Exception) {
                val errorMsg = "Erreur lors du chargement des documents: ${e.message}"
                _errorMessage.value = errorMsg
                AppLogger.e(errorMsg, e, "DocumentsViewModel")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterByCategory(category: DocumentCategory?) {
        _selectedCategory.value = category
        _allDocuments.value?.let { applyFilter(it) }
    }

    private fun applyFilter(docs: List<Document>) {
        val category = _selectedCategory.value
        _documents.value = if (category == null) {
            docs
        } else {
            docs.filter { it.category == category }
        }
    }

    fun downloadDocument(id: String) {
        viewModelScope.launch {
            try {
                val doc = FirebaseDocumentRepository.getDocumentById(id)
                if (doc != null) {
                    AppLogger.d("DocumentsViewModel", "Téléchargement: ${doc.title}")
                }
            } catch (e: Exception) {
                val errorMsg = "Erreur lors du téléchargement"
                _errorMessage.value = errorMsg
                AppLogger.e(errorMsg, e, "DocumentsViewModel")
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
