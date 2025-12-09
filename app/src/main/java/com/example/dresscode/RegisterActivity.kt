package com.example.dresscode

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // 复用 LoginViewModel，因为里面已经写好了数据库操作
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        val etName = findViewById<TextInputEditText>(R.id.et_reg_username)
        val etPwd = findViewById<TextInputEditText>(R.id.et_reg_password)
        val btnReg = findViewById<Button>(R.id.btn_do_register)
        val tvBack = findViewById<TextView>(R.id.tv_back_to_login)

        // 点击注册
        btnReg.setOnClickListener {
            val name = etName.text.toString().trim()
            val pwd = etPwd.text.toString().trim()

            if (name.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(this, "请输入完整信息", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // 调用 ViewModel 注册
            viewModel.register(name, pwd)
        }

        // 点击返回
        tvBack.setOnClickListener {
            finish() // 关闭当前页，这就自动回到上一页(登录页)了
        }

        // 监听注册结果
        viewModel.registerMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            // 如果注册成功，自动关闭页面，让用户去登录
            if (msg.contains("成功")) {
                finish()
            }
        }
    }
}