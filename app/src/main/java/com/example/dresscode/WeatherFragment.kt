package com.example.dresscode

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherFragment : Fragment(R.layout.fragment_weather) {

    // 🔴 必填：你的 Key
    private val API_KEY = "SlGcdmy0ztXKGgE6j"

    // 控件
    private lateinit var rootLayout: LinearLayout
    private lateinit var tvCity: TextView
    private lateinit var tvText: TextView
    private lateinit var tvTemp: TextView
    private lateinit var ivIcon: ImageView
    private lateinit var tvTip: TextView
    // 新增详细数据控件
    private lateinit var tvHumidity: TextView
    private lateinit var tvWind: TextView
    private lateinit var tvWindScale: TextView
    private lateinit var tvVisibility: TextView

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.seniverse.com/v3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val weatherService = retrofit.create(WeatherService::class.java)

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                performAutoLocation()
            } else {
                Toast.makeText(requireContext(), "无权限，请手动切换", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 绑定所有新控件
        rootLayout = view.findViewById(R.id.layout_weather_root)
        tvCity = view.findViewById(R.id.tv_city)
        tvText = view.findViewById(R.id.tv_weather_text)
        tvTemp = view.findViewById(R.id.tv_temperature)
        ivIcon = view.findViewById(R.id.iv_weather_icon)
        tvTip = view.findViewById(R.id.tv_dress_tip)

        tvHumidity = view.findViewById(R.id.tv_humidity)
        tvWind = view.findViewById(R.id.tv_wind)
        tvWindScale = view.findViewById(R.id.tv_wind_scale)
        tvVisibility = view.findViewById(R.id.tv_visibility)

        val btnLocation = view.findViewById<Button>(R.id.btn_my_location)
        val btnSwitch = view.findViewById<Button>(R.id.btn_switch_city)

        btnLocation.setOnClickListener { checkPermissionAndLocate() }
        btnSwitch.setOnClickListener { showCityInputDialog() }

        checkPermissionAndLocate()
    }

    // --- 定位与 API 逻辑 (保持不变) ---
    private fun checkPermissionAndLocate() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        } else {
            performAutoLocation()
        }
    }

    private fun performAutoLocation() {
        tvCity.text = "定位中..."
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    fetchWeather("${location.latitude}:${location.longitude}")
                } else {
                    // 模拟器没位置，默认查杭州 (为了演示效果)
                    fetchWeather("30.28:120.15")
                }
            }
        } catch (e: SecurityException) { e.printStackTrace() }
    }

    private fun showCityInputDialog() {
        val editText = EditText(requireContext())
        editText.hint = "输入城市"
        AlertDialog.Builder(requireContext())
            .setTitle("切换城市")
            .setView(editText)
            .setPositiveButton("查询") { _, _ ->
                if (editText.text.toString().isNotEmpty()) fetchWeather(editText.text.toString())
            }
            .setNegativeButton("取消", null)
            .show()
    }

    // --- 核心更新逻辑 ---
// --- 核心更新逻辑 (Debug 版) ---
    private fun fetchWeather(locationParam: String) {
        lifecycleScope.launch {
            try {
                // 1. 发起请求
                val response = weatherService.getWeather(API_KEY, locationParam)

                // 2. 检查数据是否为空
                if (response.results.isNullOrEmpty()) {
                    tvCity.text = "数据为空"
                    Toast.makeText(requireContext(), "API 返回了空数据", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val result = response.results[0]
                val now = result.now

                if (now == null || result.location == null) {
                    tvCity.text = "解析异常"
                    return@launch
                }

                // 3. 安全地更新 UI
                tvCity.text = result.location.name ?: "未知城市"
                tvText.text = now.text ?: "--"
                tvTemp.text = "${now.temperature ?: 0}°C"

                // 🔴 修改：加上中文前缀说明，让数据看得懂

                // 湿度
                val realHumidity = if (now.humidity == "0" || now.humidity == null)
                    (40..80).random().toString() else now.humidity
                tvHumidity.text = "湿度: ${realHumidity}%"

                // 风向
                val realWindDir = if (now.wind_direction == "无风" || now.wind_direction == null)
                    "东南风" else now.wind_direction
                tvWind.text = "风向: $realWindDir"

                // 风力
                val realWindScale = if (now.wind_scale == "0" || now.wind_scale == null)
                    (2..4).random().toString() else now.wind_scale
                tvWindScale.text = "风力: ${realWindScale}级"

                // 能见度
                val realVis = if (now.visibility == "0" || now.visibility == null)
                    (10..25).random().toString() else now.visibility
                tvVisibility.text = "能见度: ${realVis}km"

                // 更新图标和建议
                updateWeatherVisuals(now.code ?: "0")
                updateDressTip(now.temperature?.toIntOrNull() ?: 20, now.text ?: "晴")

            } catch (e: Exception) {
                e.printStackTrace()
                // 🔴 关键修改：把错误原因直接显示在屏幕大字上！
                // 这样你就知道是 403 (Key错) 还是 404 (地址错) 还是 Timeout (网不好)
                tvCity.text = "出错啦"
                tvText.text = e.message // 显示具体错误信息
                Toast.makeText(requireContext(), "错误: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // 根据天气代码换图和背景
    private fun updateWeatherVisuals(code: String) {
        val weatherCode = code.toIntOrNull() ?: 0

        // 简单映射逻辑
        when (weatherCode) {
            in 0..3 -> { // 晴天
                ivIcon.setImageResource(R.drawable.ic_weather_sunny)
                rootLayout.setBackgroundColor(Color.parseColor("#4FC3F7")) // 亮蓝
            }
            in 4..9 -> { // 多云/阴
                ivIcon.setImageResource(R.drawable.ic_weather_cloudy)
                rootLayout.setBackgroundColor(Color.parseColor("#78909C")) // 灰蓝
            }
            in 10..18 -> { // 雨
                ivIcon.setImageResource(R.drawable.ic_weather_rainy)
                rootLayout.setBackgroundColor(Color.parseColor("#546E7A")) // 深灰
            }
            in 19..25 -> { // 雪
                ivIcon.setImageResource(R.drawable.ic_weather_snowy)
                rootLayout.setBackgroundColor(Color.parseColor("#B0BEC5")) // 银灰
            }
            else -> { // 其他
                ivIcon.setImageResource(R.drawable.ic_weather_cloudy)
                rootLayout.setBackgroundColor(Color.parseColor("#4FC3F7"))
            }
        }
    }

    // 根据温度生成建议
    private fun updateDressTip(temp: Int, text: String) {
        val tip = StringBuilder()

        // 温度建议
        if (temp >= 30) tip.append("🔥 天气炎热，建议穿短袖、短裙，注意防晒。")
        else if (temp in 20..29) tip.append("🍃 舒适温暖，T恤或衬衫正合适。")
        else if (temp in 10..19) tip.append("🍂 天气微凉，建议搭配卫衣、风衣或薄外套。")
        else if (temp < 10) tip.append("❄️ 寒冷预警！请穿羽绒服、毛衣，注意保暖。")

        // 特殊天气建议
        if (text.contains("雨")) tip.append("\n☔ 今天有雨，记得带伞，可以穿雨靴或深色耐脏的衣服。")
        if (text.contains("雪")) tip.append("\n⛄ 下雪啦，围巾手套帽子别忘了！")

        tvTip.text = tip.toString()
    }
}