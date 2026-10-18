package com.devqissy.pindrop.network

import android.content.Context
import android.provider.MediaStore
import com.devqissy.pindrop.R
import com.devqissy.pindrop.utils.saveFileToGalleryWithProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

suspend fun downloadFromPinterest(
    context: Context,
    pinUrl: String,
    taskId: String,
    onStatusUpdate: (taskId: String, msg: String, progress: Float, fileIndex: Int, totalFiles: Int) -> Unit,
    onFinished: (taskId: String, success: Boolean, message: String) -> Unit
) {
    val res = context.resources

    try {
        val finalUrl = withContext(Dispatchers.IO) {
            if (pinUrl.contains("pin.it")) {
                try {
                    val connection = URL(pinUrl).openConnection() as HttpURLConnection
                    connection.instanceFollowRedirects = false
                    connection.connectTimeout = 10000
                    connection.readTimeout = 10000
                    connection.connect()
                    val location = connection.getHeaderField("Location")
                    connection.disconnect()
                    location ?: pinUrl
                } catch (e: Exception) {
                    pinUrl
                }
            } else pinUrl
        }

        onStatusUpdate(taskId, res.getString(R.string.download_connecting), 0f, 0, 1)

        val html = withContext(Dispatchers.IO) {
            val connection = URL(finalUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.inputStream.bufferedReader().use { it.readText() }
        }

        var mediaUrls = extractMediaUrls(html)

        if (mediaUrls.isEmpty()) {
            onStatusUpdate(taskId, res.getString(R.string.download_advanced_rendering), 0f, 0, 1)
            val renderedHtml = getHtmlFromWebView(context, finalUrl)
            if (renderedHtml.length > 0) {
                mediaUrls = extractMediaUrls(renderedHtml)
            }
        }

        if (mediaUrls.isEmpty()) {
            onFinished(taskId, false, res.getString(R.string.download_no_media_found))
            return
        }

        val totalFiles = mediaUrls.size
        var successCount = 0

        mediaUrls.forEachIndexed { index, url ->
            val extension = getFileExtension(url)
            val fileName = "Pinterest_${System.currentTimeMillis()}_${index + 1}.$extension"

            val progressMsg = res.getString(R.string.download_file_progress, index + 1, totalFiles)
            onStatusUpdate(taskId, progressMsg, 0f, index, totalFiles)

            val success = withContext(Dispatchers.IO) {
                saveFileToGalleryWithProgress(context, url, fileName) { bytes, total ->
                    val p = if (total > 0) bytes.toFloat() / total.toFloat() else 0f
                    val percentMsg = res.getString(
                        R.string.download_file_progress_percent,
                        index + 1,
                        totalFiles,
                        (p * 100).toInt()
                    )
                    onStatusUpdate(
                        taskId,
                        percentMsg,
                        p,
                        index,
                        totalFiles
                    )
                }
            }
            if (success) {
                successCount++
                context.contentResolver.notifyChange(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null)
                context.contentResolver.notifyChange(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null)
            }
        }

        if (successCount == totalFiles) {
            val msg = res.getString(R.string.download_success_all, totalFiles)
            onFinished(taskId, true, msg)
        } else if (successCount > 0) {
            val msg = res.getString(R.string.download_success_partial, successCount, totalFiles)
            onFinished(taskId, true, msg)
        } else {
            onFinished(taskId, false, res.getString(R.string.download_failed))
        }

    } catch (e: Exception) {
        val errorMsg = e.localizedMessage ?: res.getString(R.string.download_error)
        onFinished(taskId, false, errorMsg)
    }
}

private fun getFileExtension(url: String): String {
    return when {
        url.contains(".mp4", ignoreCase = true) -> "mp4"
        url.contains(".mov", ignoreCase = true) -> "mov"
        url.contains(".png", ignoreCase = true) -> "png"
        url.contains(".webp", ignoreCase = true) -> "webp"
        url.contains(".gif", ignoreCase = true) -> "gif"
        url.contains(".jpg", ignoreCase = true) || url.contains(".jpeg", ignoreCase = true) -> "jpg"
        else -> {
            val lastSegment = url.substringAfterLast("/")
            val dotIndex = lastSegment.lastIndexOf(".")
            if (dotIndex > 0 && dotIndex < lastSegment.length - 1) {
                lastSegment.substringAfterLast(".")
            } else {
                "jpg"
            }
        }
    }
}