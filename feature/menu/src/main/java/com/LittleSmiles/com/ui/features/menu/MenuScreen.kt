package com.LittleSmiles.com.ui.features.menu

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.LittleSmiles.com.ui.theme.RainbowOrange
import com.LittleSmiles.com.core.domain.model.AccessTier
import com.LittleSmiles.com.core.domain.model.Entitlement
import com.LittleSmiles.com.core.domain.model.LearningActivityType
import com.LittleSmiles.com.core.navigation.Screen
import com.LittleSmiles.com.core.ui.R
import com.LittleSmiles.com.core.util.SpeechPriority
import com.LittleSmiles.com.ui.components.LittleBudsTopAppBar
import com.LittleSmiles.com.ui.components.MenuData
import com.LittleSmiles.com.ui.components.ParentalGateDialog
import com.LittleSmiles.com.ui.components.ParentalGateStrength
import com.LittleSmiles.com.ui.components.squishClickable
import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    navController: NavController,
    onLogout: () -> Unit,
    viewModel: MenuViewModel = hiltViewModel()
) {
    val entitlement by viewModel.entitlement.collectAsState()
    val tts = viewModel.tts
    val context = LocalContext.current

    var showParentalGate by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refresh()
        tts.speak(
            "Hi! Welcome to Little Buds Academy! What do you want to play today?",
            SpeechPriority.HIGH
        )
    }

    BackHandler { showExitDialog = true }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit Little Buds Academy") },
            text = { Text("Are you sure you want to leave?") },
            confirmButton = {
                TextButton(onClick = { (context as? Activity)?.finishAffinity() }) {
                    Text("Exit", color = Color(0xFFE11D48))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("Stay") }
            }
        )
    }

    if (showParentalGate) {
        ParentalGateDialog(
            onDismiss = { showParentalGate = false },
            onSuccess = {
                showParentalGate = false
                navController.navigate(Screen.ParentalHub.route)
            }
        )
    }

    val items = remember {
        listOf(
            MenuData(LearningActivityType.Tracing, Color(0xFF3B82F6)),
            MenuData(LearningActivityType.Colors, Color(0xFFEF4444)),
            MenuData(LearningActivityType.Shapes, Color(0xFFEAB308)),
            MenuData(LearningActivityType.Animals, Color(0xFFA855F7)),
            MenuData(LearningActivityType.Routines, Color(0xFF22C55E)),
            MenuData(LearningActivityType.Opposites, Color(0xFFF97316)),
            MenuData(LearningActivityType.BodyParts, Color(0xFF06B6D4)),
            MenuData(LearningActivityType.Emotions, Color(0xFFEC4899)),
            MenuData(LearningActivityType.Matching, Color(0xFF84CC16)),
            MenuData(LearningActivityType.Drawing, Color(0xFFF59E0B))
        )
    }

    Scaffold(
        topBar = {
            LittleBudsTopAppBar(
                navigationIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "Little Buds Academy",
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(45.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                },
                titleContent = { BrandingTitle(entitlement) },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        IconButton(
                            onClick = { showParentalGate = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Parental Menu",
                                tint = Color(0xFF0369A1),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            )
        },
        containerColor = Color(0xFFF0F9FF)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFECFDF5),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = "✨ EARLY ACCESS 2026: ALL GAMES UNLOCKED ✨",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = Color(0xFF047857),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items, key = { it.activity.id }) { item ->
                    var isNavigating by remember { mutableStateOf(false) }

                    MenuButton(
                        data = item,
                        locked = false,
                        animateBorder = true
                    ) {
                        if (isNavigating) return@MenuButton
                        isNavigating = true
                        tts.speak("Let's play ${item.title}!", SpeechPriority.MEDIUM)
                        try {
                            navController.navigate(item.route) { launchSingleTop = true }
                        } catch (_: Exception) {
                            isNavigating = false
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BrandingTitle(entitlement: Entitlement) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val titleText = "Little Buds Academy"
        val colors = listOf(
            Color(0xFFE11D48), Color(0xFFFB923C), Color(0xFFFACC15),
            Color(0xFF4ADE80), Color(0xFF60A5FA), Color(0xFFA855F7)
        )
        Row(horizontalArrangement = Arrangement.Center) {
            titleText.split(" ").forEach { word ->
                Row {
                    word.forEachIndexed { index, char ->
                        Text(
                            text = char.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = colors[index % colors.size]
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
            }
        }

        val subtitle = "2026 EARLY ACCESS"
        Text(
            text = subtitle,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = RainbowOrange,
            modifier = Modifier.padding(top = 0.dp)
        )
    }
}

@Composable
fun MenuButton(
    data: MenuData,
    locked: Boolean,
    animateBorder: Boolean,
    onClick: () -> Unit
) {
    if (animateBorder && !locked) {
        AnimatedMenuButton(data = data, locked = false, onClick = onClick)
    } else {
        StaticMenuButton(data = data, locked = locked, onClick = onClick)
    }
}

@Composable
private fun AnimatedMenuButton(
    data: MenuData,
    locked: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "borderAnimation")
    val borderColor by infiniteTransition.animateColor(
        initialValue = Color.White.copy(alpha = 0.5f),
        targetValue = Color.White,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderColor"
    )
    val borderWidth by infiniteTransition.animateValue(
        initialValue = 2.dp,
        targetValue = 6.dp,
        typeConverter = Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderWidth"
    )
    MenuButtonSurface(
        data = data,
        locked = locked,
        borderColor = borderColor,
        borderWidth = borderWidth,
        onClick = onClick
    )
}

@Composable
private fun StaticMenuButton(
    data: MenuData,
    locked: Boolean,
    onClick: () -> Unit
) {
    MenuButtonSurface(
        data = data,
        locked = locked,
        borderColor = Color.White.copy(alpha = 0.35f),
        borderWidth = 2.dp,
        onClick = onClick
    )
}

@Composable
private fun MenuButtonSurface(
    data: MenuData,
    locked: Boolean,
    borderColor: Color,
    borderWidth: Dp,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .border(borderWidth, borderColor, RoundedCornerShape(32.dp))
            .squishClickable(onClick = onClick),
        color = if (locked) data.color.copy(alpha = 0.55f) else data.color,
        shape = RoundedCornerShape(32.dp),
        shadowElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box {
                    Icon(
                        imageVector = data.icon,
                        contentDescription = "Play ${data.title} Game",
                        tint = Color.White,
                        modifier = Modifier
                            .size(56.dp)
                            .padding(bottom = 8.dp)
                    )
                }
                Text(
                    text = data.title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
