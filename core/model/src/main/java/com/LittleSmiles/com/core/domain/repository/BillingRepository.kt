package com.LittleSmiles.com.core.domain.repository

import kotlinx.coroutines.flow.Flow

enum class PremiumProductId(val raw: String, val title: String, val subtitle: String) {
    MONTHLY(
        raw = "premium_monthly",
        title = "Monthly",
        subtitle = "Flexible — cancel anytime"
    ),
    YEARLY(
        raw = "premium_yearly",
        title = "Yearly",
        subtitle = "Best value for families"
    ),
    LIFETIME(
        raw = "premium_lifetime",
        title = "Lifetime",
        subtitle = "One purchase, forever"
    )
}

data class PremiumProduct(
    val id: PremiumProductId,
    val priceLabel: String,
    val isSuggested: Boolean = false
)

sealed class PurchaseResult {
    data object Success : PurchaseResult()
    data object Cancelled : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
    data object NotConfigured : PurchaseResult()
}

interface BillingRepository {
    val products: Flow<List<PremiumProduct>>
    val isPremium: Flow<Boolean>

    suspend fun startConnection()
    suspend fun refreshPurchases()
    suspend fun launchPurchase(activity: Any, productId: PremiumProductId): PurchaseResult
    suspend fun restorePurchases(): PurchaseResult
}
