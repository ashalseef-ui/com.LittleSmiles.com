package com.LittleSmiles.com.ui.features.auth

import android.app.Activity
import android.provider.Settings
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.LittleSmiles.com.core.ui.R
import com.LittleSmiles.com.ui.components.*
import com.LittleSmiles.com.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onContinueFree: () -> Unit = onLoginSuccess,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE) }

    var email by remember { mutableStateOf(prefs.getString("saved_email", "") ?: "") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(prefs.getBoolean("remember_me", true)) }

    val uiState by viewModel.uiState.collectAsState()
    val resendTimer by viewModel.resendTimer.collectAsState()
    val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    LaunchedEffect(rememberMe, email) {
        prefs.edit().apply {
            putBoolean("remember_me", rememberMe)
            if (rememberMe) putString("saved_email", email) else remove("saved_email")
            apply()
        }
    }

    val webClientId = stringResource(R.string.firebase_web_client_id)
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail().build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleSignInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { viewModel.signInWithCredential(GoogleAuthProvider.getCredential(it, null), deviceId) }
        } catch (e: ApiException) {
            val errorMsg = when(e.statusCode) {
                10 -> "Developer Error (10): Check SHA-1 in Firebase Console."
                7 -> "Network Error: Please check your connection."
                else -> "Google Sign-in failed (${e.statusCode})"
            }
            viewModel.setExternalError(errorMsg)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onLoginSuccess()
        }
    }

    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isPasswordSecure = password.length >= 6
    val isLoading = uiState is AuthUiState.Loading

    Box(modifier = Modifier.fillMaxSize()) {
        JoyfulSkyBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row - Compact
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = {
                        (context as? Activity)?.finishAffinity()
                        exitProcess(0)
                    },
                    modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.PowerSettingsNew, "Exit", tint = ErrorRed, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                JoyfulBranding()
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = RainbowOrange.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, RainbowOrange.copy(alpha = 0.5f))
                ) {
                    Text(
                        "✨ EARLY ACCESS 2026: ALL GAMES FREE ✨",
                        color = RainbowOrange,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            CloudLoginCard(isLoading) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Value Proposition moved to header, Card is pure auth now
                    Text(
                        "Parent Login",
                        color = SkyBlueDark,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    JoyfulTextField(
                        value = email,
                        onValueChange = { email = it; viewModel.clearState() },
                        label = "Parent's Email",
                        icon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email,
                        error = if (email.isNotEmpty() && !isEmailValid) "Oops! Check your email." else null,
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    JoyfulTextField(
                        value = password,
                        onValueChange = { password = it; viewModel.clearState() },
                        label = "Password",
                        icon = Icons.Default.Lock,
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onTogglePassword = { passwordVisible = !passwordVisible },
                        error = if (password.isNotEmpty() && !isPasswordSecure) "Need at least 6 characters!" else null,
                        enabled = !isLoading
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            onClick = { rememberMe = !rememberMe },
                            color = Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = null, // Handled by Surface click
                                    colors = CheckboxDefaults.colors(checkedColor = RainbowBlue),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Remember Email", fontSize = 13.sp, color = SlateText, fontWeight = FontWeight.Bold)
                            }
                        }
                        TextButton(
                            onClick = { 
                                if (email.trim().isBlank()) {
                                    viewModel.setExternalError("Enter email first to reset!")
                                } else {
                                    viewModel.resetPassword(email.trim())
                                }
                            }, 
                            enabled = !isLoading
                        ) {
                            Text("Forgot Pwd?", color = SkyBlueDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    JoyfulButton(
                        text = "Let's Go!",
                        onClick = { viewModel.signIn(email.trim(), password.trim(), deviceId) },
                        backgroundColor = RainbowGreen,
                        enabled = isEmailValid && isPasswordSecure && !isLoading,
                        isLoading = isLoading
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { viewModel.signUp(email.trim(), password.trim(), deviceId) }, 
                        enabled = isEmailValid && isPasswordSecure && !isLoading,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("New? Create Account!", color = SkyBlueDark, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("✨ OR ✨", color = SkyBlueDark.copy(alpha = 0.3f), fontWeight = FontWeight.Black, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    JoyfulGoogleButton(
                        onClick = { googleSignInLauncher.launch(googleSignInClient.signInIntent) },
                        enabled = !isLoading,
                        contentDescription = "Sign in with Google"
                    )

                    // Feedback messages
                    AnimatedVisibility(visible = uiState is AuthUiState.Error || uiState is AuthUiState.Info || uiState is AuthUiState.Unverified) {
                        val message = when(val s = uiState) {
                            is AuthUiState.Error -> s.message
                            is AuthUiState.Info -> s.message
                            is AuthUiState.Unverified -> "Please check ${s.email} to verify your account!"
                            else -> ""
                        }
                        val color = if (uiState is AuthUiState.Error) ErrorRed else SuccessGreen
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = message,
                                color = color,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 9.dp)
                            )
                            
                            if (uiState is AuthUiState.Unverified) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    TextButton(
                                        onClick = { viewModel.resendVerification() },
                                        enabled = resendTimer == 0
                                    ) {
                                        Text(
                                            text = if (resendTimer > 0) "Resend in ${resendTimer}s" else "Resend Email",
                                            color = if (resendTimer > 0) Color.Gray else RainbowBlue,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    TextButton(onClick = { viewModel.refreshVerificationStatus(email, deviceId) }) {
                                        Text("I've Verified!", color = RainbowGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun JoyfulSkyBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "sky")
    val skyColor1 by infiniteTransition.animateColor(
        initialValue = Color(0xFFF0F9FF),
        targetValue = Color(0xFFE0F2FE),
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Reverse),
        label = "c1"
    )
    
    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(skyColor1, SkyBlueLight)))) {
        // Floating Clouds - Decelerated
        repeat(5) { i ->
            val duration = 22000 + (i * 3000)
            val delay = i * 1500
            FloatingCloud(duration, delay, offsetMultiplier = i)
        }
        
        // Twinkling stars / sparkles - Decelerated
        repeat(12) { i ->
            Sparkle(delay = i * 400)
        }
    }
}

@Composable
fun FloatingCloud(duration: Int, delay: Int, offsetMultiplier: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "cloud")
    val xProgress by infiniteTransition.animateFloat(
        initialValue = -0.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            tween(duration, delayMillis = delay, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "x"
    )
    
    val yOffset = (50 + (offsetMultiplier * 120)).dp
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = yOffset)
            .graphicsLayer { translationX = xProgress * size.width }
    ) {
        Icon(
            imageVector = Icons.Default.Cloud,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.4f),
            modifier = Modifier.size(100.dp + (offsetMultiplier * 20).dp)
        )
    }
}

@Composable
fun Sparkle(delay: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1500, delayMillis = delay, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val x = remember { (0..100).random() / 100f }
    val y = remember { (0..100).random() / 100f }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .graphicsLayer {
                translationX = x * size.width
                translationY = y * size.height
                scaleX = scale
                scaleY = scale
                alpha = scale
            }
    ) {
        Icon(Icons.Default.AutoAwesome, null, tint = RainbowYellow, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun JoyfulBranding() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val infiniteTransition = rememberInfiniteTransition(label = "brand")
        val bounce by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -15f,
            animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "bounce"
        )

        Box(modifier = Modifier.graphicsLayer { translationY = bounce }) {
            RainbowTitle("Little Buds Academy")
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Surface(
            color = RainbowOrange,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "LEARNING IS FUN!",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun RainbowTitle(text: String) {
    val colors = listOf(RainbowRed, RainbowOrange, RainbowYellow, RainbowGreen, RainbowBlue, RainbowViolet)
    val words = text.split(" ")
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        words.chunked(2).forEach { lineWords ->
            Row(horizontalArrangement = Arrangement.Center) {
                lineWords.forEach { word ->
                    Row {
                        word.forEachIndexed { index, char ->
                            val infiniteTransition = rememberInfiniteTransition(label = "char_${word}_$index")
                            val scale by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 1.1f,
                                animationSpec = infiniteRepeatable(
                                    tween(600, delayMillis = index * 100, easing = FastOutSlowInEasing),
                                    RepeatMode.Reverse
                                ),
                                label = "scale"
                            )
                            
                            Text(
                                text = char.toString(),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = colors[(index + word.length) % colors.size],
                                letterSpacing = 2.sp,
                                modifier = Modifier.graphicsLayer { 
                                    scaleX = scale
                                    scaleY = scale
                                }
                            )
                        }
                    }
                    if (word != lineWords.last()) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CloudLoginCard(isLoading: Boolean, content: @Composable () -> Unit) {
    val borderColors = listOf(RainbowBlue, SkyBlueLight, RainbowBlue)
    val infiniteTransition = rememberInfiniteTransition(label = "card")
    val borderOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing)),
        label = "offset"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp)
            .border(
                2.dp,
                Brush.linearGradient(
                    listOf(SkyBlueLight, SkyBluePrimary)
                ),
                RoundedCornerShape(40.dp)
            ),
        color = Color.White.copy(alpha = 0.9f),
        shape = RoundedCornerShape(40.dp),
        shadowElevation = 12.dp
    ) {
        content()
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RainbowBlue, strokeWidth = 6.dp)
            }
        }
    }
}

@Composable
fun JoyfulTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    error: String? = null,
    enabled: Boolean = true
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontWeight = FontWeight.Bold) },
            leadingIcon = {
                Surface(
                    shape = CircleShape,
                    color = SkyBlueLight.copy(alpha = 0.5f),
                    modifier = Modifier.padding(6.dp).size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = SkyBlueDark, modifier = Modifier.size(18.dp))
                    }
                }
            },
            trailingIcon = if (isPassword && onTogglePassword != null) {
                {
                    IconButton(onClick = onTogglePassword) {
                        Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = SlateText)
                    }
                }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RainbowBlue,
                unfocusedBorderColor = SkyBlueLight,
                focusedContainerColor = Color(0xFFFEF9C3).copy(alpha = 0.4f),
                unfocusedContainerColor = Color(0xFFF0F9FF).copy(alpha = 0.6f),
                focusedLabelColor = RainbowBlue,
                unfocusedLabelColor = SlateText,
                cursorColor = RainbowBlue
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            isError = error != null,
            singleLine = true,
            enabled = enabled
        )
        if (error != null) {
            Text(error, color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
        }
    }
}

@Composable
fun JoyfulButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val offsetY = if (isPressed) 4.dp else 0.dp

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .offset(y = offsetY)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                onClick = onClick
            ),
        color = if (enabled) backgroundColor else Color.LightGray,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = if (enabled && !isPressed) 6.dp else 1.dp,
        border = BorderStroke(3.dp, if (enabled) backgroundColor.copy(alpha = 0.8f) else Color.Gray)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun JoyfulGoogleButton(
    onClick: () -> Unit,
    enabled: Boolean,
    contentDescription: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val offsetY = if (isPressed) 3.dp else 0.dp

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .offset(y = offsetY)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        color = Color.White,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(2.dp, SkyBlueLight),
        shadowElevation = if (isPressed) 1.dp else 4.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = SkyBlueLight.copy(alpha = 0.2f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CloudQueue, contentDescription, tint = RainbowBlue, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Text("Fly with Google", color = Color(0xFF1F2937), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
