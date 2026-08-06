package com.LittleSmiles.com.ui.navigation

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LittleSmiles.com.core.domain.model.Entitlement
import com.LittleSmiles.com.core.domain.model.ProfileResult
import com.LittleSmiles.com.core.domain.model.User
import com.LittleSmiles.com.core.domain.repository.AuthRepository
import com.LittleSmiles.com.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Clean session orchestrator for 2026 Early Access.
 * Handles user profile validation and session states.
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel(), DefaultLifecycleObserver {

    private val _uiState = MutableStateFlow<NavigationState>(NavigationState.Loading)
    val uiState: StateFlow<NavigationState> = _uiState.asStateFlow()

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    private val _entitlement = MutableStateFlow(Entitlement.Default)
    val entitlement: StateFlow<Entitlement> = _entitlement.asStateFlow()

    init {
        registerProcessLifecycleObserver()
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {
            _uiState.value = NavigationState.Loading
            val uid = authRepository.currentUserId
            if (uid == null) {
                _userProfile.value = null
                _entitlement.value = Entitlement.Default
                _uiState.value = NavigationState.LoginRequired
                return@launch
            }
            validateUser(uid)
        }
    }

    private suspend fun validateUser(uid: String) {
        when (val result = userRepository.loadUserProfile(uid)) {
            is ProfileResult.Error -> {
                Timber.e(result.cause, "Profile load failed: %s", result.message)
                _uiState.value = NavigationState.Error(result.message)
            }
            is ProfileResult.Success -> {
                val profile = result.user
                if (profile == null) {
                    _uiState.value = NavigationState.LoginRequired
                    return
                }
                _userProfile.value = profile
                _entitlement.value = Entitlement.fromUser(profile)
                _uiState.value = NavigationState.Authenticated
            }
        }
    }

    fun refreshEntitlement() {
        viewModelScope.launch {
            val uid = authRepository.currentUserId ?: return@launch
            val result = userRepository.loadUserProfile(uid)
            if (result is ProfileResult.Success) {
                result.user?.let {
                    _userProfile.value = it
                    _entitlement.value = Entitlement.fromUser(it)
                }
            }
        }
    }

    fun logout() {
        authRepository.signOut()
        _userProfile.value = null
        _entitlement.value = Entitlement.Default
        _uiState.value = NavigationState.LoginRequired
    }

    override fun onStart(owner: LifecycleOwner) {
        // App returned to foreground - check session validity
        checkSession()
    }

    override fun onCleared() {
        super.onCleared()
        removeProcessLifecycleObserver()
    }

    private fun registerProcessLifecycleObserver() {
        runCatching {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }.onFailure { Timber.d(it, "Lifecycle registration skipped") }
    }

    private fun removeProcessLifecycleObserver() {
        runCatching {
            ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        }.onFailure { Timber.d(it, "Lifecycle removal skipped") }
    }
}

sealed class NavigationState {
    data object Loading : NavigationState()
    data object Authenticated : NavigationState()
    data object LoginRequired : NavigationState()
    data class Error(val message: String) : NavigationState()
}
