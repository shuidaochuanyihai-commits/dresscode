package com.example.dresscode

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.dresscode.database.AppDatabase
import com.example.dresscode.database.User
import kotlinx.coroutines.launch

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
            // 先查查有没有这个人
            val existingUser = userDao.getUserByName(name)
            if (existingUser != null) {
                registerMessage.value = "用户名已存在，换一个吧"
            } else {
                // 没有就插入新用户
                val newUser = User(username = name, password = pwd)
                userDao.insertUser(newUser)
                registerMessage.value = "注册成功！请登录"
            }
        }
    }

    // 4. 登录逻辑
    fun login(name: String, pwd: String) {
        viewModelScope.launch {
            // 去数据库查匹配的用户
            val user = userDao.login(name, pwd)
            if (user != null) {
                // 查到了，通知 Activity 登录成功
                loginResult.value = true
                // 这里还可以把用户ID存到 SharedPreferences，以后做“我的”模块用
            } else {
                // 没查到
                loginResult.value = false
            }
        }
    }
}