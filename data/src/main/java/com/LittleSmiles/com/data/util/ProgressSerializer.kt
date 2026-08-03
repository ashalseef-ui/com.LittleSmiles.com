package com.LittleSmiles.com.data.util

import android.util.Log
import com.LittleSmiles.com.core.domain.model.UserProgress
import com.google.gson.Gson
import com.google.gson.JsonParser

/**
 * Handles serialization and deserialization for UserProgress.
 * Granular tracking data and multi-child support have been removed.
 */
class ProgressSerializer(private val gson: Gson = Gson()) {
    private val TAG = "ProgressSerializer"

    fun deserialize(json: String?, userId: String): UserProgress {
        if (json.isNullOrBlank()) return UserProgress(userId = userId)
        
        return try {
            val jsonElement = JsonParser.parseString(json)
            if (!jsonElement.isJsonObject) {
                 Log.e(TAG, "Not a JSON object for $userId, resetting")
                 return UserProgress(userId = userId)
            }
            gson.fromJson(json, UserProgress::class.java) ?: UserProgress(userId = userId)
        } catch (e: Exception) {
            Log.e(TAG, "JSON Corrupt for $userId: ${e.message}")
            UserProgress(userId = userId)
        }
    }

    fun serialize(progress: UserProgress): String {
        return gson.toJson(progress)
    }
}
