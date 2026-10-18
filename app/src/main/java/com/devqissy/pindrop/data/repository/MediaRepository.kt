package com.devqissy.pindrop.data.repository

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.devqissy.pindrop.data.model.MediaItem

fun queryMediaFiles(context: Context, isVideo: Boolean): List<MediaItem> {
    val collection = if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    val projection = arrayOf(
        MediaStore.MediaColumns._ID,
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.DATE_ADDED
    )
    val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
    val folder = if (isVideo) "%Movies/PinDrop%" else "%Pictures/PinDrop%"
    val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC"

    val result = mutableListOf<MediaItem>()

    try {
        context.contentResolver.query(
            collection,
            projection,
            selection,
            arrayOf(folder),
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "unknown"
                val dateAdded = cursor.getLong(dateColumn)
                val contentUri = Uri.withAppendedPath(collection, id.toString())
                result.add(
                    MediaItem(
                        uri = contentUri,
                        name = name,
                        dateAdded = dateAdded,
                        isVideo = isVideo
                    )
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return result
}