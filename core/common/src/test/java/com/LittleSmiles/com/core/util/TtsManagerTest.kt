package com.LittleSmiles.com.core.util

import android.content.Context
import android.speech.tts.TextToSpeech
import com.LittleSmiles.com.core.domain.repository.DevPreferencesRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TtsManagerTest {

    @MockK(relaxed = true)
    lateinit var context: Context

    @MockK(relaxed = true)
    lateinit var devPrefs: DevPreferencesRepository

    private lateinit var ttsManager: TtsManager
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        
        every { context.applicationContext } returns context
        every { devPrefs.speechRate } returns flowOf(1.0f)
        every { devPrefs.voicePitch } returns flowOf(1.0f)
        every { devPrefs.selectedVoiceName } returns flowOf("")
        
        // Mocking the constructor of TextToSpeech
        mockkConstructor(TextToSpeech::class)
        every { anyConstructed<TextToSpeech>().speak(any(), any(), any(), any()) } returns 0
    }

    @Test
    fun `speak buffers requests when not ready`() = runTest {
        ttsManager = TtsManager(context, devPrefs)
        
        // Speak while not ready
        ttsManager.speak("Hello")
        
        // Verify internal speak was not called yet
        verify(exactly = 0) { anyConstructed<TextToSpeech>().speak(any(), any(), any(), any()) }
    }

    @Test
    fun `onInit SUCCESS flushes the buffer`() = runTest {
        ttsManager = TtsManager(context, devPrefs)
        ttsManager.speak("Buffered Text")
        
        // Simulate Android callback
        ttsManager.onInit(TextToSpeech.SUCCESS)
        advanceUntilIdle()
        
        // Verify the buffered text was spoken
        verify { anyConstructed<TextToSpeech>().speak("Buffered Text", any(), any(), any()) }
    }

    @Test
    fun `speak re-initializes after release`() = runTest {
        ttsManager = TtsManager(context, devPrefs)
        
        // Initial setup
        ttsManager.onInit(TextToSpeech.SUCCESS)
        advanceUntilIdle()
        
        // Release the engine
        ttsManager.release()
        
        // Reset mock to track new calls
        clearMocks(context, answers = false)
        
        // Calling speak should now trigger a new initialization
        ttsManager.speak("Recovered")
        
        // Verify initialization was triggered by checking if context was accessed
        verify { context.applicationContext }
    }
}
