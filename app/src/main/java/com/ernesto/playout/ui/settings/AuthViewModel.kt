package com.ernesto.playout.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ernesto.playout.data.model.UserProfile
import com.ernesto.playout.data.remote.AuthDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authDataSource: AuthDataSource
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(authDataSource.isLoggedIn)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _isEmailVerified = MutableStateFlow(authDataSource.isEmailVerified)
    val isEmailVerified: StateFlow<Boolean> = _isEmailVerified

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        if (authDataSource.isLoggedIn) loadProfile()
    }

    fun register(email: String, password: String, username: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = authDataSource.register(email, password, username)
            _isLoading.value = false
            if (result.isSuccess) {
                _isLoggedIn.value = true
                _isEmailVerified.value = false
                loadProfile()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Registration failed"
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = authDataSource.login(email, password)
            _isLoading.value = false
            if (result.isSuccess) {
                _isLoggedIn.value = true
                _isEmailVerified.value = authDataSource.isEmailVerified
                loadProfile()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Login failed"
            }
        }
    }

    fun logout() {
        authDataSource.logout()
        _isLoggedIn.value = false
        _userProfile.value = null
    }

    fun resendVerification() {
        viewModelScope.launch {
            authDataSource.resendVerificationEmail()
        }
    }

    fun clearError() { _errorMessage.value = null }

    private fun loadProfile() {
        viewModelScope.launch {
            _userProfile.value = authDataSource.getUserProfile()
        }
    }
}
