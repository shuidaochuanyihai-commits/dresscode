package com.example.dresscode.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface OutfitDao {
    // 1. 插入多条穿搭数据 (用来初始化数据)
    @Insert
    suspend fun insertAll(outfits: List<Outfit>)

    // 2. 更新穿搭的收藏状态
    @Update
    suspend fun updateOutfit(outfit: Outfit)

    // 3. 获取所有穿搭 (首页展示用)
    @Query("SELECT * FROM outfit_table")
    fun getAllOutfits(): List<Outfit> // 注意：这里不用 suspend，因为 Room 会自动处理 Flow/LiveData

    // 🔴 新增：根据性别筛选穿搭
    @Query("SELECT * FROM outfit_table WHERE gender = :gender")
    fun getOutfitsByGender(gender: String): List<Outfit>

    // 4. 获取所有收藏的穿搭 (为“智能换装模块”做准备)
    @Query("SELECT * FROM outfit_table WHERE isFavorite = 1")
    suspend fun getFavoriteOutfits(): List<Outfit>

    // 5. 检查数据库是否为空 (防止重复插入假数据)
    @Query("SELECT COUNT(id) FROM outfit_table")
    suspend fun getCount(): Int

    // 🔴 新增：根据标题模糊搜索 (忽略大小写)
    // gender 逻辑：如果当前选了性别，还得在性别范围内搜；如果没选，就全局搜
    @Query("SELECT * FROM outfit_table WHERE title LIKE '%' || :keyword || '%' AND (gender = :gender OR :gender = 'all')")
    suspend fun searchOutfits(keyword: String, gender: String): List<Outfit>
}