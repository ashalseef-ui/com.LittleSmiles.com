package com.LittleSmiles.com.data.repository

import com.LittleSmiles.com.core.domain.repository.AuthRepository
import com.LittleSmiles.com.core.util.EmailValidator
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    override val currentUserEmail: String?
        get() = firebaseAuth.currentUser?.email

    override val isEmailVerified: Boolean
        get() = firebaseAuth.currentUser?.isEmailVerified ?: false

    override suspend fun signIn(email: String, pass: String): String {
        val result = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
        return result.user?.uid ?: throw Exception("Sign in failed: No user returned")
    }

    override suspend fun signUp(email: String, pass: String): String {
        if (EmailValidator.isDisposable(email)) {
            throw Exception("Temporary/disposable emails are not allowed. Please use a standard email.")
        }
        val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
        return result.user?.uid ?: throw Exception("Sign up failed: No user returned")
    }

    override suspend fun sendEmailVerification() {
        var user = firebaseAuth.currentUser
        var attempts = 0
        while (attempts < 10) { // Up to 5 seconds total
            if (user != null) {
                try {
                    user.sendEmailVerification().await()
                    return // Success!
                } catch (e: Exception) {
                    // If it fails, maybe the session needs a reload
                    try { user.reload().await() } catch (re: Exception) { /* ignore */ }
                }
            }
            
            kotlinx.coroutines.delay(500L)
            user = firebaseAuth.currentUser
            attempts++
        }
        
        throw Exception("No user signed in to verify (after $attempts attempts)")
    }

    override suspend fun reloadUser() {
        firebaseAuth.currentUser?.reload()?.await()
    }

    override suspend fun resetPassword(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    override suspend fun signInWithCredential(credential: Any): String {
        val firebaseCredential = credential as? AuthCredential
            ?: throw IllegalArgumentException("Credential must be a Firebase AuthCredential")
        val result = firebaseAuth.signInWithCredential(firebaseCredential).await()
        return result.user?.uid ?: throw Exception("Credential sign in failed: No user returned")
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }
}
