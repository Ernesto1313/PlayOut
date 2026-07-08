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

    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess: StateFlow<Boolean> = _registrationSuccess

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
                authDataSource.logout()
                _isLoggedIn.value = false
                _registrationSuccess.value = true
            } else {
                _errorMessage.value = friendlyError(result.exceptionOrNull())
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _registrationSuccess.value = false
            val result = authDataSource.login(email, password)
            _isLoading.value = false
            if (result.isSuccess) {
                _isLoggedIn.value = true
                _isEmailVerified.value = authDataSource.isEmailVerified
                loadProfile()
            } else {
                _errorMessage.value = friendlyError(result.exceptionOrNull())
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

    fun clearRegistrationSuccess() { _registrationSuccess.value = false }

    private fun loadProfile() {
        viewModelScope.launch {
            _userProfile.value = authDataSource.getUserProfile()
        }
    }

    private fun friendlyError(e: Throwable?): String {
        val msg = e?.message ?: return "Something went wrong"
        return when {
            msg.contains("password is invalid", true) ||
            msg.contains("supplied auth credential", true) ||
            msg.contains("INVALID_LOGIN_CREDENTIALS", true) ->
                "Incorrect email or password"
            msg.contains("no user record", true) ->
                "No account found with this email"
            msg.contains("email address is already in use", true) ->
                "This email is already registered"
            msg.contains("badly formatted", true) ->
                "Please enter a valid email address"
            msg.contains("at least 6 characters", true) ->
                "Password must be at least 6 characters"
            msg.contains("Username already taken", true) ->
                "That username is already taken"
            msg.contains("network error", true) ->
                "Network error. Check your connection"
            else -> "Incorrect email or password"
        }
    }
}
