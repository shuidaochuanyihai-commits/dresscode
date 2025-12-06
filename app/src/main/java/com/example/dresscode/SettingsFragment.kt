package com.example.dresscode

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var prefs: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 获取存储工具 (名字叫 "app_settings")
        prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        val rgGender = view.findViewById<RadioGroup>(R.id.rg_gender)
        val rbAll = view.findViewById<RadioButton>(R.id.rb_all)
        val rbFemale = view.findViewById<RadioButton>(R.id.rb_female)
        val rbMale = view.findViewById<RadioButton>(R.id.rb_male)
        val btnLogout = view.findViewById<Button>(R.id.btn_logout)
        // 🔴 新增：找到收藏按钮
        val btnFavorites = view.findViewById<Button>(R.id.btn_my_favorites)

        // 🔴 新增：点击弹出收藏列表
        btnFavorites.setOnClickListener {
            val dialog = FavoriteSelectionDialog()
            dialog.show(childFragmentManager, "MyFavorites") // ✅ 改成 childFragmentManager
        }


        // 2. 读取之前保存的设置，并显示在界面上
        val savedGender = prefs.getString("gender_pref", "all") // 默认是 "all"
        when (savedGender) {
            "female" -> rbFemale.isChecked = true
            "male" -> rbMale.isChecked = true
            else -> rbAll.isChecked = true
        }

        // 3. 监听选择变化，一旦点了就自动保存
        rgGender.setOnCheckedChangeListener { _, checkedId ->
            val editor = prefs.edit()
            when (checkedId) {
                R.id.rb_female -> editor.putString("gender_pref", "female")
                R.id.rb_male -> editor.putString("gender_pref", "male")
                else -> editor.putString("gender_pref", "all")
            }
            editor.apply() // 提交保存
        }

        // 4. 退出登录逻辑
        btnLogout.setOnClickListener {
            // 跳转回登录页
            val intent = Intent(requireContext(), LoginActivity::class.java)
            // 清空任务栈，防止按返回键又回到主页
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}