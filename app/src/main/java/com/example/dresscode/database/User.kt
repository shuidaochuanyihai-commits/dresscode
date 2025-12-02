package com.example.dresscode.database // 记得改成你的包名

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,          // 用户ID，自动生成
    val username: String,     // 用户名
    val password: String,     // 密码
    val gender: String = "未设置" // 性别 (后面功能会用到)
)