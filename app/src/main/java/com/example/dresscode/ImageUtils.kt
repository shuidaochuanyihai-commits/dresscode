package com.example.dresscode

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    // 1. Bitmap 转 Base64 (AI 换装用)
    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    // 2. 调整图片大小 (AI 换装用)
    fun resizeBitmap(bitmap: Bitmap, maxWidth: Int = 1024): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxWidth) return bitmap
        val newHeight = height * maxWidth / width
        return Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true)
    }

    // 🔴 3. 新增：将 Uri 复制到内部存储，返回永久路径
    fun copyUriToInternalStorage(context: Context, uri: Uri, fileName: String): String? {
        return try {
            // 打开输入流读取临时文件
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            // 创建私有目录下的新文件
            val file = File(context.filesDir, fileName)
            // 打开输出流写入新文件
            val outputStream = FileOutputStream(file)

            // 复制数据
            inputStream.copyTo(outputStream)

            // 关闭流
            inputStream.close()
            outputStream.close()

            // 返回新文件的绝对路径
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}