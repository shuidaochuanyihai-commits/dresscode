package com.example.dresscode

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.dresscode.database.AppDatabase
import com.example.dresscode.database.Outfit
import com.example.dresscode.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OutfitViewModel(application: Application) : AndroidViewModel(application) {

    private val outfitDao = AppDatabase.getDatabase(application).outfitDao()

    // LiveData 用于通知 Fragment 列表数据发生了变化
    val outfitList = MutableLiveData<List<Outfit>>()

    // 1. 初始化数据 (只在第一次启动时插入假数据)
    init {
        viewModelScope.launch {
            // 检查数据库是否有数据
            if (outfitDao.getCount() == 0) {
                val dummyData = createDummyData()
                outfitDao.insertAll(dummyData)
            }
            loadOutfits()
        }
    }

    // 2. 从数据库加载穿搭数据
    fun loadOutfits() {
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                outfitDao.getAllOutfits()
            }
            outfitList.value = data
        }
    }

    // 3. 收藏/取消收藏逻辑
    fun toggleFavorite(outfit: Outfit) {
        viewModelScope.launch(Dispatchers.IO) {
            // 切换状态
            outfit.isFavorite = !outfit.isFavorite
            // 更新数据库
            outfitDao.updateOutfit(outfit)
            // 重新加载数据 (或者只更新列表中的单个项，这里我们选择重新加载，保证数据一致性)
            loadOutfits()
        }
    }

    // 4. 假数据源 (和上次一样，但现在是为数据库准备的)
    private fun createDummyData(): List<Outfit> {
        return listOf(
            Outfit(imageResId = R.drawable.outfit_korean, title = "韩系温柔风"),
            Outfit(imageResId = R.drawable.outfit_summer, title = "夏季清凉穿搭"),
            Outfit(imageResId = R.drawable.outfit_street, title = "欧美街头风"),
            Outfit(imageResId = R.drawable.outfit_black, title = "黑色神秘感"),
            Outfit(imageResId = R.drawable.outfit_pink, title = "粉色少女心"),
            Outfit(imageResId = R.drawable.outfit_man_suit, title = "男士休闲西装"),
            Outfit(imageResId = R.drawable.outfit_sport, title = "户外运动风"),
            Outfit(imageResId = R.drawable.outfit_retro, title = "复古风格")
        )
    }
}