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
import com.LittleSmiles.com.core.domain.repository.BillingRepository
import com.LittleSmiles.com.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Session + entitlement orchestrator.
 *
 * Mandatory login phase: all content unlocked for verified users during 2026 Early Access.
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val billingRepository: BillingRepository,
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
        viewModelScope.launch {
            runCatching { billingRepository.startConnection() }
                .onFailure { Timber.e(it, "Initial billing connection failed") }
        }
        checkSession()
        /*
        viewModelScope.launch {
            billingRepository.isPremium.collect { premiumFromPlay ->
                if (premiumFromPlay) {
                    _userProfile.update { it?.copy(isPremium = true) }
                    _entitlement.value = Entitlement.fromUser(_userProfile.value)
                }
            }
        }
        */
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

    /** Mandatory Login Phase: Guest path is no longer supported. */

    private suspend fun validateUser(uid: String) {
        when (val result = userRepository.loadUserProfile(uid)) {
            is ProfileResult.Error -> {
                android.util.Log.e("AppViewModel", "Profile load failed", result.cause)
                _uiState.value = NavigationState.Error(result.message)
            }
            is ProfileResult.Success -> {
                val profile = result.user
                if (profile == null) {
                    _uiState.value = NavigationState.LoginRequired
                    return
                }
                _userProfile.value = profile
                runCatching { billingRepository.refreshPurchases() }
                val entitlement = Entitlement.fromUser(profile)
                _entitlement.value = entitlement
                // Soft freemium: always land on Menu when signed in with a profile.
                _uiState.value = NavigationState.Authenticated
            }
        }
    }

    fun refreshEntitlement() {
        viewModelScope.launch {
            val uid = authRepository.currentUserId ?: return@launch
            when (val result = userRepository.loadUserProfile(uid)) {
                is ProfileResult.Success -> {
                    result.user?.let {
                        _userProfile.value = it
                        _entitlement.value = Entitlement.fromUser(it)
                    }
                }
                is ProfileResult.Error -> Unit
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
        // Triggered every time the app enters the foreground
        viewModelScope.launch {
            runCatching { billingRepository.startConnection() }
                .onFailure { Timber.e(it, "Foreground billing refresh failed") }
        }
    }

    override fun onCleared() {
        super.onCleared()
        removeProcessLifecycleObserver()
    }

    private fun registerProcessLifecycleObserver() {
        runCatching {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }.onFailure { Timber.d(it, "ProcessLifecycleOwner registration skipped") }
    }

    private fun removeProcessLifecycleObserver() {
        runCatching {
            ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        }.onFailure { Timber.d(it, "ProcessLifecycleOwner removal skipped") }
    }
}

sealed class NavigationState {
    data object Loading : NavigationState()
    /** Signed-in user with profile (trial, free, or premium). */
    data object Authenticated : NavigationState()
    data object LoginRequired : NavigationState()
    data class Error(val message: String) : NavigationState()
}
