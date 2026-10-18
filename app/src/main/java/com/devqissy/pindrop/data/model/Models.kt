package com.devqissy.pindrop.data.model

import android.net.Uri
import androidx.compose.ui.graphics.Color

enum class DownloadStatus { PENDING, DOWNLOADING, COMPLETED, FAILED }

data class DownloadTask(
    val id: String,
    val url: String,
    var fileName: String = "",
    var progress: Float = 0f,
    var status: DownloadStatus = DownloadStatus.PENDING,
    var totalFiles: Int = 1,
    var completedFiles: Int = 0,
    var currentMessage: String = ""
)

data class MediaItem(
    val uri: Uri,
    val name: String,
    val dateAdded: Long = 0,
    val isVideo: Boolean = false
)

data class FloatingOrb(
    val radius: Float,
    val color: Color,
    val centerXFraction: Float,
    val centerYFraction: Float,
    val xAmplitude: Float,
    val yAmplitude: Float,
    val xFrequency: Float,
    val yFrequency: Float
)

data class DownloadHistoryItem(
    val id: String,
    val url: String,
    val status: DownloadStatus,
    val message: String,
    val totalFiles: Int,
    val completedFiles: Int,
    val timestamp: Long = System.currentTimeMillis()
)