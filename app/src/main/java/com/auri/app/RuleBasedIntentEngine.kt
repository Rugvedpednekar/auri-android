package com.auri.app

interface IntentEngine {
    fun parse(command: String): ParsedIntent
}

class RuleBasedIntentEngine : IntentEngine {
    private val supportedApps = listOf(
        "WhatsApp",
        "Gmail",
        "Calendar",
        "Maps",
        "YouTube",
        "Chrome",
        "Settings",
        "Camera"
    )

    override fun parse(command: String): ParsedIntent {
        val normalizedCommand = command.trim()
        val targetApp = supportedApps.firstOrNull { appName ->
            normalizedCommand.equals("Open $appName", ignoreCase = true)
        }

        return if (targetApp != null) {
            ParsedIntent(
                intentType = IntentType.OPEN_APP,
                targetApp = targetApp,
                confidence = 0.96f,
                requiresConfirmation = true,
                plannedSteps = listOf(
                    "Identify the target app from the typed command.",
                    "Prepare the Android intent for $targetApp.",
                    "Wait for confirmation before opening $targetApp."
                )
            )
        } else {
            ParsedIntent(
                intentType = IntentType.UNKNOWN,
                targetApp = null,
                confidence = 0.0f,
                requiresConfirmation = false,
                plannedSteps = listOf(
                    "No supported command matched.",
                    "Try a command like Open Gmail or Open Maps."
                )
            )
        }
    }
}
