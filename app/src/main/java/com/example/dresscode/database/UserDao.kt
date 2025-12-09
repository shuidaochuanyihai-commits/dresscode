package com.example.dresscode.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

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

    // --- 下面是新增的方法 ---

    // 1. 根据 ID 找用户 (用于设置页加载信息)
    @Query("SELECT * FROM user_table WHERE id = :uid")
    suspend fun getUserById(uid: Int): User?

    // 2. 更新用户信息 (改名、换头像用)
    @Update
    suspend fun updateUser(user: User)

    // 3. 获取所有用户 (管理员后台用)
    @Query("SELECT * FROM user_table")
    suspend fun getAllUsers(): List<User>

    // 4. 统计用户总数 (管理员后台用)
    @Query("SELECT COUNT(id) FROM user_table")
    suspend fun getUserCount(): Int

    // 5. 删除某个用户 (管理员后台用)
    @Delete
    suspend fun deleteUser(user: User)
}