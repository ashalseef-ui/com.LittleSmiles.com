package com.LittleSmiles.com.data.repository

import com.LittleSmiles.com.core.domain.repository.BillingRepository
import com.LittleSmiles.com.core.domain.repository.PremiumProduct
import com.LittleSmiles.com.core.domain.repository.PremiumProductId
import com.LittleSmiles.com.core.domain.repository.PurchaseResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Play Billing integration (Stubbed for "Coming Soon" teaser).
 * This version does not connect to the Play Store to comply with "No IAP" declaration.
 */
@Singleton
class BillingRepositoryImpl @Inject constructor() : BillingRepository {

    private val _products = MutableStateFlow(fallbackProducts())
    override val products: StateFlow<List<PremiumProduct>> = _products.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    override val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val connectionMutex = Mutex()

    override suspend fun startConnection() = connectionMutex.withLock {
        // Disabled for "Coming Soon" teaser version
    }

    override suspend fun refreshPurchases() {
        // Disabled for "Coming Soon" teaser version
        _isPremium.value = false
    }

    override suspend fun launchPurchase(activity: Any, productId: PremiumProductId): PurchaseResult {
        // Disabled for "Coming Soon" teaser version
        return PurchaseResult.NotConfigured
    }

    override suspend fun restorePurchases(): PurchaseResult {
        // Disabled for "Coming Soon" teaser version
        return PurchaseResult.Error("Restore not available in this version.")
    }

    private fun fallbackProducts(): List<PremiumProduct> = listOf(
        PremiumProduct(PremiumProductId.MONTHLY, "Coming Soon", isSuggested = false),
        PremiumProduct(PremiumProductId.YEARLY, "Coming Soon", isSuggested = true),
        PremiumProduct(PremiumProductId.LIFETIME, "Coming Soon", isSuggested = false)
    )
}
