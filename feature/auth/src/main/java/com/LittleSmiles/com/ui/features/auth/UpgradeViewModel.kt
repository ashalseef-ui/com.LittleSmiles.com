package com.LittleSmiles.com.ui.features.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LittleSmiles.com.core.domain.repository.AuthRepository
import com.LittleSmiles.com.core.domain.repository.BillingRepository
import com.LittleSmiles.com.core.domain.repository.PremiumProduct
import com.LittleSmiles.com.core.domain.repository.PremiumProductId
import com.LittleSmiles.com.core.domain.repository.PurchaseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpgradeViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val products: StateFlow<List<PremiumProduct>> = billingRepository.products
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _purchaseState = MutableStateFlow<PurchaseResult?>(null)
    val purchaseState: StateFlow<PurchaseResult?> = _purchaseState.asStateFlow()

    private val _isGuest = MutableStateFlow(authRepository.currentUserId == null)
    val isGuest: StateFlow<Boolean> = _isGuest.asStateFlow()

    fun bootstrap() {
        _isGuest.value = authRepository.currentUserId == null
        viewModelScope.launch {
            runCatching { billingRepository.startConnection() }
        }
    }

    fun purchase(activity: Activity, productId: PremiumProductId) {
        viewModelScope.launch {
            _purchaseState.value = billingRepository.launchPurchase(activity, productId)
        }
    }

    fun restore() {
        viewModelScope.launch {
            _purchaseState.value = billingRepository.restorePurchases()
        }
    }

    fun clearPurchaseState() {
        _purchaseState.value = null
    }
}
