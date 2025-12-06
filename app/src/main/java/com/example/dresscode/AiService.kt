package com.example.dresscode

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AiService {
    // 硅基流动的生图接口 (兼容 OpenAI 格式)
    @POST("v1/images/generations")
    suspend fun generateImage(
        @Header("Authorization") auth: String, // 填 "Bearer sk-xxx"
        @Body request: ImageGenerationRequest
    ): ImageGenerationResponse
}