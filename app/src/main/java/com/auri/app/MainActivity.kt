package com.auri.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private val intentEngine: IntentEngine = RuleBasedIntentEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val actionExecutor = AndroidActionExecutor(applicationContext)

        setContent {
            AuriTheme {
                AuriApp(
                    intentEngine = intentEngine,
                    actionExecutor = actionExecutor
                )
            }
        }
    }
}

@Composable
fun AuriApp(
    intentEngine: IntentEngine,
    actionExecutor: AndroidActionExecutor
) {
    var command by rememberSaveable { mutableStateOf("") }
    var parsedIntent by remember { mutableStateOf<ParsedIntent?>(null) }
    var statusMessage by rememberSaveable { mutableStateOf("Type a command to get started.") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF6F7F9)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Header()

            OutlinedTextField(
                value = command,
                onValueChange = {
                    command = it
                    parsedIntent = null
                    statusMessage = "Ready to understand your command."
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Command") },
                placeholder = { Text("Open WhatsApp") },
                singleLine = true
            )

            Button(
                onClick = {
                    parsedIntent = intentEngine.parse(command)
                    statusMessage = if (parsedIntent?.intentType == IntentType.OPEN_APP) {
                        "Auri understood the command. Review the plan before confirming."
                    } else {
                        "Auri does not support that command yet."
                    }
                },
                enabled = command.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Understand Command")
            }

            parsedIntent?.let { intent ->
                IntentPreviewCard(
                    parsedIntent = intent,
                    onConfirm = {
                        val result = actionExecutor.execute(intent)
                        statusMessage = result.message
                    },
                    onCancel = {
                        parsedIntent = null
                        statusMessage = "Command cancelled."
                    }
                )
            }

            StatusCard(statusMessage)
        }
    }
}

@Composable
private fun Header() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Auri",
            color = Color(0xFF172033),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Private on-device AI for real phone tasks",
            color = Color(0xFF526070),
            fontSize = 16.sp
        )
    }
}

@Composable
private fun IntentPreviewCard(
    parsedIntent: ParsedIntent,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            PreviewRow(label = "Detected intent", value = parsedIntent.intentType.name)
            PreviewRow(label = "Target app", value = parsedIntent.targetApp ?: "Unknown")
            PreviewRow(label = "Confidence", value = "${(parsedIntent.confidence * 100).toInt()}%")

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Planned steps",
                    color = Color(0xFF526070),
                    style = MaterialTheme.typography.labelLarge
                )
                parsedIntent.plannedSteps.forEachIndexed { index, step ->
                    Text(
                        text = "${index + 1}. $step",
                        color = Color(0xFF172033),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = onConfirm,
                    enabled = parsedIntent.requiresConfirmation &&
                        parsedIntent.intentType == IntentType.OPEN_APP,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Confirm")
                }
            }
        }
    }
}

@Composable
private fun PreviewRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            color = Color(0xFF526070),
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            text = value,
            color = Color(0xFF172033),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatusCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF1F8))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Status",
                color = Color(0xFF526070),
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                color = Color(0xFF172033),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun AuriTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = Color(0xFF1C6E8C),
            secondary = Color(0xFFCF6F4E),
            surface = Color.White,
            background = Color(0xFFF6F7F9)
        ),
        content = content
    )
}

@Preview(showBackground = true)
@Composable
private fun AuriAppPreview() {
    AuriTheme {
        Column(
            modifier = Modifier
                .background(Color(0xFFF6F7F9))
                .padding(24.dp)
        ) {
            Header()
            Spacer(modifier = Modifier.height(18.dp))
            IntentPreviewCard(
                parsedIntent = RuleBasedIntentEngine().parse("Open Gmail"),
                onConfirm = {},
                onCancel = {}
            )
        }
    }
}
