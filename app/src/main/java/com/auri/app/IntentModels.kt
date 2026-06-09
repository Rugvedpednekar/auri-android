package com.auri.app

enum class IntentType {
    OPEN_APP,
    UNKNOWN
}

data class ParsedIntent(
    val intentType: IntentType,
    val targetApp: String?,
    val confidence: Float,
    val requiresConfirmation: Boolean,
    val plannedSteps: List<String>
)
