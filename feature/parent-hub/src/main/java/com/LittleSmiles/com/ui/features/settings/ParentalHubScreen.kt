package com.LittleSmiles.com.ui.features.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.LittleSmiles.com.core.navigation.Screen
import com.LittleSmiles.com.ui.features.settings.sections.ProfileSection
import com.LittleSmiles.com.ui.theme.StickerCraftTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentalHubScreen(
    parentNavController: NavController,
    userId: String,
    onLogout: () -> Unit
) {
    val items = listOf(
        HubNavItem("Settings", Icons.Default.Settings),
        HubNavItem("Profile", Icons.Default.Person)
    )

    val pagerState = rememberPagerState(pageCount = { items.size })
    val coroutineScope = rememberCoroutineScope()

    // Handle back to menu
    BackHandler {
        parentNavController.popBackStack()
    }

    StickerCraftTheme {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        )
                    }
                    
                    // Exit Parental Mode Button
                    NavigationBarItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Exit") },
                        label = { Text("Exit") },
                        selected = false,
                        onClick = { parentNavController.popBackStack() },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = MaterialTheme.colorScheme.error,
                            unselectedTextColor = MaterialTheme.colorScheme.error
                        )
                    )
                }
            }
        ) { padding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.padding(padding),
                beyondViewportPageCount = 1
            ) { page ->
                when (page) {
                    0 -> SettingsScreen(
                        userId = userId,
                        onBack = { parentNavController.popBackStack() }
                    )
                    1 -> ProfileSection(userId = userId, onLogout = onLogout)
                }
            }
        }
    }
}

data class HubNavItem(val title: String, val icon: ImageVector)
