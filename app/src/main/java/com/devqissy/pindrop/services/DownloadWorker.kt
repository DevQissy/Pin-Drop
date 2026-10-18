package com.devqissy.pindrop.services

import android.content.Context
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_URL = "download_url"
        const val KEY_TASK_ID = "task_id"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val url = inputData.getString(KEY_URL) ?: return@withContext Result.failure()
        val taskId = inputData.getString(KEY_TASK_ID) ?: return@withContext Result.failure()

        var success = false
        var errorMessage = ""

        try {
            success = true
        } catch (e: Exception) {
            errorMessage = e.message ?: "Unknown error"
        }

        return@withContext if (success) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}