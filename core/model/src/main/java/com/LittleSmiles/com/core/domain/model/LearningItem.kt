package com.LittleSmiles.com.core.domain.model

/**
 * Interface that all modular learning data models must implement.
 * Pure Kotlin version for Domain layer.
 */
interface LearningItem {
    val name: String
    val backgroundColorHex: Long
}
