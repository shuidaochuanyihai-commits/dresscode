package com.example.dresscode

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.dresscode.database.AppDatabase
import com.example.dresscode.database.User
import kotlinx.coroutines.launch
import android.content.Context

// 继承 AndroidViewModel 可以直接获取 application 上下文，方便拿数据库
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    // 1. 获取数据库操作工具 (DAO)
    private val userDao = AppDatabase.getDatabase(application).userDao()

    // 2. 定义两个 LiveData，用来通知 Activity 结果
    // 登录结果：true=成功，false=失败
    val loginResult = MutableLiveData<Boolean>()
    // 注册结果：字符串消息（比如 "注册成功" 或 "用户名已存在"）
    val registerMessage = MutableLiveData<String>()

    // 3. 注册逻辑
    fun register(name: String, pwd: String) {
        viewModelScope.launch {
            val existingUser = userDao.getUserByName(name)
            if (existingUser != null) {
                registerMessage.value = "账号已存在，换一个吧"
            } else {
                // 🔴 修改：创建用户时，同时设置 username(账号) 和 nickname(昵称)
                // 默认昵称 = 账号名
                val newUser = User(
                    username = name,
                    password = pwd,
                    nickname = name // 初始昵称和账号一样
                )
                userDao.insertUser(newUser)
                registerMessage.value = "注册成功！请登录"
            }
        }
    }

    // 4. 登录逻辑
    // 修改 LoginViewModel.kt
    fun login(name: String, pwd: String) {
        viewModelScope.launch {
            val user = userDao.login(name, pwd)
            if (user != null) {
                // 🔴 关键修改：登录成功时，把 User ID 存到 SharedPreferences
                val prefs = getApplication<Application>().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                prefs.edit().putInt("current_user_id", user.id).apply()

                loginResult.value = true
            } else {
                loginResult.value = false
            }
        }
    }
}