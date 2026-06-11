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

    suspend fun saveFood(input: FoodInput) {
        val now = System.currentTimeMillis()
        val calories = input.manualCalories.toInt()
        val food = FoodItemEntity(
            id = input.id,
            name = input.name.trim(),
            category = input.category.trim().ifBlank { "기타" },
            servingSizeGram = input.servingSizeGram.toInt(),
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
                    weightGram = it.weightGram.toInt(),
                    kcalPer100Gram = it.kcalPer100Gram.toInt()
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
                servingSizeGram = "100",
                manualCalories = "100",
                photoUri = recipe.imageUrl,
                note = buildString {
                    appendLine("${recipe.area} 요리 · TheMealDB에서 가져온 레시피")
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

    suspend fun seedIfEmpty() {
        if (dao.countFoods() > 0) return
        seedFoods.forEach { saveFood(it) }
    }

    private val seedFoods = listOf(
        FoodInput(
            name = "김밥",
            category = "한식",
            servingSizeGram = "250",
            manualCalories = "485",
            photoUri = null,
            note = "기본 seed 데이터",
            isCustom = false,
            ingredients = emptyList()
        ),
        FoodInput(
            name = "라면",
            category = "면류",
            servingSizeGram = "550",
            manualCalories = "500",
            photoUri = null,
            note = "국물 포함 대략값",
            isCustom = false,
            ingredients = emptyList()
        ),
        FoodInput(
            name = "닭가슴살 샐러드",
            category = "샐러드",
            servingSizeGram = "300",
            manualCalories = "320",
            photoUri = null,
            note = "직접 만든 음식 예시",
            isCustom = true,
            ingredients = listOf(
                IngredientInput("닭가슴살", "120", "165"),
                IngredientInput("채소 믹스", "150", "25"),
                IngredientInput("드레싱", "30", "280")
            )
        ),
        FoodInput(
            name = "계란 볶음밥",
            category = "직접 만든 음식",
            servingSizeGram = "350",
            manualCalories = CalorieCalculator.totalIngredientCalories(
                listOf(
                    IngredientInput("밥", "210", "145"),
                    IngredientInput("계란", "100", "155"),
                    IngredientInput("식용유", "10", "900")
                )
            ).toString(),
            photoUri = null,
            note = "재료 합산형 seed 데이터",
            isCustom = true,
            ingredients = listOf(
                IngredientInput("밥", "210", "145"),
                IngredientInput("계란", "100", "155"),
                IngredientInput("식용유", "10", "900")
            )
        )
    )
}
