package com.example.dresscode.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outfit_table")
data class Outfit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val imageResId: Int,
    val title: String,
    val gender: String, // male, female, all

    // 🔴 新增：AI 识别的标签
    var style: String = "其他",   // 风格 (如：休闲、商务、复古)
    var season: String = "四季",  // 季节 (如：夏季、冬季)
    var scene: String = "日常",   // 场景 (如：上班、约会、运动)

    var isFavorite: Boolean = false
)