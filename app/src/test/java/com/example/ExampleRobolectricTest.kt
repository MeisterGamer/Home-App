package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.ShoppingItemEntity
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
}
