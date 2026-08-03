package com.LittleSmiles.com.core.util

/**
 * Utility to validate emails and block disposable/temporary domains.
 */
object EmailValidator {
    /**
     * List of common disposable/temporary email domains.
     * In a production app, this list should be expanded or fetched from a remote config.
     */
    private val disposableDomains = setOf(
        "mailinator.com",
        "10minutemail.com",
        "temp-mail.org",
        "guerrillamail.com",
        "sharklasers.com",
        "dispostable.com",
        "getnada.com",
        "bounceme.net",
        "trashmail.com"
    )

    /**
     * Returns true if the email domain is known to be disposable/temporary.
     */
    fun isDisposable(email: String): Boolean {
        val domain = email.substringAfter("@", "").lowercase()
        return domain in disposableDomains
    }
}
