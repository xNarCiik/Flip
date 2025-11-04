package com.dms.flip.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

suspend fun compressImage(context: Context, uri: Uri): File {
    val bitmap = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input)
        }
    } ?: error("Cannot decode bitmap")

    val compressedFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
    FileOutputStream(compressedFile).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out) // 80% quality
    }
    return compressedFile
}
