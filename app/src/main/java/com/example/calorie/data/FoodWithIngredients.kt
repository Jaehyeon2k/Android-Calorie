package com.example.calorie.data

import androidx.room.Embedded
import androidx.room.Relation

data class FoodWithIngredients(
    @Embedded val food: FoodItemEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "foodItemId"
    )
    val ingredients: List<IngredientEntity>
)
