package com.example.rbgames_grupo1.ui.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

fun createImageUri(context: Context): Uri? {
    val name = "avatar_${System.currentTimeMillis()}.jpg"
    val cv = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/RBGames")
            put(MediaStore.Images.Media.IS_PENDING, 0)
        }
    }
    return context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv
    )
}