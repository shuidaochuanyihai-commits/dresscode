package com.example.dresscode

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.dresscode.database.User

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var prefs: SharedPreferences
    private lateinit var viewModel: OutfitViewModel

    private lateinit var ivAvatar: ImageView
    private lateinit var tvNickname: TextView // 🔴 改名了：显示昵称
    private lateinit var tvAccount: TextView  // 🔴 新增：显示账号
    private var currentUserData: User? = null

    private val pickAvatarLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && currentUserData != null) {
            val updatedUser = currentUserData!!.copy(avatar = uri.toString())
            viewModel.updateUserInfo(updatedUser)
            Toast.makeText(requireContext(), "头像更新成功！", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        viewModel = ViewModelProvider(this)[OutfitViewModel::class.java]

        ivAvatar = view.findViewById(R.id.iv_avatar)

        // 🔴 绑定两个文本控件
        tvNickname = view.findViewById(R.id.tv_nickname)
        tvAccount = view.findViewById(R.id.tv_account_id)

        val btnEditProfile = view.findViewById<Button>(R.id.btn_edit_profile)

        val rgGender = view.findViewById<RadioGroup>(R.id.rg_gender)
        val rbAll = view.findViewById<RadioButton>(R.id.rb_all)
        val rbFemale = view.findViewById<RadioButton>(R.id.rb_female)
        val rbMale = view.findViewById<RadioButton>(R.id.rb_male)

        val rgDisplayMode = view.findViewById<RadioGroup>(R.id.rg_display_mode)
        val rbShowTitle = view.findViewById<RadioButton>(R.id.rb_show_title)
        val rbShowStyle = view.findViewById<RadioButton>(R.id.rb_show_style)
        val rbShowSeason = view.findViewById<RadioButton>(R.id.rb_show_season)
        val rbShowScene = view.findViewById<RadioButton>(R.id.rb_show_scene)

        val btnLogout = view.findViewById<Button>(R.id.btn_logout)
        val btnFavorites = view.findViewById<Button>(R.id.btn_my_favorites)
        val btnAutoTag = view.findViewById<Button>(R.id.btn_auto_tag)
        val btnAdmin = view.findViewById<TextView>(R.id.btn_admin_panel)

        // 🔴 1. 观察并显示用户信息
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                currentUserData = user

                // 显示昵称 (大字)
                tvNickname.text = user.nickname

                // 显示账号 (小字)
                tvAccount.text = "账号: ${user.username}"

                if (!user.avatar.isNullOrEmpty()) {
                    Glide.with(this).load(user.avatar).circleCrop().into(ivAvatar)
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_launcher_background)
                }
            }
        }

        viewModel.loadCurrentUser()

        // 2. 点击编辑资料
        btnEditProfile.setOnClickListener {
            showEditMenu()
        }

        // --- 其他原有逻辑保持不变 ---
        val savedGender = prefs.getString("gender_pref", "all")
        when (savedGender) {
            "female" -> rbFemale.isChecked = true
            "male" -> rbMale.isChecked = true
            else -> rbAll.isChecked = true
        }
        rgGender.setOnCheckedChangeListener { _, checkedId ->
            val editor = prefs.edit()
            when (checkedId) {
                R.id.rb_female -> editor.putString("gender_pref", "female")
                R.id.rb_male -> editor.putString("gender_pref", "male")
                else -> editor.putString("gender_pref", "all")
            }
            editor.apply()
        }

        val savedMode = prefs.getString("display_mode", "title")
        when (savedMode) {
            "style" -> rbShowStyle.isChecked = true
            "season" -> rbShowSeason.isChecked = true
            "scene" -> rbShowScene.isChecked = true
            else -> rbShowTitle.isChecked = true
        }
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

        btnAutoTag.setOnClickListener {
            Toast.makeText(requireContext(), "开始 AI 分析...", Toast.LENGTH_SHORT).show()
            viewModel.autoTagAllOutfits(requireContext())
        }
        btnFavorites.setOnClickListener {
            val dialog = FavoriteSelectionDialog()
            dialog.show(childFragmentManager, "MyFavorites")
        }
        btnLogout.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        btnAdmin.setOnClickListener {
            startActivity(Intent(requireContext(), AdminActivity::class.java))
        }
    }

    private fun showEditMenu() {
        val options = arrayOf("更换头像", "修改昵称")
        AlertDialog.Builder(requireContext())
            .setTitle("编辑个人资料")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pickAvatarLauncher.launch("image/*")
                    1 -> showEditNameDialog()
                }
            }
            .show()
    }

    // 🔴 修改：这里只改 nickname，不改 username
    private fun showEditNameDialog() {
        val editText = EditText(requireContext())
        editText.hint = "请输入新昵称"
        if (currentUserData != null) {
            // 默认显示当前的昵称
            editText.setText(currentUserData!!.nickname)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("修改昵称")
            .setView(editText)
            .setPositiveButton("保存") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty() && currentUserData != null) {
                    // 🔴 关键：只更新 nickname 字段
                    val updatedUser = currentUserData!!.copy(nickname = newName)
                    viewModel.updateUserInfo(updatedUser)
                    Toast.makeText(requireContext(), "昵称已修改", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
}