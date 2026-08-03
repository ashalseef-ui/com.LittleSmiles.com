package com.LittleSmiles.com.ui.features.settings

import android.app.Application
import android.speech.tts.Voice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.LittleSmiles.com.core.domain.repository.DevPreferencesRepository
import com.LittleSmiles.com.core.util.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val devPrefs: DevPreferencesRepository,
    val ttsManager: TtsManager
) : AndroidViewModel(application) {

    private val _userId = MutableStateFlow<String?>(null)

    val speechRate = devPrefs.speechRate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    val pitch = devPrefs.voicePitch
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    val selectedVoiceName = devPrefs.selectedVoiceName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _isVoiceFilterEnabled = MutableStateFlow(true)
    val isVoiceFilterEnabled: StateFlow<Boolean> = _isVoiceFilterEnabled.asStateFlow()

    fun init(userId: String) {
        if (_userId.value == userId) return
        _userId.value = userId
    }

    fun verifyDataIntegrity(userId: String) {
        // No-op after cleanup
    }

    fun updateRate(rate: Float) {
        viewModelScope.launch {
            devPrefs.setSpeechRate(rate)
            ttsManager.setSpeechRate(rate)
        }
    }

    fun resetRate() {
        updateRate(1.0f)
    }

    fun updatePitch(pitch: Float) {
        viewModelScope.launch {
            devPrefs.setVoicePitch(pitch)
            ttsManager.setPitch(pitch)
        }
    }

    fun resetPitch() {
        updatePitch(1.0f)
    }

    fun updateVoice(voice: Voice) {
        viewModelScope.launch {
            devPrefs.setSelectedVoiceName(voice.name)
            ttsManager.setVoice(voice)
        }
    }

    fun toggleVoiceFilter() {
        _isVoiceFilterEnabled.value = !_isVoiceFilterEnabled.value
    }
    
    fun initVoice() {
        viewModelScope.launch {
            val name = devPrefs.selectedVoiceName.first()
            if (name.isNotEmpty()) {
                ttsManager.getVoices().find { it.name == name }?.let {
                    ttsManager.setVoice(it)
                }
            }
        }
    }
}
