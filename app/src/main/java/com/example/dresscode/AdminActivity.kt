package com.example.dresscode

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dresscode.database.AppDatabase
import com.example.dresscode.database.User
import kotlinx.coroutines.launch

class AdminActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var tvUserCount: TextView
    private lateinit var tvOutfitCount: TextView
    private lateinit var rvUsers: RecyclerView
    private lateinit var userAdapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        // 1. 初始化数据库
        db = AppDatabase.getDatabase(this)

        // 2. 绑定控件
        tvUserCount = findViewById(R.id.tv_user_count)
        tvOutfitCount = findViewById(R.id.tv_outfit_count)
        rvUsers = findViewById(R.id.rv_users)
        val btnBack = findViewById<Button>(R.id.btn_back)

        // 3. 初始化列表 (RecyclerView)
        rvUsers.layoutManager = LinearLayoutManager(this)

        // 初始化 Adapter，处理点击事件（删除用户）
        userAdapter = UserAdapter(mutableListOf()) { user ->
            showDeleteDialog(user)
        }
        rvUsers.adapter = userAdapter

        // 4. 返回按钮
        btnBack.setOnClickListener { finish() }

        // 5. 加载数据
        loadDashboardData()
    }

    private fun loadDashboardData() {
        lifecycleScope.launch {
            // 获取统计数字
            val userCount = db.userDao().getUserCount()
            val outfitCount = db.outfitDao().getCount()

            // 更新 UI
            tvUserCount.text = userCount.toString()
            tvOutfitCount.text = outfitCount.toString()

            // 获取并显示用户列表
            val users = db.userDao().getAllUsers()
            userAdapter.updateData(users)
        }
    }

    // 弹出删除确认框
    private fun showDeleteDialog(user: User) {
        AlertDialog.Builder(this)
            .setTitle("警告")
            .setMessage("确定要删除用户 '${user.username}' 吗？此操作不可恢复。")
            .setPositiveButton("删除") { _, _ ->
                lifecycleScope.launch {
                    db.userDao().deleteUser(user)
                    Toast.makeText(this@AdminActivity, "用户已删除", Toast.LENGTH_SHORT).show()
                    // 删除后刷新数据
                    loadDashboardData()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    // ==========================================
    // 内部类：简单的用户列表适配器 (UserAdapter)
    // ==========================================
    class UserAdapter(
        private val users: MutableList<User>,
        private val onClick: (User) -> Unit
    ) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

        // 刷新数据的方法
        fun updateData(newUsers: List<User>) {
            users.clear()
            users.addAll(newUsers)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            // 直接用代码生成一个简单的 TextView 作为列表项 (省去写 item_user.xml)
            val textView = TextView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    150 // 高度
                )
                textSize = 18f
                gravity = Gravity.CENTER_VERTICAL
                setPadding(40, 0, 40, 0)
                setBackgroundColor(Color.WHITE)
            }
            return UserViewHolder(textView)
        }

        override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
            val user = users[position]
            // 显示用户信息
            (holder.itemView as TextView).text = "👤 ID:${user.id} | 用户名: ${user.username}"

            // 点击事件
            holder.itemView.setOnClickListener { onClick(user) }
        }

        override fun getItemCount(): Int {
            return users.size
        }

        class UserViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view)
    }
}