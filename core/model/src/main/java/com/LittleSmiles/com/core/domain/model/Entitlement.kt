package com.LittleSmiles.com.core.domain.model

/**
 * Monetization access tiers for Little Buds Academy.
 *
 * Funnel: Free (traffic) → Trial (activation) → Premium (revenue).
 * After trial expires, free activities stay open (soft freemium) to retain
 * users and convert high-intent taps on locked games.
 */
enum class AccessTier {
    /** Content unlocked for verified users. */
    FREE,
    /** Full catalog during the one-time 14-day trial. */
    TRIAL,
    /** Paid full access. */
    PREMIUM
}

data class Entitlement(
    val tier: AccessTier,
    val trialDaysLeft: Long = 0,
    val isSignedIn: Boolean = false
) {
    val hasFullAccess: Boolean
        get() = tier == AccessTier.TRIAL || tier == AccessTier.PREMIUM

    fun canAccess(activity: LearningActivityType): Boolean =
        hasFullAccess || activity.isFree

    fun canAccessRoute(route: String): Boolean {
        val activity = LearningActivityType.all.firstOrNull { it.route == route }
            ?: return hasFullAccess
        return canAccess(activity)
    }

    companion object {
        val Default: Entitlement = Entitlement(
            tier = AccessTier.FREE,
            trialDaysLeft = 0,
            isSignedIn = false
        )

        fun fromUser(user: User?): Entitlement {
            if (user == null) return Default
            
            // 2026 EARLY ACCESS: All verified users get full access automatically.
            return Entitlement(
                tier = AccessTier.PREMIUM,
                trialDaysLeft = 0,
                isSignedIn = true
            )
        }
    }
}

sealed class ProfileResult {
    data class Success(val user: User?) : ProfileResult()
    data class Error(val message: String, val cause: Throwable? = null) : ProfileResult()
}
