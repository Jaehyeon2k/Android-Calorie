package com.example.calorie.network

data class MealDbRecipe(
    val externalId: String,
    val name: String,
    val category: String,
    val area: String,
    val instructions: String,
    val imageUrl: String?,
    val sourceUrl: String?,
    val ingredients: List<String>
)

data class RecipeSearchState(
    val query: String = "chicken",
    val isLoading: Boolean = false,
    val recipes: List<MealDbRecipe> = emptyList(),
    val error: String? = null
)
