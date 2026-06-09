package com.auri.app

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.provider.Settings

data class ActionResult(
    val success: Boolean,
    val message: String
)

class AndroidActionExecutor(private val context: Context) {
    private val packageNames = mapOf(
        "WhatsApp" to "com.whatsapp",
        "Gmail" to "com.google.android.gm",
        "Calendar" to "com.google.android.calendar",
        "Maps" to "com.google.android.apps.maps",
        "YouTube" to "com.google.android.youtube",
        "Chrome" to "com.android.chrome"
    )

    fun execute(parsedIntent: ParsedIntent): ActionResult {
        if (parsedIntent.intentType != IntentType.OPEN_APP || parsedIntent.targetApp == null) {
            return ActionResult(false, "Auri could not find an action to run.")
        }

        return when (parsedIntent.targetApp) {
            "Settings" -> startExplicitAction(
                intent = Intent(Settings.ACTION_SETTINGS),
                targetApp = "Settings"
            )
            "Camera" -> startExplicitAction(
                intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE),
                targetApp = "Camera"
            )
            else -> startInstalledApp(parsedIntent.targetApp)
        }
    }

    private fun startInstalledApp(targetApp: String): ActionResult {
        val packageName = packageNames[targetApp]
            ?: return ActionResult(false, "Auri does not know how to open $targetApp yet.")

        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: return ActionResult(false, "$targetApp is not installed on this phone.")

        return startActivitySafely(
            intent = launchIntent,
            successMessage = "Opening $targetApp.",
            failureMessage = "Auri could not open $targetApp."
        )
    }

    private fun startExplicitAction(intent: Intent, targetApp: String): ActionResult {
        val resolvedActivity = intent.resolveActivity(context.packageManager)
            ?: return ActionResult(false, "$targetApp is not available on this phone.")

        intent.component = resolvedActivity
        return startActivitySafely(
            intent = intent,
            successMessage = "Opening $targetApp.",
            failureMessage = "Auri could not open $targetApp."
        )
    }

    private fun startActivitySafely(
        intent: Intent,
        successMessage: String,
        failureMessage: String
    ): ActionResult {
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            ActionResult(true, successMessage)
        } catch (_: ActivityNotFoundException) {
            ActionResult(false, failureMessage)
        } catch (_: SecurityException) {
            ActionResult(false, failureMessage)
        }
    }
}
