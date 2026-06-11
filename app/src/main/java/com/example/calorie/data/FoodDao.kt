package com.example.calorie.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM food_items ORDER BY updatedAt DESC")
    fun observeFoods(): Flow<List<FoodItemEntity>>

    @Transaction
    @Query("SELECT * FROM food_items WHERE id = :id")
    fun observeFood(id: Long): Flow<FoodWithIngredients?>

    @Query("SELECT COUNT(*) FROM food_items")
    suspend fun countFoods(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFood(food: FoodItemEntity): Long

    @Update
    suspend fun updateFood(food: FoodItemEntity)

    @Query("DELETE FROM food_items WHERE id = :id")
    suspend fun deleteFood(id: Long)

    @Query("DELETE FROM ingredients WHERE foodItemId = :foodItemId")
    suspend fun deleteIngredients(foodItemId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<IngredientEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity)

    @Query("SELECT * FROM recipes ORDER BY importedAt DESC")
    fun observeRecipes(): Flow<List<RecipeEntity>>
}
