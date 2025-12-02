package com.example.dresscode

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. 初始化 ViewModel
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        // 2. 找到控件
        val etUsername = findViewById<TextInputEditText>(R.id.et_username)
        val etPassword = findViewById<TextInputEditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val btnRegister = findViewById<Button>(R.id.btn_register)

        // 3. 点击“注册”按钮
        btnRegister.setOnClickListener {
            val name = etUsername.text.toString().trim()
            val pwd = etPassword.text.toString().trim()

            if (name.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(this, "账号密码不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 调用 ViewModel 的注册方法
            viewModel.register(name, pwd)
        }

        // 4. 点击“登录”按钮
        btnLogin.setOnClickListener {
            val name = etUsername.text.toString().trim()
            val pwd = etPassword.text.toString().trim()

            if (name.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(this, "请输入账号密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 调用 ViewModel 的登录方法
            viewModel.login(name, pwd)
        }

        // 5. 观察者：监听注册结果
        viewModel.registerMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // 6. 观察者：监听登录结果
        viewModel.loginResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "登录成功！", Toast.LENGTH_SHORT).show()
                // 跳转到主页 (MainActivity)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // 关闭登录页，这样按返回键不会回到登录页
            } else {
                Toast.makeText(this, "账号或密码错误", Toast.LENGTH_SHORT).show()
            }
        }
    }
}