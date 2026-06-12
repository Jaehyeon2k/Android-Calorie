package com.example.calorie.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "meal_records",
    foreignKeys = [
        ForeignKey(
            entity = FoodItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["foodItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("foodItemId")]
)
data class MealRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // format: "yyyy-MM-dd"
    val mealType: String, // "아침", "점심", "저녁", "간식"
    val foodItemId: Long,
    val consumedGrams: Int, // 얼마나 먹었는지 (g)
    val consumedCalories: Int, // 섭취 칼로리
    val timestamp: Long = System.currentTimeMillis()
)

data class MealRecordWithFood(
    @Embedded val record: MealRecordEntity,
    @Relation(
        parentColumn = "foodItemId",
        entityColumn = "id"
    )
    val food: FoodItemEntity
)
