package com.example.apuniliapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.apuniliapp.data.model.User
import com.example.apuniliapp.utils.AuthenticationService
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authService = AuthenticationService(application)

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    init {
        _currentUser.value = authService.getCurrentUser()
    }

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            val result = authService.login(email, password)
            when (result) {
                is AuthenticationService.AuthResult.Success -> {
                    _currentUser.value = result.user
                    _authState.value = AuthState.Success(result.user)
                }
                is AuthenticationService.AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                is AuthenticationService.AuthResult.Locked -> {
                    _authState.value = AuthState.Locked(result.message)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authService.logout()
            _currentUser.value = null
            _authState.value = AuthState.LoggedOut
        }
    }

    fun isLoggedIn(): Boolean = authService.isLoggedIn()

    fun isSessionExpired(): Boolean = authService.isSessionExpired()

    fun getCurrentUser(): User? = _currentUser.value

    sealed class AuthState {
        object Loading : AuthState()
        data class Success(val user: User) : AuthState()
        data class Error(val message: String) : AuthState()
        data class Locked(val message: String) : AuthState()
        object LoggedOut : AuthState()
    }
}
