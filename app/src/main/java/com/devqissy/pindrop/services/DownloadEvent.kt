package com.devqissy.pindrop.services

sealed class DownloadEvent {
    data class OnProgress(
        val taskId: String,
        val message: String,
        val progress: Float,
        val fileIndex: Int,
        val totalFiles: Int
    ) : DownloadEvent()

    data class OnFinished(
        val taskId: String,
        val success: Boolean,
        val message: String,
        val totalFiles: Int,
        val completedFiles: Int
    ) : DownloadEvent()
}