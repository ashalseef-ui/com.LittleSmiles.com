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
 * Soft freemium: expired trial users still reach Menu (free games).
 * Guests reach Menu with free games only (traffic / habit).
 * Locked games convert via Upgrade / Start Trial CTAs.
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

    private val _entitlement = MutableStateFlow(Entitlement.GuestFree)
    val entitlement: StateFlow<Entitlement> = _entitlement.asStateFlow()

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
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
                _entitlement.value = Entitlement.GuestFree
                _uiState.value = NavigationState.FreePlay
                return@launch
            }
            validateUser(uid)
        }
    }

    /** Guest / soft-free path from Login — keeps users in-app for organic growth. */
    fun continueAsFree() {
        authRepository.signOut()
        _userProfile.value = null
        _entitlement.value = Entitlement.GuestFree
        _uiState.value = NavigationState.FreePlay
    }

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
        _entitlement.value = Entitlement.GuestFree
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
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
    }
}

sealed class NavigationState {
    data object Loading : NavigationState()
    /** Signed-in user with profile (trial, free, or premium). */
    data object Authenticated : NavigationState()
    /** Not signed in — free activities only. */
    data object FreePlay : NavigationState()
    data object LoginRequired : NavigationState()
    data class Error(val message: String) : NavigationState()
}
