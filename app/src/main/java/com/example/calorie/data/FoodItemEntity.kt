package com.example.calorie.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "food_items",
    indices = [Index(value = ["name"], unique = true)]
)
data class FoodItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val servingSizeGram: Int,
    val calories: Int,
    val photoUri: String?,
    val note: String,
    val isCustom: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
