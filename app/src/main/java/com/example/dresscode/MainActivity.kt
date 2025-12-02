package com.example.dresscode

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. 获取 NavHostFragment
        // 我们在 XML 里定义的 FragmentContainerView 的 ID 是 nav_host_fragment
        // 这里必须强转为 NavHostFragment 才能获取 NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        // 2. 获取 NavController
        // NavController 是负责管理页面跳转的核心组件
        val navController = navHostFragment.navController

        // 3. 获取 BottomNavigationView
        // 获取 XML 里的底部导航栏视图
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_view)

        // 4. 将底部导航栏与 NavController 绑定
        // 这一步是关键：它会自动处理点击事件，点击 Tab 时切换到对应的 Fragment
        // 前提是：menu 里的 item id 必须和 nav_graph 里的 fragment id 完全一致
        bottomNav.setupWithNavController(navController)
    }
}