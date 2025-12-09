package com.example.dresscode.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val username: String, // 🔴 登录账号 (唯一，注册后不可改)
    val password: String, // 登录密码

    val nickname: String, // 🔴 新增：显示昵称 (可随意修改)

    val gender: String = "未设置",
    val avatar: String? = null
)