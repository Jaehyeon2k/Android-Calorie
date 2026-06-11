package com.example.calorie.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalorieCalculatorTest {
    @Test
    fun ingredientCalories_calculatesFromWeightAndKcalPer100Gram() {
        assertEquals(198, CalorieCalculator.ingredientCalories(120, 165))
    }

    @Test
    fun totalIngredientCalories_sumsEveryIngredient() {
        val ingredients = listOf(
            IngredientInput("밥", "210", "145"),
            IngredientInput("계란", "100", "155"),
            IngredientInput("식용유", "10", "900")
        )

        assertEquals(549, CalorieCalculator.totalIngredientCalories(ingredients))
    }

    @Test
    fun validate_rejectsBlankFoodName() {
        val result = CalorieCalculator.validate(validInput().copy(name = ""))

        assertFalse(result.isValid)
        assertEquals("음식 이름을 입력해줘.", result.message)
    }

    @Test
    fun validate_acceptsValidCustomFood() {
        val result = CalorieCalculator.validate(validInput())

        assertTrue(result.isValid)
    }

    private fun validInput(): FoodInput {
        return FoodInput(
            name = "계란 볶음밥",
            category = "직접 만든 음식",
            servingSizeGram = "350",
            manualCalories = "549",
            photoUri = null,
            note = "",
            isCustom = true,
            ingredients = listOf(
                IngredientInput("밥", "210", "145"),
                IngredientInput("계란", "100", "155")
            )
        )
    }
}
