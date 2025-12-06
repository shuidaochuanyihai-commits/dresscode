package com.example.dresscode

// 1. 最外层响应
data class SeniverseResponse(
    val results: List<WeatherResult>
)

// 2. 结果层
data class WeatherResult(
    val location: Location,
    val now: Now
)

// 3. 城市信息
data class Location(
    val name: String // 城市名，例如 "北京"
)

// 4. 实况天气
data class Now(
    val text: String,        // 天气现象文字，例如 "晴"
    val temperature: String, // 温度，例如 "25"
    val code: String         // 天气图标代码 (后面可以用这个显示图标)
)