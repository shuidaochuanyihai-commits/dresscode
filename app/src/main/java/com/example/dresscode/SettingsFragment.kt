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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var prefs: SharedPreferences
    private lateinit var viewModel: OutfitViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        viewModel = ViewModelProvider(this)[OutfitViewModel::class.java]

        // --- 绑定控件 ---
        // 1. 性别设置
        val rgGender = view.findViewById<RadioGroup>(R.id.rg_gender)
        val rbAll = view.findViewById<RadioButton>(R.id.rb_all)
        val rbFemale = view.findViewById<RadioButton>(R.id.rb_female)
        val rbMale = view.findViewById<RadioButton>(R.id.rb_male)

        // 2. 首页展示模式设置 (你要的新功能)
        val rgDisplayMode = view.findViewById<RadioGroup>(R.id.rg_display_mode)
        val rbShowTitle = view.findViewById<RadioButton>(R.id.rb_show_title)
        val rbShowStyle = view.findViewById<RadioButton>(R.id.rb_show_style)
        val rbShowSeason = view.findViewById<RadioButton>(R.id.rb_show_season)
        val rbShowScene = view.findViewById<RadioButton>(R.id.rb_show_scene)

        // 3. 按钮
        val btnLogout = view.findViewById<Button>(R.id.btn_logout)
        val btnFavorites = view.findViewById<Button>(R.id.btn_my_favorites)
        val btnAutoTag = view.findViewById<Button>(R.id.btn_auto_tag)
        val btnAdmin = view.findViewById<TextView>(R.id.btn_admin_panel) // 注意是 TextView

        // --- 逻辑初始化 ---

        // 1. 回显性别设置
        val savedGender = prefs.getString("gender_pref", "all")
        when (savedGender) {
            "female" -> rbFemale.isChecked = true
            "male" -> rbMale.isChecked = true
            else -> rbAll.isChecked = true
        }

        // 监听性别变化并保存
        rgGender.setOnCheckedChangeListener { _, checkedId ->
            val editor = prefs.edit()
            when (checkedId) {
                R.id.rb_female -> editor.putString("gender_pref", "female")
                R.id.rb_male -> editor.putString("gender_pref", "male")
                else -> editor.putString("gender_pref", "all")
            }
            editor.apply()
        }

        // 2. 回显展示模式设置
        val savedMode = prefs.getString("display_mode", "title")
        when (savedMode) {
            "style" -> rbShowStyle.isChecked = true
            "season" -> rbShowSeason.isChecked = true
            "scene" -> rbShowScene.isChecked = true
            else -> rbShowTitle.isChecked = true
        }

        // 监听展示模式变化并保存
        rgDisplayMode.setOnCheckedChangeListener { _, checkedId ->
            val editor = prefs.edit()
            when (checkedId) {
                R.id.rb_show_style -> editor.putString("display_mode", "style")
                R.id.rb_show_season -> editor.putString("display_mode", "season")
                R.id.rb_show_scene -> editor.putString("display_mode", "scene")
                else -> editor.putString("display_mode", "title")
            }
            editor.apply()
        }

        // --- 按钮点击事件 ---

        // AI 自动打标
        btnAutoTag.setOnClickListener {
            Toast.makeText(requireContext(), "开始请求通义千问分析所有图片...", Toast.LENGTH_LONG).show()
            viewModel.autoTagAllOutfits(requireContext())
        }

        // 查看收藏
        btnFavorites.setOnClickListener {
            val dialog = FavoriteSelectionDialog()
            dialog.show(childFragmentManager, "MyFavorites")
        }

        // 退出登录
        btnLogout.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // 进入后台
        btnAdmin.setOnClickListener {
            startActivity(Intent(requireContext(), AdminActivity::class.java))
        }
    }
}