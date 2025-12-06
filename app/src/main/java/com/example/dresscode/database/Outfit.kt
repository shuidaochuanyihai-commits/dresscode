package com.example.dresscode.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// 🔴 关键点：必须有 @Entity，且 tableName 正确
@Entity(tableName = "outfit_table")
data class Outfit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val imageResId: Int,   // 图片资源 ID
    val title: String,     // 标题
    var isFavorite: Boolean = false // 是否收藏
)