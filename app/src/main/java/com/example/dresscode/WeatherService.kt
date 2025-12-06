package com.example.dresscode

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    // 心知天气的实况天气接口: https://api.seniverse.com/v3/weather/now.json
    @GET("weather/now.json")
    suspend fun getWeather(
        @Query("key") apiKey: String,      // 你的 API 私钥
        @Query("location") location: String, // 城市名 或 经纬度(lat:lon)
        @Query("language") lang: String = "zh-Hans", // 语言：简体中文
        @Query("unit") unit: String = "c"  // 单位：摄氏度
    ): SeniverseResponse
}