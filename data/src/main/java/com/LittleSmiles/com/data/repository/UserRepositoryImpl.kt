package com.LittleSmiles.com.data.repository

import com.LittleSmiles.com.core.domain.model.ProfileResult
import com.LittleSmiles.com.core.domain.model.User
import com.LittleSmiles.com.core.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val functions: FirebaseFunctions
) : UserRepository {

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                trySend(null)
            } else {
                trySend(User(firebaseUser.uid, firebaseUser.email ?: "", null, null, false, 0))
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun getUserProfile(uid: String): User? {
        return when (val result = loadUserProfile(uid)) {
            is ProfileResult.Success -> result.user
            is ProfileResult.Error -> null
        }
    }

    override suspend fun loadUserProfile(uid: String): ProfileResult {
        var attempts = 0
        while (attempts < 3) {
            try {
                val doc = db.collection("users").document(uid).get().await()
                if (!doc.exists()) {
                    return ProfileResult.Success(null)
                } else {
                    val timestamp = doc.getTimestamp("trialStartDate")
                    return ProfileResult.Success(
                        User(
                            uid = uid,
                            email = doc.getString("email") ?: "",
                            deviceId = doc.getString("deviceId"),
                            trialStartDate = timestamp?.toDate()?.time,
                            isPremium = doc.getBoolean("isPremium") ?: false,
                            minutesUsedToday = doc.getLong("minutesUsedToday") ?: 0
                        )
                    )
                }
            } catch (e: Exception) {
                attempts++
                if (attempts >= 3 || (e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == false)) {
                    Timber.e(e, "Firestore error loading profile for uid: $uid (attempt $attempts)")
                    return ProfileResult.Error(
                        message = e.message ?: "Could not load your profile. Check your connection.",
                        cause = e
                    )
                }
                kotlinx.coroutines.delay(500L * attempts)
            }
        }
        return ProfileResult.Error("Timeout loading profile")
    }

    override suspend fun createUserProfile(user: User) {
        runCatching {
            val userData = hashMapOf(
                "email" to user.email,
                "deviceId" to null,
                "trialStartDate" to null,
                "isPremium" to false,
                "minutesUsedToday" to 0
            )
            db.collection("users").document(user.uid).set(userData).await()
        }.onFailure { Timber.e(it, "Failed to create user profile") }
    }

    override suspend fun updateDeviceId(uid: String, deviceId: String) {
        writeWithLogging("Failed to update device ID") {
            db.collection("users").document(uid).update("deviceId", deviceId).await()
        }
    }

    override suspend fun updateUsage(uid: String, minutes: Long) {
        writeWithLogging("Failed to update usage") {
            db.collection("users").document(uid).update("minutesUsedToday", minutes).await()
        }
    }

    override suspend fun logLoginTimestamp(uid: String) {
        writeWithLogging("Failed to log login timestamp for uid: $uid") {
            val timestampData = hashMapOf(
                "lastLogin" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid).collection("login_history").add(timestampData).await()
            db.collection("users").document(uid).set(
                mapOf("lastLogin" to com.google.firebase.firestore.FieldValue.serverTimestamp()),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
        }
    }

    override suspend fun resetDeviceId(uid: String) {
        writeWithLogging("Failed to reset device ID") {
            db.collection("users").document(uid).update("deviceId", null).await()
        }
    }

    override suspend fun startTrial(uid: String, deviceId: String) {
        writeWithLogging("Failed to start trial for uid: $uid") {
            val updateData = hashMapOf(
                "deviceId" to deviceId,
                "trialStartDate" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid).set(
                updateData,
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
        }
    }

    private suspend fun <T> writeWithLogging(message: String, block: suspend () -> T): T {
        return try {
            block()
        } catch (e: Exception) {
            Timber.e(e, message)
            throw e
        }
    }

    override suspend fun syncPremiumStatus(
        purchaseToken: String,
        productId: String,
        isSubscription: Boolean
    ): Result<Unit> = runCatching {
        val data = hashMapOf(
            "purchaseToken" to purchaseToken,
            "productId" to productId,
            "productType" to if (isSubscription) "subs" else "inapp"
        )

        functions
            .getHttpsCallable("verifyPurchase")
            .call(data)
            .await()
        
        Unit
    }.onFailure { 
        Timber.e(it, "Premium sync failed")
    }
}
