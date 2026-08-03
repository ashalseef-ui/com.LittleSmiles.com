package com.LittleSmiles.com.core.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import com.LittleSmiles.com.core.domain.repository.DevPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

enum class SpeechPriority {
    LOW,    // Exploration taps ("Cow")
    MEDIUM, // Success feedback ("Great Job!")
    HIGH    // Instructional prompts ("Find the Cow")
}

/**
 * Manages Text-to-Speech lifecycle and operations.
 * Enhanced: Added buffering for early speech requests and setting persistence.
 */
class TtsManager(
    private val context: Context,
    private val devPrefs: DevPreferencesRepository
) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val speechQueue = mutableListOf<Pair<String, SpeechPriority>>()
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        initializeTts()
    }

    private fun initializeTts() {
        synchronized(this) {
            if (tts == null) {
                try {
                    tts = TextToSpeech(context.applicationContext, this)
                } catch (e: Exception) {
                    Log.e("TtsManager", "TTS initialization failed", e)
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            scope.launch {
                // Apply saved settings
                val rate = devPrefs.speechRate.first()
                val pitch = devPrefs.voicePitch.first()
                val voiceName = devPrefs.selectedVoiceName.first()
                
                tts?.setSpeechRate(rate)
                tts?.setPitch(pitch)
                
                if (voiceName.isNotEmpty()) {
                    tts?.voices?.find { it.name == voiceName }?.let {
                        tts?.voice = it
                    }
                }

                _isReady.value = true
                
                // Flush buffer
                synchronized(speechQueue) {
                    speechQueue.forEach { (text, priority) ->
                        speak(text, priority)
                    }
                    speechQueue.clear()
                }
            }
        } else {
            Log.e("TtsManager", "onInit failed with status $status")
            _isReady.value = false
        }
    }

    fun speak(text: String, priority: SpeechPriority = SpeechPriority.LOW) {
        if (!_isReady.value || tts == null) {
            synchronized(speechQueue) {
                if (speechQueue.size < 5) { // Cap buffer size
                    speechQueue.add(text to priority)
                }
            }
            if (tts == null) initializeTts()
            return
        }

        try {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
        } catch (e: Exception) {
            Log.e("TtsManager", "Speech failed", e)
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e("TtsManager", "Stop failed", e)
        }
    }

    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }

    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch)
    }

    fun setVoice(voice: Voice) {
        tts?.voice = voice
    }

    fun getVoices(): List<Voice> {
        return tts?.voices?.toList() ?: emptyList()
    }

    fun getCurrentVoice(): Voice? {
        return tts?.voice
    }

    fun release() {
        synchronized(this) {
            try {
                tts?.stop()
                tts?.shutdown()
            } catch (e: Exception) {
                Log.e("TtsManager", "Release failed", e)
            }
            tts = null
            _isReady.value = false
        }
    }
}
