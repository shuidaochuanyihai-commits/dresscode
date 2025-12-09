package com.example.dresscode

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.dresscode.database.Outfit

class UploadActivity : AppCompatActivity() {

    private lateinit var viewModel: OutfitViewModel
    private var selectedUri: Uri? = null

    // 选项数据
    private val styles = listOf("休闲", "商务", "街头", "甜美", "复古", "其他")
    private val seasons = listOf("夏季", "冬季", "春秋", "四季")
    private val scenes = listOf("日常", "上班", "约会", "运动", "派对", "其他")

    // UI 控件
    private lateinit var ivPreview: ImageView
    private lateinit var tvHint: TextView
    private lateinit var etTitle: EditText
    private lateinit var spStyle: Spinner
    private lateinit var spSeason: Spinner
    private lateinit var spScene: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        viewModel = ViewModelProvider(this)[OutfitViewModel::class.java]

        ivPreview = findViewById(R.id.iv_upload_preview)
        tvHint = findViewById(R.id.tv_upload_hint)
        etTitle = findViewById(R.id.et_title)
        spStyle = findViewById(R.id.sp_upload_style)
        spSeason = findViewById(R.id.sp_upload_season)
        spScene = findViewById(R.id.sp_upload_scene)

        val btnAi = findViewById<Button>(R.id.btn_ai_analyze)
        val btnPublish = findViewById<Button>(R.id.btn_publish)

        // 1. 初始化下拉框
        setupSpinner(spStyle, styles)
        setupSpinner(spSeason, seasons)
        setupSpinner(spScene, scenes)

        // 2. 图片选择
        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedUri = uri
                ivPreview.setImageURI(uri)
                tvHint.visibility = View.GONE
            }
        }

        findViewById<View>(R.id.iv_upload_preview).setOnClickListener {
            pickImage.launch("image/*")
        }

        // 3. AI 识别
        btnAi.setOnClickListener {
            if (selectedUri == null) {
                Toast.makeText(this, "请先上传图片", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(this, "AI 正在分析图片...", Toast.LENGTH_LONG).show()
            viewModel.analyzeSingleImage(this, selectedUri!!)
        }

        viewModel.aiAnalysisResult.observe(this) { result ->
            if (result != null) {
                selectSpinnerValue(spStyle, styles, result["style"])
                selectSpinnerValue(spSeason, seasons, result["season"])
                selectSpinnerValue(spScene, scenes, result["scene"])
                Toast.makeText(this, "识别完成！", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. 发布逻辑
        btnPublish.setOnClickListener {
            val title = etTitle.text.toString().trim()

            if (selectedUri == null) {
                Toast.makeText(this, "请先上传图片", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (title.isEmpty()) {
                Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 保存图片到私有目录 (防止权限丢失)
            val fileName = "outfit_${System.currentTimeMillis()}.jpg"
            val savedPath = ImageUtils.copyUriToInternalStorage(this, selectedUri!!, fileName)

            if (savedPath == null) {
                Toast.makeText(this, "图片保存失败", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val style = spStyle.selectedItem.toString()
            val season = spSeason.selectedItem.toString()
            val scene = spScene.selectedItem.toString()

            val newOutfit = Outfit(
                imageResId = 0,
                title = title,
                gender = "all",
                style = style,
                season = season,
                scene = scene,
                imagePath = savedPath, // 存路径
                isFavorite = false
            )

            viewModel.insertOutfit(newOutfit)
            Toast.makeText(this, "发布成功！", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    private fun setupSpinner(spinner: Spinner, data: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, data)
        spinner.adapter = adapter
    }

    private fun selectSpinnerValue(spinner: Spinner, data: List<String>, value: String?) {
        val index = data.indexOf(value)
        if (index >= 0) {
            spinner.setSelection(index)
        }
    }

}