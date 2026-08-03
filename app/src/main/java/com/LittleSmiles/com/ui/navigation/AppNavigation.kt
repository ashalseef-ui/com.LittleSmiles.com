package com.LittleSmiles.com.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.LittleSmiles.com.core.navigation.Screen
import com.LittleSmiles.com.core.ui.R
import com.LittleSmiles.com.ui.features.auth.LoginScreen
import com.LittleSmiles.com.ui.features.auth.UpgradeScreen
import com.LittleSmiles.com.ui.features.drawing.DrawingScreen
import com.LittleSmiles.com.ui.features.learning.screens.AnimalScreen
import com.LittleSmiles.com.ui.features.learning.screens.BodyPartScreen
import com.LittleSmiles.com.ui.features.learning.screens.ColorScreen
import com.LittleSmiles.com.ui.features.learning.screens.EmotionScreen
import com.LittleSmiles.com.ui.features.learning.screens.OppositeScreen
import com.LittleSmiles.com.ui.features.learning.screens.RoutineScreen
import com.LittleSmiles.com.ui.features.learning.screens.ShapeScreen
import com.LittleSmiles.com.ui.features.loading.LoadingScreen
import com.LittleSmiles.com.ui.features.matching.MatchingScreen
import com.LittleSmiles.com.ui.features.menu.MenuScreen
import com.LittleSmiles.com.ui.features.settings.ParentalHubScreen
import com.LittleSmiles.com.ui.features.settings.SettingsScreen
import com.LittleSmiles.com.ui.features.tracing.TracingScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: AppViewModel = hiltViewModel()
) {
    val navState by viewModel.uiState.collectAsState()
    val entitlement by viewModel.entitlement.collectAsState()
    val user by viewModel.userProfile.collectAsState()
    val userId = user?.uid ?: "guest"
    val context = LocalContext.current
    val webClientId = stringResource(R.string.firebase_web_client_id)

    val gso = remember(webClientId) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember(gso) { GoogleSignIn.getClient(context, gso) }

    val onLogoutRequest: () -> Unit = {
        googleSignInClient.signOut().addOnCompleteListener {
            viewModel.logout()
        }
    }

    LaunchedEffect(navState) {
        when (navState) {
            is NavigationState.Authenticated, is NavigationState.FreePlay -> {
                val currentRoute = navController.currentDestination?.route
                if (currentRoute == Screen.Splash.route ||
                    currentRoute == Screen.Login.route ||
                    currentRoute == Screen.Upgrade.route ||
                    currentRoute == Screen.TrialExpired.route
                ) {
                    // Don't auto-leave Upgrade after purchase until onPurchased handles it;
                    // only bounce from splash/login into menu.
                    if (currentRoute == Screen.Splash.route || currentRoute == Screen.Login.route) {
                        navController.navigate(Screen.Menu.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
            is NavigationState.LoginRequired -> {
                if (navController.currentDestination?.route != Screen.Login.route) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is NavigationState.Loading -> {
                if (navController.currentDestination?.route != Screen.Splash.route) {
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is NavigationState.Error -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(300)) },
            popEnterTransition = { fadeIn(tween(300)) },
            popExitTransition = { fadeOut(tween(300)) }
        ) {
            composable(Screen.Splash.route) { LoadingScreen() }

            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { viewModel.checkSession() },
                    onContinueFree = { viewModel.continueAsFree() }
                )
            }

            composable(Screen.Upgrade.route) {
                UpgradeScreen(
                    onPurchased = {
                        viewModel.refreshEntitlement()
                        navController.navigate(Screen.Menu.route) {
                            popUpTo(Screen.Menu.route) { inclusive = true }
                        }
                    },
                    onContinueFree = { navController.popBackStack() },
                    onLoginForTrial = {
                        navController.navigate(Screen.Login.route)
                    }
                )
            }

            composable(Screen.TrialExpired.route) {
                UpgradeScreen(
                    onPurchased = {
                        viewModel.refreshEntitlement()
                        navController.navigate(Screen.Menu.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onContinueFree = {
                        navController.navigate(Screen.Menu.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onLoginForTrial = { navController.navigate(Screen.Login.route) }
                )
            }

            composable(Screen.Menu.route) {
                MenuScreen(navController = navController, onLogout = onLogoutRequest)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    userId = userId,
                    onBack = {
                        navController.previousBackStackEntry?.savedStateHandle?.set(
                            "show_parent_menu",
                            true
                        )
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Colors.route) {
                ColorScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Shapes.route) {
                ShapeScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Animals.route) {
                GuardedPremiumRoute(
                    allowed = entitlement.canAccessRoute(Screen.Animals.route),
                    onBlocked = { navController.navigate(Screen.Upgrade.route) }
                ) {
                    AnimalScreen(onBack = { navController.popBackStack() })
                }
            }
            composable(Screen.Routines.route) {
                GuardedPremiumRoute(
                    allowed = entitlement.canAccessRoute(Screen.Routines.route),
                    onBlocked = { navController.navigate(Screen.Upgrade.route) }
                ) {
                    RoutineScreen(onBack = { navController.popBackStack() })
                }
            }
            composable(Screen.Opposites.route) {
                GuardedPremiumRoute(
                    allowed = entitlement.canAccessRoute(Screen.Opposites.route),
                    onBlocked = { navController.navigate(Screen.Upgrade.route) }
                ) {
                    OppositeScreen(onBack = { navController.popBackStack() })
                }
            }
            composable(Screen.Letters.route) {
                TracingScreen(mode = "ABC", onBack = { navController.popBackStack() })
            }
            composable(Screen.Numbers.route) {
                TracingScreen(mode = "123", onBack = { navController.popBackStack() })
            }
            composable(Screen.Tracing.route) {
                TracingScreen(mode = "ABC", onBack = { navController.popBackStack() })
            }
            composable(Screen.BodyParts.route) {
                GuardedPremiumRoute(
                    allowed = entitlement.canAccessRoute(Screen.BodyParts.route),
                    onBlocked = { navController.navigate(Screen.Upgrade.route) }
                ) {
                    BodyPartScreen(onBack = { navController.popBackStack() })
                }
            }
            composable(Screen.Emotions.route) {
                GuardedPremiumRoute(
                    allowed = entitlement.canAccessRoute(Screen.Emotions.route),
                    onBlocked = { navController.navigate(Screen.Upgrade.route) }
                ) {
                    EmotionScreen(onBack = { navController.popBackStack() })
                }
            }
            composable(Screen.Matching.route) {
                GuardedPremiumRoute(
                    allowed = entitlement.canAccessRoute(Screen.Matching.route),
                    onBlocked = { navController.navigate(Screen.Upgrade.route) }
                ) {
                    MatchingScreen(onBack = { navController.popBackStack() })
                }
            }
            composable(Screen.Drawing.route) {
                GuardedPremiumRoute(
                    allowed = entitlement.canAccessRoute(Screen.Drawing.route),
                    onBlocked = { navController.navigate(Screen.Upgrade.route) }
                ) {
                    DrawingScreen(onBack = { navController.popBackStack() })
                }
            }

            composable(Screen.ParentalHub.route) {
                ParentalHubScreen(
                    parentNavController = navController,
                    userId = userId,
                    onLogout = onLogoutRequest
                )
            }
        }

        if (navState is NavigationState.Error) {
            AlertDialog(
                onDismissRequest = { viewModel.checkSession() },
                title = { Text("App Error") },
                text = { Text((navState as NavigationState.Error).message) },
                confirmButton = {
                    TextButton(onClick = { viewModel.checkSession() }) {
                        Text("Retry")
                    }
                }
            )
        }
    }
}

@Composable
private fun GuardedPremiumRoute(
    allowed: Boolean,
    onBlocked: () -> Unit,
    content: @Composable () -> Unit
) {
    LaunchedEffect(allowed) {
        if (!allowed) onBlocked()
    }
    if (allowed) {
        content()
    }
}
