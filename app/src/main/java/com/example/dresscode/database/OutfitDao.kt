package com.example.dresscode.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface OutfitDao {
    // 1. 插入多条穿搭数据 (初始化用)
    @Insert
    suspend fun insertAll(outfits: List<Outfit>)

    // 2. 更新穿搭的收藏状态或标签
    @Update
    suspend fun updateOutfit(outfit: Outfit)

    // 🔴 修改 1：获取所有穿搭 (加了 ORDER BY id DESC)
    @Query("SELECT * FROM outfit_table ORDER BY id DESC")
    fun getAllOutfits(): List<Outfit>

    // 🔴 修改 2：根据性别筛选 (加了 ORDER BY id DESC)
    @Query("SELECT * FROM outfit_table WHERE gender = :gender ORDER BY id DESC")
    fun getOutfitsByGender(gender: String): List<Outfit>

    // 🔴 修改 3：高级筛选 (加了 ORDER BY id DESC)
    @Query("""
        SELECT * FROM outfit_table 
        WHERE (gender = :gender OR :gender = 'all')
        AND title LIKE '%' || :keyword || '%'
        AND (:style = '' OR style = :style)
        AND (:season = '' OR season = :season)
        AND (:scene = '' OR scene = :scene)
        ORDER BY id DESC
    """)
    suspend fun filterOutfits(
        keyword: String,
        gender: String,
        style: String,
        season: String,
        scene: String
    ): List<Outfit>

    // 获取所有收藏的穿搭 (收藏列表通常也可以倒序，看你喜好)
    @Query("SELECT * FROM outfit_table WHERE isFavorite = 1 ORDER BY id DESC")
    suspend fun getFavoriteOutfits(): List<Outfit>

    // 检查数据库是否为空
    @Query("SELECT COUNT(id) FROM outfit_table")
    suspend fun getCount(): Int
}