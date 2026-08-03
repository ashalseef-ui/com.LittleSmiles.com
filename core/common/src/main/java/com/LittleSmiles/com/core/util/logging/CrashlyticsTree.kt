package com.LittleSmiles.com.core.util.logging

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Timber Tree that reports high-priority logs and exceptions to Firebase Crashlytics.
 */
class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
            return
        }

        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.log("${priorityToString(priority)}/${tag ?: "NoTag"}: $message")

        if (t != null) {
            crashlytics.recordException(t)
        }
    }

    private fun priorityToString(priority: Int): String = when (priority) {
        Log.ERROR -> "E"
        Log.WARN -> "W"
        Log.ASSERT -> "A"
        else -> priority.toString()
    }
}
