package com.example.dresscode.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    // 注册：插入一个新用户
    @Insert
    suspend fun insertUser(user: User)

    // 登录：根据用户名和密码查找用户
    @Query("SELECT * FROM user_table WHERE username = :name AND password = :pwd LIMIT 1")
    suspend fun login(name: String, pwd: String): User?

    // 查重：看看用户名是不是已经被注册了
    @Query("SELECT * FROM user_table WHERE username = :name LIMIT 1")
    suspend fun getUserByName(name: String): User?
}