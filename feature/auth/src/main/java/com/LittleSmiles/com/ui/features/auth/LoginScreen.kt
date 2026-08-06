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
                12501 -> "Sign-in Cancelled (12501). Check SHA-1/Web Client ID if unexpected."
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
        CrystalAmbientBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .navigationBarsPadding(),
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
                    modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.PowerSettingsNew, "Exit", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(25.dp))
            
            // Integrated Branding
            val brandName = "Little Buds Academy"
            brandName.split(" ").chunked(2).forEach { lineWords ->
                Row(horizontalArrangement = Arrangement.Center) {
                    lineWords.forEach { word ->
                        Text(
                            text = word,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 2.sp
                        )
                        if (word != lineWords.last()) Spacer(Modifier.width(12.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "✨ EARLY ACCESS 2026: ALL FREE. ✨",
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(10.dp))

            // --- Floating Auth Content ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .padding(horizontal = 9.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Parental Sign In",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                CrystalTextField(
                    value = email,
                    onValueChange = { email = it; viewModel.clearState() },
                    label = "Parent's Email",
                    icon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    error = if (email.isNotEmpty() && !isEmailValid) "Oops! Check your email." else null,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(10.dp))

                CrystalTextField(
                    value = password,
                    onValueChange = { password = it; viewModel.clearState() },
                    label = "Secure Password",
                    icon = Icons.Default.Lock,
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onTogglePassword = { passwordVisible = !passwordVisible },
                    error = if (password.isNotEmpty() && !isPasswordSecure) "At least 6 characters" else null,
                    enabled = !isLoading
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { rememberMe = !rememberMe }) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color.White,
                                uncheckedColor = Color.White.copy(alpha = 0.4f),
                                checkmarkColor = SkyBluePrimary
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Remember Me", fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    TextButton(
                        onClick = { 
                            if (email.trim().isBlank()) {
                                viewModel.setExternalError("Enter email first!")
                            } else {
                                viewModel.resetPassword(email.trim())
                            }
                        }, 
                        enabled = !isLoading
                    ) {
                        Text("Forgot Pwd?", color = Color.White.copy(alpha = 0.8f), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                LiquidButton(
                    text = "Let's Play!",
                    onClick = { viewModel.signIn(email.trim(), password.trim(), deviceId) },
                    enabled = isEmailValid && isPasswordSecure && !isLoading,
                    isLoading = isLoading
                )

                Spacer(modifier = Modifier.height(7.dp))

                TextButton(
                    onClick = { viewModel.signUp(email.trim(), password.trim(), deviceId) }, 
                    enabled = isEmailValid && isPasswordSecure && !isLoading
                ) {
                    Text("New Here? Create Account", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(7.dp))
                Text("✨ OR ✨", color = Color.White.copy(alpha = 0.3f), fontWeight = FontWeight.Black, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(7.dp))

                CrystalGoogleButton(
                    onClick = { googleSignInLauncher.launch(googleSignInClient.signInIntent) },
                    enabled = !isLoading
                )

                // Feedback messages
                AnimatedVisibility(visible = (uiState is AuthUiState.Error || uiState is AuthUiState.Info || uiState is AuthUiState.Unverified)) {
                    val (message, color) = when(val s = uiState) {
                        is AuthUiState.Error -> s.message to Color(0xFFFDA4AF)
                        is AuthUiState.Info -> s.message to Color.White
                        is AuthUiState.Unverified -> "Please check ${s.email} to verify!" to Color.White
                        else -> "" to Color.White
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = message,
                            color = color,
                            fontSize = 14.sp,
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
                                        color = if (resendTimer > 0) Color.White.copy(alpha = 0.4f) else Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                TextButton(onClick = { viewModel.refreshVerificationStatus(email, deviceId) }) {
                                    Text("I've Verified!", color = Color.White, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(64.dp))
            
            Text(
                "Verified Parent Portal",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}



@Composable
fun CrystalTextField(
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
            label = { Text(label, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold) },
            leadingIcon = {
                Icon(icon, null, tint = Color.White, modifier = Modifier.padding(start = 12.dp).size(22.dp))
            },
            trailingIcon = if (isPassword && onTogglePassword != null) {
                {
                    IconButton(onClick = onTogglePassword) {
                        Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = Color.White.copy(alpha = 0.6f))
                    }
                }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                focusedContainerColor = Color.White.copy(alpha = 0.25f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.15f),
                cursorColor = Color.White
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            isError = error != null,
            singleLine = true,
            enabled = enabled
        )
        if (error != null) {
            Text(error, color = Color(0xFFFDA4AF), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp, top = 3.dp))
        }
    }
}

@Composable
fun LiquidButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "scale")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .graphicsLayer { 
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                onClick = onClick
            ),
        color = Color.Transparent,
        shape = RoundedCornerShape(30.dp),
    ) {
        Box(
            modifier = Modifier.background(
                Brush.linearGradient(
                    listOf(Color(0xFF22C55E), Color(0xFF10B981), Color(0xFF06B6D4))
                )
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun CrystalGoogleButton(
    onClick: () -> Unit,
    enabled: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable(enabled = enabled, onClick = onClick),
        color = Color.White.copy(alpha = 0.15f),
        shape = RoundedCornerShape(27.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CloudQueue, null, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(14.dp))
            Text("Login with Google", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
