package com.example.dresscode

data class ImageGenerationRequest(
    // 🔴 使用 FLUX.1-schnell，这是目前免费里效果最震撼的，比 SDXL 强
    val model: String = "black-forest-labs/FLUX.1-schnell",

    val prompt: String,

    // 🔴 删掉了 image 字段，解决了 403 根源

    val image_size: String = "1024x1024",
    val num_inference_steps: Int = 4 // FLUX 只需要 4 步，速度飞快
)

data class ImageGenerationResponse(
    val data: List<ImageUrl>
)

data class ImageUrl(
    val url: String
)

// 🔴 新增：通义千问多模态请求体
data class QwenRequest(
    val model: String = "qwen-vl-max", // 使用通义千问 VL Max 模型
    val messages: List<QwenMessage>
)

data class QwenMessage(
    val role: String,
    val content: List<QwenContent>
)

data class QwenContent(
    val type: String, // "text" 或 "image_url"
    val text: String? = null,
    val image_url: QwenImageUrl? = null
)

data class QwenImageUrl(
    val url: String // 支持 "data:image/jpeg;base64,..." 格式
)

// 🔴 新增：响应体
data class QwenResponse(
    val choices: List<QwenChoice>
)

data class QwenChoice(
    val message: QwenMessageContent
)

data class QwenMessageContent(
    val content: String // 这里面就是 AI 返回的分析结果 (JSON)
)