package com.example.calorie.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.calorie.domain.FoodInput
import com.example.calorie.domain.IngredientInput
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FoodRepositoryTest {
    private lateinit var database: CalorieDatabase
    private lateinit var repository: FoodRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CalorieDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = FoodRepository(database.foodDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveFood_persistsFoodAndIngredients() = runBlocking {
        repository.saveFood(customFood())

        val food = repository.foods.first().first()
        val detail = repository.observeFood(food.id).first()

        assertEquals("계란 볶음밥", food.name)
        assertEquals(2, detail?.ingredients?.size)
    }

    @Test
    fun deleteFood_removesFood() = runBlocking {
        repository.saveFood(customFood())
        val foodId = repository.foods.first().first().id

        repository.deleteFood(foodId)

        assertNull(repository.observeFood(foodId).first())
    }

    private fun customFood(): FoodInput {
        return FoodInput(
            name = "계란 볶음밥",
            category = "직접 만든 음식",
            servingSizeGram = "350",
            manualCalories = "549",
            photoUri = null,
            note = "테스트 데이터",
            isCustom = true,
            ingredients = listOf(
                IngredientInput("밥", "210", "145"),
                IngredientInput("계란", "100", "155")
            )
        )
    }
}
