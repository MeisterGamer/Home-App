package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

sealed class VoiceActionResult {
    data class AddShoppingItem(val name: String, val category: String = "Lebensmittel") : VoiceActionResult()
    data class CompleteChore(val choreQuery: String) : VoiceActionResult()
    data class AddUrgentRepair(val title: String, val description: String) : VoiceActionResult()
    data class LogExpense(val amount: Double, val title: String) : VoiceActionResult()
    data class ToggleSmartLights(val turnOn: Boolean) : VoiceActionResult()
    data class ToggleSmartHeating(val turnOn: Boolean) : VoiceActionResult()
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
                        onError("Spracherkennungsfehler: Code $error")
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
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Befehl sprechen, z. B. 'Milch zur Einkaufsliste' oder 'Geschirr erledigt'")
            }
            try {
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                onError("Spracherkennung konnte nicht gestartet werden: ${e.message}")
            }
        } else {
            onError("Spracherkennung auf diesem Gerät nicht verfügbar")
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

            // 1. Expense intent (German & English: "15 euro für bäckerei", "ausgabe 25€ supermarkt", "$40 groceries")
            val expenseRegex = Regex("""(?:ausgabe|ausgegeben|gekostet|expense|spent|log expense)?\s*(?:von)?\s*(\d+(?:[.,]\d{1,2})?)\s*(?:€|euro|dollar|dollars|\$)?\s*(?:für|for|on|in)?\s*(.*)""")
            if (text.contains("euro") || text.contains("€") || text.contains("dollar") || text.contains("$") || text.contains("ausgabe") || text.contains("spent") || text.contains("expense")) {
                val match = expenseRegex.find(text)
                if (match != null) {
                    val amountStr = match.groupValues[1].replace(",", ".")
                    val item = match.groupValues[2]
                        .replace("euro", "")
                        .replace("€", "")
                        .replace("dollar", "")
                        .replace("$", "")
                        .trim()
                        .ifEmpty { "Haushaltsausgabe" }
                    val amount = amountStr.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        return VoiceActionResult.LogExpense(amount, item.replaceFirstChar { it.uppercase() })
                    }
                }
            }

            // 2. Shopping list intent (German: "milch zur einkaufsliste", "kaufe butter", "brot besorgen"; English: "add milk to shopping")
            if (text.contains("einkauf") || text.contains("kauf") || text.contains("besorg") || text.contains("shopping") || text.contains("buy") || text.contains("groceries")) {
                val cleaned = text
                    .replace("zur einkaufsliste hinzufügen", "")
                    .replace("auf die einkaufsliste", "")
                    .replace("zur einkaufsliste", "")
                    .replace("auf einkaufsliste", "")
                    .replace("einkaufsliste", "")
                    .replace("bitte kaufen", "")
                    .replace("kaufen", "")
                    .replace("kaufe", "")
                    .replace("besorgen", "")
                    .replace("besorge", "")
                    .replace("brauchen noch", "")
                    .replace("wir brauchen", "")
                    .replace("füge", "")
                    .replace("hinzufügen", "")
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

            // 3. Complete chore intent (German: "geschirr erledigt", "küche geputzt", "müll fertig"; English: "dishes complete")
            if (text.contains("erledigt") || text.contains("fertig") || text.contains("gemacht") || text.contains("abgehakt") ||
                text.contains("complete") || text.contains("finished") || text.contains("done with") || text.contains("did")
            ) {
                val choreQuery = text
                    .replace("als erledigt markieren", "")
                    .replace("habe ich gemacht", "")
                    .replace("habe ich erledigt", "")
                    .replace("ist erledigt", "")
                    .replace("erledigt", "")
                    .replace("fertig", "")
                    .replace("gemacht", "")
                    .replace("abgehakt", "")
                    .replace("complete", "")
                    .replace("finished", "")
                    .replace("done with", "")
                    .replace("i did", "")
                    .replace("mark", "")
                    .replace("done", "")
                    .replace("the chore", "")
                    .replace("chore", "")
                    .replace("aufgabe", "")
                    .trim()
                if (choreQuery.isNotEmpty()) {
                    return VoiceActionResult.CompleteChore(choreQuery)
                }
            }

            // 4. Urgent repair intent (German: "rohrbruch", "wasserschaden", "dringend", "kaputt", "reparatur"; English: "urgent", "leak", "broken")
            if (text.contains("dringend") || text.contains("reparatur") || text.contains("kaputt") || text.contains("rohrbruch") ||
                text.contains("wasserschaden") || text.contains("urgent") || text.contains("repair") || text.contains("leak") || text.contains("broken")
            ) {
                val repairTitle = text
                    .replace("dringende reparatur", "")
                    .replace("dringend", "")
                    .replace("reparatur", "")
                    .replace("melden", "")
                    .replace("es gibt einen", "")
                    .replace("es gibt eine", "")
                    .replace("there is a", "")
                    .replace("urgent repair", "")
                    .replace("urgent", "")
                    .replace("report", "")
                    .trim()
                    .replaceFirstChar { it.uppercase() }
                return VoiceActionResult.AddUrgentRepair(
                    title = if (repairTitle.isNotEmpty()) repairTitle else "Dringende Haushaltsreparatur",
                    description = "Per Sprachassistent gemeldet: '$rawText'"
                )
            }

            // 5. Senior / Elder mode toggle
            if (text.contains("senior") || text.contains("pflege") || text.contains("oma") || text.contains("opa") || text.contains("elder")) {
                return VoiceActionResult.ToggleSeniorMode
            }

            // 6. Medication taken
            if (text.contains("medikament") || text.contains("tablette") || text.contains("pill") || text.contains("medicine") || text.contains("medication") || text.contains("eingenommen")) {
                return VoiceActionResult.TakeMedication
            }

            // 7. Smart Home Lights toggle
            if (text.contains("licht") || text.contains("beleuchtung") || text.contains("light") || text.contains("lights") || text.contains("lampe")) {
                val turnOff = text.contains("aus") || text.contains("off") || text.contains("ab")
                return VoiceActionResult.ToggleSmartLights(!turnOff)
            }

            // 8. Smart Home Heating toggle
            if (text.contains("heizung") || text.contains("wärme") || text.contains("heat") || text.contains("heating") || text.contains("thermostat")) {
                val turnOff = text.contains("aus") || text.contains("off") || text.contains("eco")
                return VoiceActionResult.ToggleSmartHeating(!turnOff)
            }

            return VoiceActionResult.Unknown(rawText)
        }
    }
}
