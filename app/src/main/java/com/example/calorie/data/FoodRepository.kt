package com.example.calorie.data

import com.example.calorie.domain.CalorieCalculator
import com.example.calorie.domain.FoodInput
import com.example.calorie.domain.IngredientInput
import com.example.calorie.network.MealDbRecipe
import kotlinx.coroutines.flow.Flow

class FoodRepository(private val dao: FoodDao) {
    val foods: Flow<List<FoodItemEntity>> = dao.observeFoods()
    val importedRecipes: Flow<List<RecipeEntity>> = dao.observeRecipes()

    fun observeFood(id: Long): Flow<FoodWithIngredients?> = dao.observeFood(id)

    fun observeMealsByDate(date: String): Flow<List<MealRecordWithFood>> = dao.observeMealsByDate(date)

    suspend fun saveMealRecord(record: MealRecordEntity) {
        dao.insertMealRecord(record)
    }

    suspend fun deleteMealRecord(id: Long) {
        dao.deleteMealRecord(id)
    }

    suspend fun saveFood(input: FoodInput) {
        val now = System.currentTimeMillis()
        val calories = input.manualCalories.toDoubleOrNull()?.toInt() ?: 0
        val food = FoodItemEntity(
            id = input.id,
            name = input.name.trim(),
            category = input.category.trim().ifBlank { "기타" },
            servingSizeGram = input.servingSizeGram.toDoubleOrNull()?.toInt() ?: 100,
            calories = calories,
            photoUri = input.photoUri,
            note = input.note.trim(),
            isCustom = input.isCustom,
            createdAt = if (input.id == 0L) now else now,
            updatedAt = now
        )
        val foodId = if (input.id == 0L) {
            dao.insertFood(food)
        } else {
            dao.updateFood(food)
            input.id
        }
        dao.deleteIngredients(foodId)
        dao.insertIngredients(
            input.ingredients.map {
                IngredientEntity(
                    foodItemId = foodId,
                    name = it.name.trim(),
                    weightGram = it.weightGram.toDoubleOrNull()?.toInt() ?: 0,
                    kcalPer100Gram = it.kcalPer100Gram.toDoubleOrNull()?.toInt() ?: 0
                )
            }
        )
    }

    suspend fun deleteFood(id: Long) {
        dao.deleteFood(id)
    }

    suspend fun importRecipe(recipe: MealDbRecipe) {
        dao.insertRecipe(
            RecipeEntity(
                externalId = recipe.externalId,
                name = recipe.name,
                category = recipe.category,
                area = recipe.area,
                instructions = recipe.instructions,
                imageUrl = recipe.imageUrl,
                sourceUrl = recipe.sourceUrl,
                ingredientsText = recipe.ingredients.joinToString("\n"),
                importedAt = System.currentTimeMillis()
            )
        )
        saveFood(
            FoodInput(
                name = recipe.name,
                category = "레시피/${recipe.category}",
                servingSizeGram = recipe.servingSize,
                manualCalories = recipe.kcal,
                photoUri = recipe.imageUrl,
                note = buildString {
                    appendLine("${recipe.area} 요리 · 식약처에서 가져온 레시피")
                    if (recipe.ingredients.isNotEmpty()) {
                        appendLine()
                        appendLine("재료")
                        recipe.ingredients.take(8).forEach { appendLine("- $it") }
                    }
                    appendLine()
                    append(recipe.instructions.take(500))
                },
                isCustom = false,
                ingredients = emptyList()
            )
        )
    }


}
