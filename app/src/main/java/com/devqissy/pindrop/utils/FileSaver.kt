package com.devqissy.pindrop.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

suspend fun saveFileToGalleryWithProgress(
    context: Context,
    fileUrl: String,
    fileName: String,
    onProgress: (bytesRead: Long, totalBytes: Long) -> Unit
): Boolean {
    var inputStream: java.io.InputStream? = null
    var outputStream: java.io.OutputStream? = null
    var pendingUri: android.net.Uri? = null

    try {
        val connection = URL(fileUrl).openConnection() as HttpURLConnection
        connection.connectTimeout = 15000
        connection.readTimeout = 20000
        connection.setRequestProperty("User-Agent", "Mozilla/5.0")
        connection.connect()
        if (connection.responseCode != HttpURLConnection.HTTP_OK) return false

        val totalBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connection.contentLengthLong
        } else {
            connection.contentLength.toLong()
        }

        if (!hasEnoughStorageSpace(totalBytes)) {
            return false
        }

        inputStream = connection.inputStream
        val isVideo = fileName.endsWith(".mp4", ignoreCase = true) ||
                fileName.endsWith(".mov", ignoreCase = true)
        val mimeType = when {
            fileName.endsWith(".mp4", ignoreCase = true) -> "video/mp4"
            fileName.endsWith(".mov", ignoreCase = true) -> "video/quicktime"
            fileName.endsWith(".png", ignoreCase = true) -> "image/png"
            fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
            fileName.endsWith(".gif", ignoreCase = true) -> "image/gif"
            else -> if (isVideo) "video/mp4" else "image/jpeg"
        }

        val resolver = context.contentResolver

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    if (isVideo) "${Environment.DIRECTORY_MOVIES}/PinDrop"
                    else "${Environment.DIRECTORY_PICTURES}/PinDrop"
                )
                // FIX 1: Mark file as pending so MediaStore doesn't expose
                // an incomplete file to other apps (e.g. video players)
                // while we are still writing to it.
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val contentUri = if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            resolver.insert(contentUri, contentValues)
        } else {
            val targetDir = if (isVideo) Environment.DIRECTORY_MOVIES else Environment.DIRECTORY_PICTURES
            val baseDir = Environment.getExternalStoragePublicDirectory(targetDir)
            val pinDropDir = File(baseDir, "PinDrop")
            if (!pinDropDir.exists()) pinDropDir.mkdirs()
            val file = File(pinDropDir, fileName)
            android.net.Uri.fromFile(file)
        }

        if (uri == null) return false

        // Keep a reference so we can clean up on failure (API >= Q)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            pendingUri = uri
        }

        outputStream = resolver.openOutputStream(uri)
            ?: if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) FileOutputStream(File(uri.path!!))
            else null

        if (outputStream == null) {
            cleanupPendingUri(context, pendingUri)
            return false
        }

        val buffer = ByteArray(8192)
        var bytesRead: Int
        var totalBytesRead: Long = 0
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
            totalBytesRead += bytesRead
            onProgress(totalBytesRead, totalBytes)
        }
        outputStream.flush()

        // FIX 1 (cont): Now that writing is complete, clear IS_PENDING
        // so the file becomes visible and playable by media apps.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val updateValues = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
            resolver.update(uri, updateValues, null, null)
        }

        pendingUri = null // success — don't delete on finally
        return true

    } catch (e: Exception) {
        e.printStackTrace()
        // FIX 1 (cont): If writing failed, remove the incomplete entry
        // from MediaStore so there is no orphan record or corrupt file.
        cleanupPendingUri(context, pendingUri)
        return false
    } finally {
        inputStream?.close()
        outputStream?.close()
    }
}

/**
 * Deletes a MediaStore entry that was created with IS_PENDING=1 but
 * never successfully completed (e.g. due to an exception or cancellation).
 * Only relevant on API >= Q.
 */
private fun cleanupPendingUri(context: Context, uri: android.net.Uri?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && uri != null) {
        try {
            context.contentResolver.delete(uri, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun hasEnoughStorageSpace(requiredBytes: Long): Boolean {
    try {
        val stat = StatFs(Environment.getDataDirectory().path)
        val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
        val minRequired = requiredBytes + (50 * 1024 * 1024) // 50MB buffer
        return availableBytes >= minRequired
    } catch (e: Exception) {
        return true
    }
}