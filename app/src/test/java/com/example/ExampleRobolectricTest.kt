package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.entity.ChoreEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.ShoppingItemEntity
import com.example.voice.VoiceActionResult
import com.example.voice.VoiceCommandHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("HomeNest", appName)
  }

  @Test
  fun `test dynamic remaining budget calculation`() {
    val monthlyCap = 1200.0
    val expenses = listOf(
      ExpenseEntity(id = 1, title = "Lebensmittel", category = "LEBENSMITTEL", amount = 85.50, date = 1000L, loggedByName = "Sarah"),
      ExpenseEntity(id = 2, title = "Bäcker", category = "LEBENSMITTEL", amount = 14.50, date = 2000L, loggedByName = "Markus"),
      ExpenseEntity(id = 3, title = "Baumarkt", category = "REPARATUR", amount = 100.00, date = 3000L, loggedByName = "Sarah")
    )
    val totalSpent = expenses.sumOf { it.amount }
    assertEquals(200.0, totalSpent, 0.001)

    val remaining = monthlyCap - totalSpent
    assertEquals(1000.0, remaining, 0.001)

    // Dynamic typed amount deduction
    val typedAmount = 35.50
    val previewRemaining = remaining - typedAmount
    assertEquals(964.50, previewRemaining, 0.001)
  }

  @Test
  fun `test shopping item toggle and attributes`() {
    val item = ShoppingItemEntity(
      id = 1,
      name = "Frische Vollmilch",
      category = "Lebensmittel",
      quantity = "2x",
      estimatedCost = 3.78,
      isPurchased = false,
      addedByName = "Familie",
      priority = "Normal"
    )

    assertFalse(item.isPurchased)
    assertEquals("2x", item.quantity)
    assertEquals(3.78, item.estimatedCost, 0.001)

    // Toggle to checked/purchased
    val toggled = item.copy(isPurchased = true)
    assertTrue(toggled.isPurchased)
  }

  @Test
  fun `test voice command parser for German and English`() {
    // German Shopping
    val cmd1 = VoiceCommandHelper.parseCommand("Milch zur Einkaufsliste hinzufügen")
    assertTrue(cmd1 is VoiceActionResult.AddShoppingItem)
    assertEquals("Milch", (cmd1 as VoiceActionResult.AddShoppingItem).name)

    // German Chore Completion
    val cmd2 = VoiceCommandHelper.parseCommand("Geschirrspüler erledigt")
    assertTrue(cmd2 is VoiceActionResult.CompleteChore)

    // German Expense Logging
    val cmd3 = VoiceCommandHelper.parseCommand("25 Euro für Bäcker ausgegeben")
    assertTrue(cmd3 is VoiceActionResult.LogExpense)
    assertEquals(25.0, (cmd3 as VoiceActionResult.LogExpense).amount, 0.001)

    // German Senior Mode
    val cmd4 = VoiceCommandHelper.parseCommand("Seniorenmodus aktivieren")
    assertTrue(cmd4 is VoiceActionResult.ToggleSeniorMode)

    // English Shopping
    val cmd5 = VoiceCommandHelper.parseCommand("Add organic butter to shopping list")
    assertTrue(cmd5 is VoiceActionResult.AddShoppingItem)
  }

  @Test
  fun `test chore entity urgent repair and completion logic`() {
    val chore = ChoreEntity(
      id = 1,
      title = "Rohrbruch Bad reparieren",
      description = "Dringender Alarm",
      category = "MAINTENANCE",
      assignedMemberId = 1L,
      assignedMemberName = "David",
      dueDate = System.currentTimeMillis(),
      priority = "URGENT",
      isUrgentRepair = true,
      pointsValue = 30
    )

    assertTrue(chore.isUrgentRepair)
    assertFalse(chore.isCompleted)

    val completed = chore.copy(isCompleted = true, completedAt = System.currentTimeMillis())
    assertTrue(completed.isCompleted)
    assertTrue(completed.completedAt != null)
  }

  @Test
  fun `test smart lights and heating voice commands`() {
    val cmdLightsOn = VoiceCommandHelper.parseCommand("Licht im Wohnzimmer an")
    assertTrue(cmdLightsOn is VoiceActionResult.ToggleSmartLights)
    assertTrue((cmdLightsOn as VoiceActionResult.ToggleSmartLights).turnOn)

    val cmdLightsOff = VoiceCommandHelper.parseCommand("Licht ausmachen")
    assertTrue(cmdLightsOff is VoiceActionResult.ToggleSmartLights)
    assertFalse((cmdLightsOff as VoiceActionResult.ToggleSmartLights).turnOn)

    val cmdHeatOn = VoiceCommandHelper.parseCommand("Heizung aufdrehen an")
    assertTrue(cmdHeatOn is VoiceActionResult.ToggleSmartHeating)
    assertTrue((cmdHeatOn as VoiceActionResult.ToggleSmartHeating).turnOn)

    val cmdHeatOff = VoiceCommandHelper.parseCommand("Heizung aus")
    assertTrue(cmdHeatOff is VoiceActionResult.ToggleSmartHeating)
    assertFalse((cmdHeatOff as VoiceActionResult.ToggleSmartHeating).turnOn)
  }

  @Test
  fun `test calendar appointment and task classification`() {
    val appointment = ChoreEntity(
      id = 10,
      title = "Zahnarzttermin Max",
      description = "Kontrolluntersuchung 15:30",
      category = "APPOINTMENT",
      assignedMemberId = 2L,
      assignedMemberName = "Max",
      dueDate = System.currentTimeMillis(),
      priority = "MEDIUM",
      pointsValue = 10
    )

    val task = ChoreEntity(
      id = 11,
      title = "Rasen mähen",
      description = "Gartenarbeit",
      category = "YARD",
      assignedMemberId = 1L,
      assignedMemberName = "Eltern",
      dueDate = System.currentTimeMillis(),
      priority = "LOW",
      pointsValue = 20
    )

    val allEvents = listOf(appointment, task)
    val appointments = allEvents.filter { it.category.equals("APPOINTMENT", ignoreCase = true) }
    val tasks = allEvents.filter { !it.category.equals("APPOINTMENT", ignoreCase = true) }

    assertEquals(1, appointments.size)
    assertEquals("Zahnarzttermin Max", appointments[0].title)
    assertEquals(1, tasks.size)
    assertEquals("Rasen mähen", tasks[0].title)
  }
}
