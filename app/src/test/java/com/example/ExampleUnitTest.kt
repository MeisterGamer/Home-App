package com.example

import com.example.data.model.AppLanguage
import com.example.localization.LanguageManager
import com.example.voice.VoiceActionResult
import com.example.voice.VoiceCommandHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testVoiceShoppingCommand() {
    val result = VoiceCommandHelper.parseCommand("Add organic milk to shopping list")
    assertTrue(result is VoiceActionResult.AddShoppingItem)
    assertEquals("Organic milk", (result as VoiceActionResult.AddShoppingItem).name)
  }

  @Test
  fun testVoiceChoreCompletionCommand() {
    val result = VoiceCommandHelper.parseCommand("Complete unload clean dishes")
    assertTrue(result is VoiceActionResult.CompleteChore)
  }

  @Test
  fun testVoiceUrgentRepairCommand() {
    val result = VoiceCommandHelper.parseCommand("Urgent repair water pipe leak in kitchen")
    assertTrue(result is VoiceActionResult.AddUrgentRepair)
  }

  @Test
  fun testLanguageManager() {
    val esHome = LanguageManager.getString("tab_home", AppLanguage.ES)
    assertEquals("Inicio", esHome)

    val deBudget = LanguageManager.getString("tab_budget", AppLanguage.DE)
    assertEquals("Budget", deBudget)
  }
}
