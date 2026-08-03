package com.LittleSmiles.com.ui.navigation

import com.LittleSmiles.com.core.domain.model.User
import com.LittleSmiles.com.core.domain.repository.AuthRepository
import com.LittleSmiles.com.core.domain.repository.BillingRepository
import com.LittleSmiles.com.core.domain.repository.PremiumProduct
import com.LittleSmiles.com.core.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

@ExperimentalCoroutinesApi
class AppViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var userRepository: UserRepository
    private lateinit var billingRepository: BillingRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AppViewModel

    @Before
    fun setup() {
        userRepository = mockk()
        billingRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)

        every { authRepository.currentUserId } returns null
        every { billingRepository.products } returns MutableStateFlow(emptyList<PremiumProduct>())
        every { billingRepository.isPremium } returns MutableStateFlow(false)
        coEvery { billingRepository.startConnection() } returns Unit
        coEvery { billingRepository.refreshPurchases() } returns Unit
    }

    @Test
    fun `when no user is logged in, state should be FreePlay`() = runTest {
        every { authRepository.currentUserId } returns null

        viewModel = AppViewModel(userRepository, billingRepository, authRepository)

        assertEquals(NavigationState.FreePlay, viewModel.uiState.value)
        assertTrue(!viewModel.entitlement.value.hasFullAccess)
    }

    @Test
    fun `when user is logged in but profile is missing, state should be LoginRequired`() = runTest {
        every { authRepository.currentUserId } returns "test_uid"
        coEvery { userRepository.loadUserProfile("test_uid") } returns
            com.LittleSmiles.com.core.domain.model.ProfileResult.Success(null)

        viewModel = AppViewModel(userRepository, billingRepository, authRepository)

        assertEquals(NavigationState.LoginRequired, viewModel.uiState.value)
    }

    @Test
    fun `when user is logged in and within 7-day trial, state should be Authenticated with full access`() =
        runTest {
            every { authRepository.currentUserId } returns "test_uid"

            val recentUser = User(
                uid = "test_uid",
                email = "test@test.com",
                deviceId = "device123",
                trialStartDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2),
                isPremium = false,
                minutesUsedToday = 0
            )
            coEvery { userRepository.loadUserProfile("test_uid") } returns
                com.LittleSmiles.com.core.domain.model.ProfileResult.Success(recentUser)

            viewModel = AppViewModel(userRepository, billingRepository, authRepository)

            assertEquals(NavigationState.Authenticated, viewModel.uiState.value)
            assertTrue(viewModel.entitlement.value.hasFullAccess)
        }

    @Test
    fun `when trial expired, state stays Authenticated with free soft access`() = runTest {
        every { authRepository.currentUserId } returns "test_uid"

        val expiredUser = User(
            uid = "test_uid",
            email = "test@test.com",
            deviceId = "device123",
            trialStartDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10),
            isPremium = false,
            minutesUsedToday = 0
        )
        coEvery { userRepository.loadUserProfile("test_uid") } returns
            com.LittleSmiles.com.core.domain.model.ProfileResult.Success(expiredUser)

        viewModel = AppViewModel(userRepository, billingRepository, authRepository)

        assertEquals(NavigationState.Authenticated, viewModel.uiState.value)
        assertTrue(!viewModel.entitlement.value.hasFullAccess)
        assertTrue(
            viewModel.entitlement.value.canAccess(
                com.LittleSmiles.com.core.domain.model.LearningActivityType.Colors
            )
        )
        assertTrue(
            !viewModel.entitlement.value.canAccess(
                com.LittleSmiles.com.core.domain.model.LearningActivityType.Animals
            )
        )
    }

    @Test
    fun `logout should sign out from firebase and set state to LoginRequired`() = runTest {
        every { authRepository.currentUserId } returns "test_uid"
        coEvery { userRepository.loadUserProfile("test_uid") } returns
            com.LittleSmiles.com.core.domain.model.ProfileResult.Success(mockk(relaxed = true))

        viewModel = AppViewModel(userRepository, billingRepository, authRepository)
        viewModel.logout()

        verify { authRepository.signOut() }
        assertEquals(NavigationState.LoginRequired, viewModel.uiState.value)
    }
}
