package com.example.calorie.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipes",
    indices = [Index(value = ["externalId"], unique = true)]
)
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val externalId: String,
    val name: String,
    val category: String,
    val area: String,
    val instructions: String,
    val imageUrl: String?,
    val sourceUrl: String?,
    val ingredientsText: String,
    val importedAt: Long
)
