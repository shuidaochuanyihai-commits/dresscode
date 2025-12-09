package com.example.dresscode

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        val etUsername = findViewById<TextInputEditText>(R.id.et_username)
        val etPassword = findViewById<TextInputEditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)

        // 🔴 新增：去注册的文字按钮
        val tvGoRegister = findViewById<TextView>(R.id.tv_go_register)

        // 点击“登录”
        btnLogin.setOnClickListener {
            val name = etUsername.text.toString().trim()
            val pwd = etPassword.text.toString().trim()

            if (name.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(this, "请输入账号密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.login(name, pwd)
        }

        // 🔴 修改：点击“立即注册”，跳转到注册页面
        tvGoRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // 监听登录结果
        viewModel.loginResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "登录成功！", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "账号或密码错误", Toast.LENGTH_SHORT).show()
            }
        }
    }
}