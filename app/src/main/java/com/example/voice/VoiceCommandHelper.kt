package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

sealed class VoiceActionResult {
    data class AddShoppingItem(val name: String, val category: String = "Groceries") : VoiceActionResult()
    data class CompleteChore(val choreQuery: String) : VoiceActionResult()
    data class AddUrgentRepair(val title: String, val description: String) : VoiceActionResult()
    data class LogExpense(val amount: Double, val title: String) : VoiceActionResult()
    object ToggleSeniorMode : VoiceActionResult()
    object TakeMedication : VoiceActionResult()
    data class Unknown(val rawText: String) : VoiceActionResult()
}

class VoiceCommandHelper(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null

    init {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onError(error: Int) {
                        onError("Speech recognition error: code $error")
                    }
                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            onResult(matches[0])
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    fun startListening() {
        if (speechRecognizer != null) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a home command, e.g., 'Add milk to shopping list'")
            }
            try {
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                onError("Failed to start speech listener: ${e.message}")
            }
        } else {
            onError("Speech recognition service is not available on this device")
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    fun destroy() {
        speechRecognizer?.destroy()
    }

    companion object {
        fun parseCommand(rawText: String): VoiceActionResult {
            val text = rawText.lowercase().trim()

            // 1. Shopping list intent
            if (text.contains("shopping") || text.contains("buy") || text.contains("groceries")) {
                val cleaned = text
                    .replace("add", "")
                    .replace("to shopping list", "")
                    .replace("to the shopping list", "")
                    .replace("to shopping", "")
                    .replace("to buy list", "")
                    .replace("i need to buy", "")
                    .replace("need to buy", "")
                    .replace("buy", "")
                    .replace("please", "")
                    .trim()
                if (cleaned.isNotEmpty()) {
                    return VoiceActionResult.AddShoppingItem(cleaned.replaceFirstChar { it.uppercase() })
                }
            }

            // 2. Complete chore intent
            if (text.contains("complete") || text.contains("finished") || text.contains("done with") || text.contains("did")) {
                val choreQuery = text
                    .replace("complete", "")
                    .replace("finished", "")
                    .replace("done with", "")
                    .replace("i did", "")
                    .replace("mark", "")
                    .replace("done", "")
                    .replace("the chore", "")
                    .replace("chore", "")
                    .trim()
                if (choreQuery.isNotEmpty()) {
                    return VoiceActionResult.CompleteChore(choreQuery)
                }
            }

            // 3. Urgent repair intent
            if (text.contains("urgent") || text.contains("repair") || text.contains("leak") || text.contains("broken")) {
                val repairTitle = text
                    .replace("there is a", "")
                    .replace("urgent repair", "")
                    .replace("urgent", "")
                    .replace("report", "")
                    .trim()
                    .replaceFirstChar { it.uppercase() }
                return VoiceActionResult.AddUrgentRepair(
                    title = if (repairTitle.isNotEmpty()) repairTitle else "Urgent Home Repair",
                    description = "Reported via Hands-Free Voice Command: '$rawText'"
                )
            }

            // 4. Log expense intent
            if (text.contains("expense") || text.contains("spent") || text.contains("dollar") || text.contains("$")) {
                val regex = Regex("""(?:spent|log expense|dollar|dollars|\$)?\s*(\d+(?:\.\d{1,2})?)\s*(?:dollar|dollars|\$)?\s*(?:for|on)?\s*(.*)""")
                val match = regex.find(text)
                if (match != null) {
                    val amountStr = match.groupValues[1]
                    val item = match.groupValues[2].trim().ifEmpty { "Household Expense" }
                    val amount = amountStr.toDoubleOrNull() ?: 20.0
                    return VoiceActionResult.LogExpense(amount, item.replaceFirstChar { it.uppercase() })
                }
            }

            // 5. Senior / Elder mode toggle
            if (text.contains("senior") || text.contains("elder") || text.contains("grandma") || text.contains("old people")) {
                return VoiceActionResult.ToggleSeniorMode
            }

            // 6. Medication taken
            if (text.contains("medicine") || text.contains("medication") || text.contains("pill") || text.contains("took my")) {
                return VoiceActionResult.TakeMedication
            }

            return VoiceActionResult.Unknown(rawText)
        }
    }
}
