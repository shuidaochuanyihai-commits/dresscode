package com.example.dresscode

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
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

    // 🔴 必填：请填入你的心知天气 Key
    private val API_KEY = "SlGcdmy0ztXKGgE6j"

    private lateinit var tvCity: TextView
    private lateinit var tvText: TextView
    private lateinit var tvTemp: TextView

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.seniverse.com/v3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val weatherService = retrofit.create(WeatherService::class.java)

    // 权限回调
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                performAutoLocation()
            } else {
                tvCity.text = "无权限"
                Toast.makeText(requireContext(), "请授予权限以使用定位功能", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 绑定控件 (这里必须显式指定 <TextView> 防止报错)
        tvCity = view.findViewById<TextView>(R.id.tv_city)
        tvText = view.findViewById<TextView>(R.id.tv_weather_text)
        tvTemp = view.findViewById<TextView>(R.id.tv_temperature)

        // 2. 绑定新按钮 (注意 ID 是 btn_my_location 和 btn_switch_city)
        val btnLocation = view.findViewById<Button>(R.id.btn_my_location)
        val btnSwitch = view.findViewById<Button>(R.id.btn_switch_city)

        // 点击“定位当前”
        btnLocation.setOnClickListener {
            checkPermissionAndLocate()
        }

        // 点击“切换城市”
        btnSwitch.setOnClickListener {
            showCityInputDialog()
        }

        // 自动触发一次
        checkPermissionAndLocate()
    }

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
                    // 真实定位
                    fetchWeather("${location.latitude}:${location.longitude}")
                    Toast.makeText(requireContext(), "定位成功", Toast.LENGTH_SHORT).show()
                } else {
                    // 模拟器没位置时，兜底查杭州 (为了演示效果)
                    fetchWeather("30.28:120.15")
                    tvCity.text = "模拟器定位(杭州)"
                }
            }
        } catch (e: SecurityException) { e.printStackTrace() }
    }

    private fun showCityInputDialog() {
        val editText = EditText(requireContext())
        editText.hint = "输入城市 (如: Shanghai)"
        AlertDialog.Builder(requireContext())
            .setTitle("手动切换")
            .setView(editText)
            .setPositiveButton("查询") { _, _ ->
                if (editText.text.toString().isNotEmpty()) fetchWeather(editText.text.toString())
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun fetchWeather(locationParam: String) {
        lifecycleScope.launch {
            try {
                val response = weatherService.getWeather(API_KEY, locationParam)
                val result = response.results[0]
                tvCity.text = result.location.name
                tvText.text = result.now.text
                tvTemp.text = "${result.now.temperature}°C"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}