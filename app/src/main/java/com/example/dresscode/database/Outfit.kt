package com.example.dresscode.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outfit_table")
data class Outfit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val imageResId: Int, // 本地资源ID (预设的)
    val title: String,
    val gender: String,
    var style: String = "其他",
    var season: String = "四季",
    var scene: String = "日常",
    var isFavorite: Boolean = false,

    // 🔴 新增：支持用户上传的图片路径
    val imagePath: String? = null
)