package com.devqissy.pindrop.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.devqissy.pindrop.MainActivity
import com.devqissy.pindrop.R
import com.devqissy.pindrop.network.downloadFromPinterest
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class DownloadService : Service() {

    private val binder = DownloadBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var wakeLock: PowerManager.WakeLock

    private data class DownloadRequest(
        val taskId: String,
        val url: String
    )

    private val downloadQueue = mutableListOf<DownloadRequest>()
    private val activeJobs = ConcurrentHashMap<String, Job>()
    private val isProcessingQueue = AtomicBoolean(false)

    private var eventListener: ((DownloadEvent) -> Unit)? = null

    fun setEventListener(listener: (DownloadEvent) -> Unit) {
        this.eventListener = listener
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "pin_drop_download_channel"
        private var isServiceRunning = false

        private val activeDownloadCount = AtomicInteger(0)

        fun hasActiveDownloads(): Boolean = activeDownloadCount.get() > 0

        fun start(context: Context) {
            if (!isServiceRunning) {
                val intent = Intent(context, DownloadService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, DownloadService::class.java)
            context.stopService(intent)
        }

        fun isRunning(): Boolean = isServiceRunning
    }

    inner class DownloadBinder : Binder() {
        fun getService(): DownloadService = this@DownloadService

        fun startDownload(
            taskId: String,
            url: String,
            onEvent: (DownloadEvent) -> Unit
        ) {
            eventListener = onEvent
            addDownload(url, taskId)
        }

        fun cancelDownload(taskId: String) {
            cancelDownloadJob(taskId)
        }

        fun setEventListener(listener: (DownloadEvent) -> Unit) {
            this@DownloadService.setEventListener(listener)
        }

        fun getQueueSize(): Int = synchronized(downloadQueue) { downloadQueue.size }

        fun clearQueue() {
            synchronized(downloadQueue) {
                downloadQueue.clear()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "PinDrop:DownloadLock"
        )

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val readyText = getString(R.string.notification_download_ready)
        startForeground(
            NOTIFICATION_ID,
            createNotification(readyText, -2)
        )

        try {
            if (!wakeLock.isHeld) {
                wakeLock.acquire(30 * 60 * 1000L)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        intent?.let {
            val url = it.getStringExtra("url")
            val taskId = it.getStringExtra("task_id")
            val fromWorker = it.getBooleanExtra("from_worker", false)

            if (fromWorker && url != null && taskId != null) {
                addDownload(url, taskId)
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    fun addDownload(pinUrl: String, taskId: String = System.currentTimeMillis().toString()) {
        activeDownloadCount.incrementAndGet()
        synchronized(downloadQueue) {
            downloadQueue.add(DownloadRequest(taskId, pinUrl))
            updateQueueNotification()
        }
        processQueue()
    }

    private fun processQueue() {
        if (!isProcessingQueue.compareAndSet(false, true)) {
            return
        }

        serviceScope.launch {
            try {
                while (true) {
                    val request: DownloadRequest?

                    synchronized(downloadQueue) {
                        if (downloadQueue.isEmpty()) {
                            isProcessingQueue.set(false)
                            if (activeJobs.isEmpty()) {
                                stopSelf()
                            }
                            return@launch
                        }
                        request = downloadQueue.removeAt(0)
                    }

                    request?.let { req ->
                        updateQueueNotification()

                        val job = serviceScope.launch {
                            performDownload(req.url, req.taskId)
                        }
                        activeJobs[req.taskId] = job
                        job.join()
                        activeJobs.remove(req.taskId)
                        activeDownloadCount.decrementAndGet()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                isProcessingQueue.set(false)
            }
        }
    }

    private suspend fun performDownload(url: String, taskId: String) {
        downloadFromPinterest(
            context = this@DownloadService,
            pinUrl = url,
            taskId = taskId,
            onStatusUpdate = { id, msg, progress, fileIndex, totalFiles ->
                updateNotification(msg, (progress * 100).toInt())
                eventListener?.invoke(
                    DownloadEvent.OnProgress(id, msg, progress, fileIndex, totalFiles)
                )
            },
            onFinished = { id, success, message ->
                val completedText = getString(R.string.notification_download_completed)
                val errorText = getString(R.string.notification_download_error)

                if (success) {
                    updateNotification("$completedText: $message", 100)
                } else {
                    updateNotification("$errorText: $message", 0)
                }

                eventListener?.invoke(
                    DownloadEvent.OnFinished(id, success, message, 0, 0)
                )
            }
        )
    }

    fun cancelDownloadJob(taskId: String) {
        activeJobs[taskId]?.cancel()
        activeJobs.remove(taskId)

        val wasInQueue: Boolean
        synchronized(downloadQueue) {
            wasInQueue = downloadQueue.removeAll { it.taskId == taskId }
        }
        if (wasInQueue) {
            activeDownloadCount.decrementAndGet()
        }

        val cancelledText = getString(R.string.notification_download_cancelled)
        updateNotification(cancelledText, 0)
        updateQueueNotification()

        if (activeJobs.isEmpty() && synchronized(downloadQueue) { downloadQueue.isEmpty() }) {
            stopSelf()
        }
    }

    private fun updateQueueNotification() {
        val queueSize = synchronized(downloadQueue) { downloadQueue.size }
        val activeCount = activeJobs.size

        val message: String
        val progress: Int

        when {
            activeCount > 0 && queueSize > 0 -> {
                message = getString(
                    R.string.notification_active_download,
                    queueSize
                )
                progress = -1
            }

            activeCount > 0 -> {
                message = getString(
                    R.string.notification_download_in_progress
                )
                progress = -1
            }

            queueSize > 0 -> {
                message = getString(
                    R.string.notification_queue_items,
                    queueSize
                )
                progress = -2
            }

            else -> {
                message = getString(
                    R.string.notification_download_ready
                )
                progress = -2
            }
        }

        updateNotification(message, progress)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(message: String, progress: Int): Notification {
        val intent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PendingIntent.FLAG_IMMUTABLE
                    } else {
                        0
                    }
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setOngoing(true)

        when {
            progress in 0..100 -> {
                builder.setProgress(100, progress, false)
            }

            progress == -1 -> {
                builder.setProgress(0, 0, true)
            }

            else -> {
            }
        }

        return builder.build()
    }

    private fun updateNotification(message: String, progress: Int) {
        val notification = createNotification(message, progress)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        activeDownloadCount.set(0)

        activeJobs.values.forEach { it.cancel() }
        activeJobs.clear()

        synchronized(downloadQueue) {
            downloadQueue.clear()
        }

        try {
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        serviceScope.cancel()
    }
}