package com.LittleSmiles.com.core.domain.repository

/**
 * Interface for authentication operations.
 * Decouples the UI layer from Firebase.
 */
interface AuthRepository {
    /**
     * The UID of the currently signed-in user, or null if not signed in.
     */
    val currentUserId: String?

    /**
     * The email of the currently signed-in user, or null if not signed in.
     */
    val currentUserEmail: String?

    /**
     * Returns true if the current user has verified their email.
     */
    val isEmailVerified: Boolean

    /**
     * Signs in with email and password. Returns the user UID.
     */
    suspend fun signIn(email: String, pass: String): String

    /**
     * Creates a new user with email and password. Returns the user UID.
     */
    suspend fun signUp(email: String, pass: String): String

    /**
     * Sends a verification email to the current user.
     */
    suspend fun sendEmailVerification()

    /**
     * Reloads the user's data from Firebase (e.g. to refresh email verification status).
     */
    suspend fun reloadUser()

    /**
     * Sends a password reset email.
     */
    suspend fun resetPassword(email: String)

    /**
     * Signs in with a credential (e.g. Google).
     * The [credential] should be a Firebase AuthCredential.
     */
    suspend fun signInWithCredential(credential: Any): String

    /**
     * Signs out the current user.
     */
    fun signOut()
}
