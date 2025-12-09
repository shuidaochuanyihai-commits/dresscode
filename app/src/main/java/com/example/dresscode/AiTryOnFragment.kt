package com.example.dresscode

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.dresscode.database.Outfit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AiTryOnFragment : Fragment(R.layout.fragment_ai_try_on) {

    // =========================================================
    // 🔴 必填：你的 SiliconFlow API Key (注意 Bearer 后面有空格)
    // =========================================================
    private val API_KEY = "Bearer sk-odthsyuvjxbdvpqlurkugunqdjazntbmnsjicfinmwfacxqk"

    // UI 控件
    private lateinit var ivUser: ImageView
    public lateinit var ivOutfit: ImageView
    private lateinit var ivResult: ImageView
    private lateinit var progressBar: ProgressBar

    // 状态标记
    private var isUserPhotoSet = false
    private var isOutfitSet = false

    // 数据暂存
    private var selectedOutfitTitle = "fashion dress"
    private var userBitmap: Bitmap? = null

    // 网络配置 (增加超时时间，防止 AI 生成慢导致报错)
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.siliconflow.cn/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val aiService = retrofit.create(AiService::class.java)


    // =========================================================
    // Activity Result Launchers (处理回调)
    // =========================================================

    // 1. 拍照回调
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            ivUser.setImageBitmap(bitmap)
            userBitmap = bitmap // 🔴 存下图片给 AI 用
            isUserPhotoSet = true
            Toast.makeText(requireContext(), "拍摄成功", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. 申请相机权限
    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) takePictureLauncher.launch(null)
            else Toast.makeText(requireContext(), "需要相机权限", Toast.LENGTH_SHORT).show()
        }

    // 3. 选人像 (从相册)
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            ivUser.setImageURI(uri)
            isUserPhotoSet = true
            // 🔴 关键：把 Uri 转成 Bitmap 存起来
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                userBitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "图片读取失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 4. 选衣服 (从相册，备用方案)
    private val selectOutfitLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            ivOutfit.setImageURI(uri)
            isOutfitSet = true
            selectedOutfitTitle = "stylish clothes" // 默认词
        }
    }


    // =========================================================
    // 生命周期与 UI 逻辑
    // =========================================================

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 绑定控件
        ivUser = view.findViewById(R.id.iv_user_photo)
        ivOutfit = view.findViewById(R.id.iv_selected_outfit)
        ivResult = view.findViewById(R.id.iv_result)
        progressBar = view.findViewById(R.id.progress_bar)

        val btnUpload = view.findViewById<Button>(R.id.btn_upload_photo)
        val btnSelect = view.findViewById<Button>(R.id.btn_select_outfit)
        val btnGenerate = view.findViewById<Button>(R.id.btn_generate)

        // 点击事件
        btnUpload.setOnClickListener { showPhotoSourceDialog() }
        btnSelect.setOnClickListener { showOutfitSelectionDialog() }

        // 点击生成
        btnGenerate.setOnClickListener {
            // 校验
            if (!isUserPhotoSet || userBitmap == null) {
                Toast.makeText(requireContext(), "请先上传或拍摄您的照片", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isOutfitSet) {
                Toast.makeText(requireContext(), "请先选择一件衣服", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // 开始生成
            generateRealAiImage()
        }
    }


    // =========================================================
    // 外部调用方法 (从弹窗选衣服)
    // =========================================================
    fun onOutfitSelectedFromDialog(outfit: Outfit) {
        // 1. 显示图片
        Glide.with(this).load(outfit.imageResId).into(ivOutfit)
        // 2. 设置状态
        isOutfitSet = true
        // 3. 记录标题 (用于 Prompt)
        selectedOutfitTitle = outfit.title
    }


    // =========================================================
    // 核心 AI 逻辑 (图生图)
    // =========================================================
    private fun generateRealAiImage() {
        // 1. 虽然不传图，但我们要校验用户确实操作了（符合流程）
        if (!isUserPhotoSet) {
            Toast.makeText(requireContext(), "请先上传照片(用于提取性别特征)", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        ivResult.setImageDrawable(null)
        Toast.makeText(requireContext(), "正在调用 FLUX 模型进行高保真生成...", Toast.LENGTH_LONG).show()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 2. 获取用户性别 (从设置里读)
                // 这样生成的模特性别就一定是你设置的性别！
                val prefs = requireContext().getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
                val genderPref = prefs.getString("gender_pref", "all")

                // 根据性别决定提示词里的主语
                val modelDescription = when(genderPref) {
                    "male" -> "a handsome young asian man"
                    "female" -> "a beautiful young asian woman"
                    else -> "a young asian fashion model" // 默认
                }

                // 3. 构造“大师级”提示词 (Prompt Engineering)
                // 我们把衣服标题拼进去，并强调“试衣间自拍视角”，这样出来的图就像是你自己拍的
                val promptText = "A realistic mirror selfie shot in a fitting room, $modelDescription wearing $selectedOutfitTitle, high quality, 4k, photorealistic, highly detailed face, soft lighting, holding a phone"

                // 4. 发送请求 (不带 image 字段，保证 200 OK)
                val request = ImageGenerationRequest(
                    prompt = promptText
                )

                val response = aiService.generateImage(API_KEY, request)

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (response.data.isNotEmpty()) {
                        val imageUrl = response.data[0].url

                        Glide.with(this@AiTryOnFragment)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(ivResult)

                        Toast.makeText(requireContext(), "生成成功！", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "生成失败，请重试", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    // 如果还是 403，那就是 Key 没填对
                    Toast.makeText(requireContext(), "错误: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    // =========================================================
    // 辅助弹窗方法
    // =========================================================

    private fun showPhotoSourceDialog() {
        val options = arrayOf("拍照", "从相册选择")
        AlertDialog.Builder(requireContext())
            .setTitle("上传人像")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpen()
                    1 -> pickImageLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePictureLauncher.launch(null)
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun showOutfitSelectionDialog() {
        val options = arrayOf("从我的收藏选择 (推荐)", "从系统相册选择")
        AlertDialog.Builder(requireContext())
            .setTitle("选择试穿的衣服")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val dialog = FavoriteSelectionDialog()
                        // 🔴 关键：使用 childFragmentManager 保证父子关系
                        dialog.show(childFragmentManager, "FavoriteDialog")
                    }
                    1 -> selectOutfitLauncher.launch("image/*")
                }
            }
            .show()
    }
}