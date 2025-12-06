package com.example.dresscode

import android.app.Application
import android.content.Context // 🔴 补上了这个
import android.content.SharedPreferences // 🔴 还有这个
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.dresscode.database.AppDatabase
import com.example.dresscode.database.Outfit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OutfitViewModel(application: Application) : AndroidViewModel(application) {

    private val outfitDao = AppDatabase.getDatabase(application).outfitDao()

    // 获取 SharedPreferences
    private val prefs: SharedPreferences = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    // LiveData
    val outfitList = MutableLiveData<List<Outfit>>()

    init {
        viewModelScope.launch {
            if (outfitDao.getCount() == 0) {
                val dummyData = createDummyData()
                outfitDao.insertAll(dummyData)
            }
            // 初始化时加载
            loadOutfits()
        }
    }

    // 加载穿搭 (带性别筛选)
    fun loadOutfits() {
        viewModelScope.launch {
            // 读取设置，如果没有设置过默认是 "all"
            val preferredGender = prefs.getString("gender_pref", "all") ?: "all"

            val data = withContext(Dispatchers.IO) {
                if (preferredGender == "all") {
                    outfitDao.getAllOutfits()
                } else {
                    outfitDao.getOutfitsByGender(preferredGender)
                }
            }
            outfitList.value = data
        }
    }

    // 收藏/取消收藏
    fun toggleFavorite(outfit: Outfit) {
        viewModelScope.launch(Dispatchers.IO) {
            outfit.isFavorite = !outfit.isFavorite
            outfitDao.updateOutfit(outfit)
            loadOutfits()
        }
    }

    // 假数据 (带性别)
    private fun createDummyData(): List<Outfit> {
        return listOf(
            // --- 👧 女生专区 (把原本看起来像女装的都划过来) ---
            Outfit(imageResId = R.drawable.outfit_korean, title = "韩系温柔风", gender = "female"),
            Outfit(imageResId = R.drawable.outfit_summer, title = "夏季清凉穿搭", gender = "female"),
            Outfit(imageResId = R.drawable.outfit_pink, title = "粉色少女心", gender = "female"),
            // 原本是中性的，现在强制划为女生
            Outfit(imageResId = R.drawable.outfit_street, title = "欧美街头风", gender = "female"),
            Outfit(imageResId = R.drawable.outfit_black, title = "黑色神秘感", gender = "female"),
            Outfit(imageResId = R.drawable.outfit_retro, title = "复古风格", gender = "female"),

            // --- 👦 男生专区 (之前的西装 + 新加的3张) ---
            Outfit(imageResId = R.drawable.outfit_man_suit, title = "男士商务西装", gender = "male"),
            // 下面这三张是你刚才新加的图片，如果没有加会报错，请确保图片已放入 drawable
            Outfit(imageResId = R.drawable.outfit_man_casual, title = "清爽休闲风", gender = "male"),
            Outfit(imageResId = R.drawable.outfit_man_sport, title = "活力运动风", gender = "male"),
            Outfit(imageResId = R.drawable.outfit_man_jacket, title = "型男夹克", gender = "male")
        )
    }
    // 🔴 新增：搜索方法
    fun searchOutfits(keyword: String) {
        viewModelScope.launch {
            // 1. 获取当前性别偏好 (搜索也要遵守性别筛选)
            val preferredGender = prefs.getString("gender_pref", "all") ?: "all"

            val data = withContext(Dispatchers.IO) {
                if (keyword.isEmpty()) {
                    // 如果没输入字，就恢复正常加载
                    if (preferredGender == "all") outfitDao.getAllOutfits()
                    else outfitDao.getOutfitsByGender(preferredGender)
                } else {
                    // 如果输入了字，就去搜
                    outfitDao.searchOutfits(keyword, preferredGender)
                }
            }
            outfitList.value = data
        }
    }
}