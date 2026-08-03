package com.LittleSmiles.com.ui.features.menu

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.navigation.compose.rememberNavController
import com.LittleSmiles.com.core.domain.model.LearningActivityType
import com.LittleSmiles.com.ui.theme.MyApplicationTheme
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class MenuAccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun allMenuButtonsHaveCorrectAccessibilityLabels() {
        // We use a relaxed mock for the ViewModel to avoid full dependency injection
        val mockViewModel = mockk<MenuViewModel>(relaxed = true)
        
        composeTestRule.setContent {
            MyApplicationTheme {
                MenuScreen(
                    navController = rememberNavController(),
                    onLogout = {},
                    viewModel = mockViewModel
                )
            }
        }

        // The audit fix added "Play [Title] Game" as the description.
        // We verify that the free games (which show by default in the mock) are present.
        val freeActivities = LearningActivityType.all.filter { it.isFree }
        
        freeActivities.forEach { activity ->
            composeTestRule
                .onNodeWithContentDescription("Play ${activity.displayName} Game")
                .assertIsDisplayed()
        }
    }
}
