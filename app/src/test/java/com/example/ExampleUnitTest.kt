package com.example

import com.example.util.CurrencyHelper
import com.example.util.JarvisAiAdvisor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun currencyHelper_paiseFormattingAndParsing() {
    val paise = 25050L
    val formatted = CurrencyHelper.formatPaise(paise, "INR")
    assertEquals("₹250.50", formatted)

    val parsed = CurrencyHelper.parseToPaise("250.50", "INR")
    assertEquals(25050L, parsed)

    val parsedInteger = CurrencyHelper.parseToPaise("1500", "INR")
    assertEquals(150000L, parsedInteger)
  }

  @Test
  fun jarvisAiAdvisor_naturalLanguageParsing() {
    val parsed = JarvisAiAdvisor.parseNaturalLanguage("Spent 450 on Uber with UPI")
    assertNotNull(parsed)
    assertEquals(45000L, parsed?.amountPaise)
    assertEquals("Transport & Fuel", parsed?.category)
    assertEquals("UPI", parsed?.paymentMethod)
    assertEquals("EXPENSE", parsed?.type)

    val income = JarvisAiAdvisor.parseNaturalLanguage("Received 75000 salary bonus")
    assertNotNull(income)
    assertEquals(7500000L, income?.amountPaise)
    assertEquals("INCOME", income?.type)
  }
}
