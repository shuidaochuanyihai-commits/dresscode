package com.example.dresscode

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

// 使用 object 关键字，相当于 Java 的静态类 (Static)，可以在任何地方直接调用
object ImageUtils {

    /**
     * 将 Bitmap 图片转成 Base64 字符串
     * @param bitmap 原图
     * @return Base64 字符串 (用于发给 API)
     */
    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // 压缩格式为 JPEG，质量 60% (既保留细节，又减小体积)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        val byteArray = outputStream.toByteArray()
        // NO_WRAP 表示不换行，这是 API 通常要求的格式
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    /**
     * 调整图片大小
     * 防止图片太大传不上去，或者浪费流量
     * @param bitmap 原图
     * @param maxWidth 最大宽度 (默认 1024)
     */
    fun resizeBitmap(bitmap: Bitmap, maxWidth: Int = 1024): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // 如果图片本来就比最大宽度小，直接返回原图
        if (width <= maxWidth) return bitmap

        // 等比例缩放高度
        val newHeight = height * maxWidth / width
        // 创建新的缩放后的图片
        return Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true)
    }
}