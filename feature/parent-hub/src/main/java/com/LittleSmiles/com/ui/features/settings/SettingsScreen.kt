package com.LittleSmiles.com.ui.features.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.LittleSmiles.com.ui.features.settings.components.*
import com.LittleSmiles.com.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userId: String,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val tts = viewModel.ttsManager
    val speechRate by viewModel.speechRate.collectAsState()
    val pitch by viewModel.pitch.collectAsState()
    val selectedVoiceName by viewModel.selectedVoiceName.collectAsState()
    val isVoiceFilterEnabled by viewModel.isVoiceFilterEnabled.collectAsState()
    
    LaunchedEffect(userId) { 
        viewModel.init(userId)
        viewModel.initVoice()
    }
    BackHandler { onBack() }

    val voices = remember(tts, isVoiceFilterEnabled) {
        val allVoices = tts.getVoices()
        if (isVoiceFilterEnabled) {
            allVoices.filter { 
                it.locale.toLanguageTag().startsWith("en-US", ignoreCase = true) || 
                it.locale.toLanguageTag().startsWith("en-IN", ignoreCase = true) 
            }.sortedBy { it.name }
        } else {
            allVoices.sortedBy { it.name }
        }
    }
    
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(HubBackground)) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { SectionHeader("SETTINGS") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HubLabel)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. App Voice Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        StickerCard(
                            modifier = Modifier.fillMaxWidth().clickable { expanded = true }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text("App Voice", color = HubLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Text("🐦", fontSize = 44.sp)
                            }
                        }
                        IconButton(
                            onClick = { viewModel.toggleVoiceFilter() },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(28.dp)
                                .background(if (isVoiceFilterEnabled) HubOrange else Color.LightGray, CircleShape)
                        ) {
                            Icon(Icons.Default.FilterList, "Filter", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }

                    StickerCard(
                        modifier = Modifier.weight(1f).clickable { tts.speak("I am ready to play and learn with you!") }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("Hear Demo", color = HubLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text("🔊", fontSize = 44.sp)
                        }
                    }
                }

                // 2. Speech Speed Slider
                StickerCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Speech Speed", color = HubLabel, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { viewModel.resetRate() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Rounded.RestartAlt, "Reset", tint = HubLabel.copy(alpha = 0.6f))
                        }
                    }
                    Slider(
                        value = speechRate,
                        onValueChange = { viewModel.updateRate(it) },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(thumbColor = HubOrange, activeTrackColor = HubOrange)
                    )
                    Text("${"%.1f".format(speechRate)}x Speed", color = HubLabel.copy(alpha = 0.8f), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }

                // 3. Voice Pitch Slider
                StickerCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Voice Pitch", color = HubLabel, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { viewModel.resetPitch() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Rounded.RestartAlt, "Reset", tint = HubLabel.copy(alpha = 0.6f))
                        }
                    }
                    Slider(
                        value = pitch,
                        onValueChange = { viewModel.updatePitch(it) },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(thumbColor = HubOrange, activeTrackColor = HubOrange)
                    )
                    Text("${"%.1f".format(pitch)}x Pitch", color = HubLabel.copy(alpha = 0.8f), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }

                if (expanded) {
                    VoiceSelectionModal(voices = voices, selectedVoiceName = selectedVoiceName, onVoiceSelected = { viewModel.updateVoice(it); expanded = false; tts.speak("Hello!") }, onDismiss = { expanded = false })
                }

                Text("Little Buds Academy v1.2.0 Stable", color = HubLabel.copy(alpha = 0.5f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun VoiceSelectionModal(voices: List<android.speech.tts.Voice>, selectedVoiceName: String?, onVoiceSelected: (android.speech.tts.Voice) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select App Voice", fontWeight = FontWeight.Bold) },
        text = {
            Box(Modifier.height(300.dp)) {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    voices.forEach { voice ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { onVoiceSelected(voice) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = voice.name == selectedVoiceName, onClick = { onVoiceSelected(voice) })
                            Text(voice.name, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
