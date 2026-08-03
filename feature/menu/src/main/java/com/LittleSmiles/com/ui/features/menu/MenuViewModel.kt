package com.LittleSmiles.com.ui.features.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LittleSmiles.com.core.domain.model.Entitlement
import com.LittleSmiles.com.core.domain.model.ProfileResult
import com.LittleSmiles.com.core.domain.model.User
import com.LittleSmiles.com.core.domain.repository.AuthRepository
import com.LittleSmiles.com.core.domain.repository.UserRepository
import com.LittleSmiles.com.core.util.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    val tts: TtsManager
) : ViewModel() {

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    private val _entitlement = MutableStateFlow(Entitlement.GuestFree)
    val entitlement: StateFlow<Entitlement> = _entitlement.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val uid = authRepository.currentUserId
            if (uid == null) {
                _userProfile.value = null
                _entitlement.value = Entitlement.GuestFree
                return@launch
            }
            when (val result = userRepository.loadUserProfile(uid)) {
                is ProfileResult.Success -> {
                    _userProfile.value = result.user
                    _entitlement.value = Entitlement.fromUser(result.user)
                }
                is ProfileResult.Error -> {
                    // Keep last known entitlement; avoid kicking kids out mid-play.
                    if (_userProfile.value == null) {
                        _entitlement.value = Entitlement.GuestFree.copy(isSignedIn = true)
                    }
                }
            }
        }
    }
}
