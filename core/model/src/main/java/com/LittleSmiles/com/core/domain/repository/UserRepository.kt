package com.LittleSmiles.com.core.domain.repository

import com.LittleSmiles.com.core.domain.model.ProfileResult
import com.LittleSmiles.com.core.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun getUserProfile(uid: String): User?
    suspend fun loadUserProfile(uid: String): ProfileResult
    suspend fun createUserProfile(user: User)
    suspend fun updateDeviceId(uid: String, deviceId: String)
    suspend fun updateUsage(uid: String, minutes: Long)
    suspend fun logLoginTimestamp(uid: String)
    suspend fun resetDeviceId(uid: String)
    suspend fun startTrial(uid: String, deviceId: String)
    /** Trigger server-side verification of a Play Store purchase. */
    suspend fun syncPremiumStatus(
        purchaseToken: String,
        productId: String,
        isSubscription: Boolean
    ): Result<Unit>
    suspend fun reportError(message: String, stackTrace: String)
}
