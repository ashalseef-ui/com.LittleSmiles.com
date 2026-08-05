package com.LittleSmiles.com.ui.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LittleSmiles.com.core.domain.model.ProfileResult
import com.LittleSmiles.com.core.domain.model.User
import com.LittleSmiles.com.core.domain.repository.AuthRepository
import com.LittleSmiles.com.core.domain.repository.UserRepository
import com.LittleSmiles.com.core.util.analytics.AnalyticsEvents
import com.LittleSmiles.com.core.util.analytics.AnalyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _resendTimer = MutableStateFlow(0)
    val resendTimer: StateFlow<Int> = _resendTimer.asStateFlow()

    fun signIn(email: String, pass: String, deviceId: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val normalizedEmail = email.trim()
                val normalizedPass = pass.trim()
                val uid = authRepository.signIn(normalizedEmail, normalizedPass)
                if (!authRepository.isEmailVerified) {
                    _uiState.value = AuthUiState.Unverified(normalizedEmail)
                    return@launch
                }
                handleLogin(uid, normalizedEmail, deviceId)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Sign in failed")
            }
        }
    }

    private suspend fun handleLogin(uid: String, email: String, deviceId: String) {
        when (val result = userRepository.loadUserProfile(uid)) {
            is ProfileResult.Success -> {
                val profile = result.user
                if (profile != null) {
                    // Single Device Enforcement
                    if (profile.deviceId != null && profile.deviceId != deviceId) {
                        authRepository.signOut()
                        _uiState.value = AuthUiState.Error("Account already bound to another device.")
                        return
                    }

                    // Start trial if verified but not started yet
                    if (authRepository.isEmailVerified && !profile.hasTrialStarted) {
                        runCatching { userRepository.startTrial(uid, deviceId) }
                    } else if (profile.deviceId == null) {
                        // Bind device for existing verified users who haven't logged in since the lock was added
                        runCatching { userRepository.updateDeviceId(uid, deviceId) }
                    }

                    runCatching { userRepository.logLoginTimestamp(uid) }
                    _uiState.value = AuthUiState.Success
                } else {
                    // New profile (e.g. Google Sign-in or first sign-in)
                    val newUser = User(uid, email, null, null, false, 0)
                    userRepository.createUserProfile(newUser)
                    
                    if (authRepository.isEmailVerified) {
                        runCatching { userRepository.startTrial(uid, deviceId) }
                        analyticsHelper.logEvent(AnalyticsEvents.TRIAL_STARTED)
                    }
                    
                    runCatching { userRepository.logLoginTimestamp(uid) }
                    _uiState.value = AuthUiState.Success
                }
            }
            is ProfileResult.Error -> {
                _uiState.value = AuthUiState.Error(result.message)
            }
        }
    }

    fun signUp(email: String, pass: String, deviceId: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val normalizedEmail = email.trim()
                val normalizedPass = pass.trim()
                val uid = authRepository.signUp(normalizedEmail, normalizedPass)
                // Wait for the Auth state to stabilize before sending email
                kotlinx.coroutines.delay(1000) 
                authRepository.sendEmailVerification()
                startResendTimer()
                
                // Create profile with null trialStartDate and deviceId (pending verification)
                val newUser = User(uid, normalizedEmail, null, null, false, 0)
                userRepository.createUserProfile(newUser)
                
                _uiState.value = AuthUiState.Unverified(normalizedEmail)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Sign up failed")
            }
        }
    }

    fun resendVerification() {
        if (_resendTimer.value > 0) return
        
        viewModelScope.launch {
            try {
                authRepository.sendEmailVerification()
                _uiState.value = AuthUiState.Info("Verification email sent! Please check your inbox.")
                startResendTimer()
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Failed to resend email")
            }
        }
    }

    private fun startResendTimer() {
        viewModelScope.launch {
            _resendTimer.value = 30
            while (_resendTimer.value > 0) {
                kotlinx.coroutines.delay(1000)
                _resendTimer.value -= 1
            }
        }
    }

    fun refreshVerificationStatus(email: String, deviceId: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                authRepository.reloadUser()
                if (authRepository.isEmailVerified) {
                    val uid = authRepository.currentUserId ?: throw Exception("Not signed in")
                    handleLogin(uid, email, deviceId)
                } else {
                    _uiState.value = AuthUiState.Error("Email still not verified. Please check your inbox.")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Refresh failed")
            }
        }
    }

    fun resetPassword(email: String) {
        val normalizedEmail = email.trim()
        if (normalizedEmail.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your email address first.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                authRepository.resetPassword(normalizedEmail)
                _uiState.value = AuthUiState.Info("Reset link sent! Please check your inbox.")
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Failed to send reset email")
            }
        }
    }

    fun signInWithCredential(credential: Any, deviceId: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val uid = authRepository.signInWithCredential(credential)
                val normalizedEmail = (authRepository.currentUserEmail ?: "").trim()
                
                // Harden Google Login: check verification status
                if (!authRepository.isEmailVerified) {
                    _uiState.value = AuthUiState.Unverified(normalizedEmail)
                    return@launch
                }
                
                handleLogin(uid, normalizedEmail, deviceId)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Google sign in failed")
            }
        }
    }

    fun clearState() {
        _uiState.value = AuthUiState.Idle
    }

    fun setExternalError(message: String) {
        _uiState.value = AuthUiState.Error(message)
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Unverified(val email: String) : AuthUiState()
    data class Info(val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
