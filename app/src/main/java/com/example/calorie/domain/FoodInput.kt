package com.example.calorie.domain

data class IngredientInput(
    val name: String,
    val weightGram: String,
    val kcalPer100Gram: String
)

data class FoodInput(
    val id: Long = 0,
    val name: String,
    val category: String,
    val servingSizeGram: String,
    val manualCalories: String,
    val photoUri: String?,
    val note: String,
    val isCustom: Boolean,
    val ingredients: List<IngredientInput>
)

data class ValidationResult(
    val isValid: Boolean,
    val message: String? = null
)

object CalorieCalculator {
    fun ingredientCalories(weightGram: Double, kcalPer100Gram: Double): Int {
        return ((weightGram * kcalPer100Gram) / 100).toInt()
    }

    fun calculateBmr(heightCm: Float, weightKg: Float, age: Int, isMale: Boolean): Float {
        var bmr = (10 * weightKg) + (6.25f * heightCm) - (5 * age)
        bmr += if (isMale) 5f else -161f
        return bmr
    }

    fun totalIngredientCalories(ingredients: List<IngredientInput>): Int {
        return ingredients.sumOf {
            ingredientCalories(
                weightGram = it.weightGram.toDoubleOrNull() ?: 0.0,
                kcalPer100Gram = it.kcalPer100Gram.toDoubleOrNull() ?: 0.0
            )
        }
    }

    fun validate(input: FoodInput): ValidationResult {
        if (input.name.isBlank()) return ValidationResult(false, "음식 이름을 입력해줘.")
        val servingSize = input.servingSizeGram.toIntOrNull()
        if (servingSize == null || servingSize <= 0) {
            return ValidationResult(false, "1회 제공량은 1g 이상이어야 해.")
        }
        val manualCalories = input.manualCalories.toIntOrNull()
        if (manualCalories == null || manualCalories <= 0) {
            return ValidationResult(false, "칼로리는 1kcal 이상이어야 해.")
        }
        input.ingredients.forEach { ingredient ->
            if (ingredient.name.isBlank()) return ValidationResult(false, "재료 이름을 입력해줘.")
            val weight = ingredient.weightGram.toIntOrNull()
            val kcal = ingredient.kcalPer100Gram.toIntOrNull()
            if (weight == null || weight <= 0) return ValidationResult(false, "재료 중량은 1g 이상이어야 해.")
            if (kcal == null || kcal <= 0) return ValidationResult(false, "재료 100g당 칼로리는 1kcal 이상이어야 해.")
        }
        return ValidationResult(true)
    }
}
