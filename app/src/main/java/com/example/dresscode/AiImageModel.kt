package com.example.dresscode

data class ImageGenerationRequest(

    val model: String = "black-forest-labs/FLUX.1-schnell",

    val prompt: String,

    val image_size: String = "1024x1024",
    val num_inference_steps: Int = 4
)

data class ImageGenerationResponse(
    val data: List<ImageUrl>
)

data class ImageUrl(
    val url: String
)

data class QwenRequest(
    val model: String = "qwen-vl-max",
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

data class QwenResponse(
    val choices: List<QwenChoice>
)

data class QwenChoice(
    val message: QwenMessageContent
)

data class QwenMessageContent(
    val content: String
)