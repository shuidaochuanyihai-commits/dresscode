package com.example.dresscode

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.dresscode.database.AppDatabase
import com.example.dresscode.database.Outfit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class OutfitViewModel(application: Application) : AndroidViewModel(application) {

    private val outfitDao = AppDatabase.getDatabase(application).outfitDao()
    private val prefs: SharedPreferences = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    // 🔴 必填：你的通义千问 API Key
    // 去 https://dashscope.console.aliyun.com/apiKey 申请
    private val QWEN_API_KEY = "Bearer sk-153688ffb2e449e795ab871867bce8e6"

    // 列表数据
    val outfitList = MutableLiveData<List<Outfit>>()

    // 🔴 新增：筛选条件 (空字符串代表不筛选)
    val filterStyle = MutableLiveData("")
    val filterSeason = MutableLiveData("")
    val filterScene = MutableLiveData("")

    // 🔴 新增：通义千问专用网络配置 (因为图片分析比较慢，超时设长一点)
    private val qwenClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val qwenRetrofit = Retrofit.Builder()
        .baseUrl("https://dashscope.aliyuncs.com/") // 阿里云灵积地址
        .client(qwenClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val qwenService = qwenRetrofit.create(AiService::class.java)


    init {
        viewModelScope.launch {
            if (outfitDao.getCount() == 0) {
                val dummyData = createDummyData()
                outfitDao.insertAll(dummyData)
            }
            // 默认加载一次
            applyFilters()
        }
    }

    // 🔴 核心方法 1：统一搜索与筛选
    // 这个方法替代了之前的 loadOutfits 和 searchOutfits
    fun applyFilters(keyword: String = "") {
        viewModelScope.launch {
            // 1. 获取所有筛选条件
            val gender = prefs.getString("gender_pref", "all") ?: "all"
            val style = filterStyle.value ?: ""
            val season = filterSeason.value ?: ""
            val scene = filterScene.value ?: ""

            // 2. 去数据库查询
            val data = withContext(Dispatchers.IO) {
                // 调用我们在 DAO 里新写的万能筛选方法
                outfitDao.filterOutfits(keyword, gender, style, season, scene)
            }
            outfitList.value = data
        }
    }

    // 🔴 核心方法 2：AI 自动打标 (核心功能！)
    fun autoTagAllOutfits(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val allOutfits = outfitDao.getAllOutfits()

            for (outfit in allOutfits) {
                // 如果这个衣服已经有标签了(不是默认值)，就跳过，省点 API 额度
                if (outfit.style != "其他" && outfit.season != "四季") continue

                try {
                    // 1. 转图片为 Base64
                    val bitmap = BitmapFactory.decodeResource(context.resources, outfit.imageResId)
                    val base64 = ImageUtils.bitmapToBase64(bitmap)

                    // 2. 构造 Prompt
                    val prompt = """
                        请分析这张图片的服装。
                        严格只返回一个 JSON，包含三个字段：style, season, scene。
                        style 只能选: [休闲, 商务, 街头, 甜美, 复古]。
                        season 只能选: [夏季, 冬季, 春秋]。
                        scene 只能选: [日常, 上班, 约会, 运动, 派对]。
                        不要返回任何 Markdown 格式，只返回纯 JSON 字符串。
                    """.trimIndent()

                    // 3. 发送请求给通义千问
                    val messages = listOf(
                        QwenMessage(
                            role = "user",
                            content = listOf(
                                QwenContent(type = "text", text = prompt),
                                QwenContent(type = "image_url", image_url = QwenImageUrl("data:image/jpeg;base64,$base64"))
                            )
                        )
                    )

                    val response = qwenService.analyzeImage(QWEN_API_KEY, QwenRequest(messages = messages))

                    // 4. 解析结果
                    val jsonContent = response.choices[0].message.content
                    // 简单提取 JSON 里的值
                    val newStyle = extractValue(jsonContent, "style")
                    val newSeason = extractValue(jsonContent, "season")
                    val newScene = extractValue(jsonContent, "scene")

                    // 5. 更新数据库
                    val newOutfit = outfit.copy(style = newStyle, season = newSeason, scene = newScene)
                    outfitDao.updateOutfit(newOutfit)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            // 循环结束后，刷新列表显示最新标签
            withContext(Dispatchers.Main) {
                applyFilters()
            }
        }
    }

    // 辅助：从 JSON 字符串里扣出值
    private fun extractValue(json: String, key: String): String {
        val pattern = "\"$key\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        val match = pattern.find(json)
        // 如果没找到，返回默认值
        return match?.groupValues?.get(1) ?: "其他"
    }

    // 收藏功能保持不变
    fun toggleFavorite(outfit: Outfit) {
        viewModelScope.launch(Dispatchers.IO) {
            outfit.isFavorite = !outfit.isFavorite
            outfitDao.updateOutfit(outfit)
            applyFilters() // 刷新一下
        }
    }

    // 假数据 (带 style, season, scene 初始值)
    private fun createDummyData(): List<Outfit> {
        return listOf(
            Outfit(imageResId = R.drawable.outfit_korean, title = "韩系温柔风", gender = "female", style = "甜美", season = "春秋", scene = "约会"),
            Outfit(imageResId = R.drawable.outfit_summer, title = "夏季清凉穿搭", gender = "female", style = "休闲", season = "夏季", scene = "日常"),
            Outfit(imageResId = R.drawable.outfit_man_suit, title = "男士商务西装", gender = "male", style = "商务", season = "春秋", scene = "上班"),
            Outfit(imageResId = R.drawable.outfit_man_sport, title = "活力运动风", gender = "male", style = "休闲", season = "夏季", scene = "运动"),
            // ... 给其他衣服也随便填点默认值 ...
            Outfit(imageResId = R.drawable.outfit_black, title = "黑色神秘感", gender = "female", style = "街头", season = "冬季", scene = "派对")
        )

    }
}