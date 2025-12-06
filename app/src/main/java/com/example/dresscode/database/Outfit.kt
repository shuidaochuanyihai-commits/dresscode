package com.example.dresscode.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outfit_table")
data class Outfit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val imageResId: Int,
    val title: String,
    val gender: String,      // 🔴 新增字段：用于筛选 ("all", "male", "female")
    var isFavorite: Boolean = false
)