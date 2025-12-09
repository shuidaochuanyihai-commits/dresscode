package com.example.dresscode

data class SeniverseResponse(
    val results: List<WeatherResult>?
)

data class WeatherResult(
    val location: Location?,
    val now: Now?
)

data class Location(
    val name: String?
)

data class Now(
    val text: String? = "未知",
    val temperature: String? = "0",
    val code: String? = "0",

    // 🔴 全部改成可空类型 (?)，防止 API 缺斤短两导致崩溃
    val humidity: String? = "0",
    val wind_direction: String? = "无风",
    val wind_scale: String? = "0",
    val visibility: String? = "0"
)