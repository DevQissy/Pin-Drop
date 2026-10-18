package com.devqissy.pindrop.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.devqissy.pindrop.R
import com.devqissy.pindrop.data.local.DownloadHistoryManager
import com.devqissy.pindrop.data.model.DownloadHistoryItem
import com.devqissy.pindrop.data.model.DownloadStatus
import com.devqissy.pindrop.data.model.DownloadTask
import com.devqissy.pindrop.services.DownloadEvent
import com.devqissy.pindrop.services.DownloadService
import com.devqissy.pindrop.services.RestartDownloadWorker
import com.devqissy.pindrop.utils.RatingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit

class DownloadViewModel(application: Application) : AndroidViewModel(application) {

    private val _downloads = MutableStateFlow<List<DownloadTask>>(emptyList())
    val downloads: StateFlow<List<DownloadTask>> = _downloads

    private val _history = MutableStateFlow<List<DownloadHistoryItem>>(emptyList())
    val history: StateFlow<List<DownloadHistoryItem>> = _history

    private var downloadService: DownloadService? = null
    private var isBound = false
    private var pendingUrl: String? = null
    private var pendingTaskId: String? = null

    private val workRequestIds = mutableMapOf<String, UUID>()

    private val historyManager = DownloadHistoryManager(application.applicationContext).also {
        _history.value = it.loadHistory()
    }

    private val ratingManager = RatingManager(application.applicationContext)

    private val eventListener: (DownloadEvent) -> Unit = { event ->
        when (event) {
            is DownloadEvent.OnProgress -> {
                updateTask(event.taskId) { t ->
                    t.currentMessage = event.message
                    t.progress = event.progress
                    if (t.status != DownloadStatus.COMPLETED && t.status != DownloadStatus.FAILED) {
                        t.status = DownloadStatus.DOWNLOADING
                    }
                    t.totalFiles = event.totalFiles
                    t.completedFiles = event.fileIndex
                }
            }
            is DownloadEvent.OnFinished -> {
                val finishedTask = _downloads.value.find { it.id == event.taskId }

                if (finishedTask != null) {
                    val historyItem = DownloadHistoryItem(
                        id = finishedTask.id,
                        url = finishedTask.url,
                        status = if (event.success) DownloadStatus.COMPLETED else DownloadStatus.FAILED,
                        message = event.message,
                        totalFiles = finishedTask.totalFiles,
                        completedFiles = finishedTask.completedFiles,
                        timestamp = System.currentTimeMillis()
                    )

                    historyManager.addHistoryItem(historyItem)
                    _history.update { listOf(historyItem) + it }

                    _downloads.update { list ->
                        list.filterNot { it.id == event.taskId }
                    }

                    if (event.success) {
                        ratingManager.recordSuccessfulDownload()
                    }
                }

                workRequestIds[event.taskId]?.let { workId ->
                    WorkManager.getInstance(getApplication()).cancelWorkById(workId)
                    workRequestIds.remove(event.taskId)
                }
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as DownloadService.DownloadBinder
            downloadService = binder.getService()
            isBound = true

            downloadService?.setEventListener(eventListener)

            pendingUrl?.let { url ->
                pendingTaskId?.let { taskId ->
                    downloadService?.addDownload(url, taskId)
                }
                pendingUrl = null
                pendingTaskId = null
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            downloadService = null
            isBound = false
        }
    }

    private fun bindService() {
        if (!isBound) {
            val context = getApplication<Application>().applicationContext
            val intent = Intent(context, DownloadService::class.java)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun unbindService() {
        if (isBound) {
            val context = getApplication<Application>().applicationContext
            context.unbindService(serviceConnection)
            isBound = false
            downloadService = null
        }
    }

    private fun getString(resId: Int): String {
        return getApplication<Application>().applicationContext.getString(resId)
    }

    fun initHistoryManager() {
        // no-op
    }

    fun addDownload(pinUrl: String, navController: NavHostController) {
        if (!isNetworkAvailable()) {
            val noInternetMessage = getString(R.string.vm_no_internet)
            val taskId = System.currentTimeMillis().toString()
            val failedTask = DownloadTask(
                id = taskId,
                url = pinUrl,
                status = DownloadStatus.FAILED,
                currentMessage = noInternetMessage,
                totalFiles = 1,
                completedFiles = 0,
                progress = 0f
            )

            _downloads.update { listOf(failedTask) + it }

            viewModelScope.launch {
                kotlinx.coroutines.delay(3000)
                _downloads.update { list ->
                    list.filterNot { it.id == taskId }
                }
                val historyItem = DownloadHistoryItem(
                    id = failedTask.id,
                    url = failedTask.url,
                    status = DownloadStatus.FAILED,
                    message = noInternetMessage,
                    totalFiles = 1,
                    completedFiles = 0,
                    timestamp = System.currentTimeMillis()
                )
                historyManager.addHistoryItem(historyItem)
                _history.update { listOf(historyItem) + it }
            }
            return
        }

        val inQueueMessage = getString(R.string.vm_in_queue)
        val taskId = System.currentTimeMillis().toString()

        val task = DownloadTask(
            id = taskId,
            url = pinUrl,
            status = DownloadStatus.PENDING,
            currentMessage = inQueueMessage
        )
        _downloads.update { listOf(task) + it }

        // FIX 2: The original 30-second delay was shorter than most downloads,
        // causing RestartDownloadWorker to fire while (or just after) the download
        // was already finished. This resulted in the service being started again
        // and the same file being downloaded a second time (duplicate / corrupt).
        // A 5-minute delay gives enough headroom for typical downloads, and the
        // worker is always cancelled in onFinished / cancelDownload when the task
        // completes normally, so it will never actually run for successful downloads.
        val workRequest = OneTimeWorkRequestBuilder<RestartDownloadWorker>()
            .setInitialDelay(5, TimeUnit.MINUTES)
            .setInputData(
                workDataOf(
                    RestartDownloadWorker.KEY_URL to pinUrl,
                    RestartDownloadWorker.KEY_TASK_ID to taskId
                )
            )
            .build()

        workRequestIds[taskId] = workRequest.id
        WorkManager.getInstance(getApplication()).enqueue(workRequest)

        bindService()

        if (isBound && downloadService != null) {
            downloadService?.addDownload(pinUrl, taskId)
        } else {
            pendingUrl = pinUrl
            pendingTaskId = taskId
        }
    }

    fun cancelDownload(taskId: String) {
        downloadService?.cancelDownloadJob(taskId)
        val cancelledMessage = getString(R.string.vm_cancelled_by_user)

        val cancelledTask = _downloads.value.find { it.id == taskId }
        if (cancelledTask != null) {
            _downloads.update { list ->
                list.filterNot { it.id == taskId }
            }

            val historyItem = DownloadHistoryItem(
                id = cancelledTask.id,
                url = cancelledTask.url,
                status = DownloadStatus.FAILED,
                message = cancelledMessage,
                totalFiles = cancelledTask.totalFiles,
                completedFiles = cancelledTask.completedFiles,
                timestamp = System.currentTimeMillis()
            )
            historyManager.addHistoryItem(historyItem)
            _history.update { listOf(historyItem) + it }
        }

        workRequestIds[taskId]?.let { workId ->
            WorkManager.getInstance(getApplication()).cancelWorkById(workId)
            workRequestIds.remove(taskId)
        }
    }

    fun clearHistory() {
        historyManager.clearHistory()
        _history.value = emptyList()
    }

    fun markUserRated() {
        ratingManager.markUserRated()
    }

    fun markRatingDialogDismissed() {
        ratingManager.markDialogDismissed()
    }

    fun shouldShowRatingDialog(): Boolean {
        return ratingManager.shouldShowRatingDialog()
    }

    private fun updateTask(taskId: String, update: (DownloadTask) -> Unit) {
        _downloads.update { list ->
            list.map {
                if (it.id == taskId) {
                    val modified = it.copy()
                    update(modified)
                    modified
                } else it
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    )
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    override fun onCleared() {
        super.onCleared()
        unbindService()
    }
}