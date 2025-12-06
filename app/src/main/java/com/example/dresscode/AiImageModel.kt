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