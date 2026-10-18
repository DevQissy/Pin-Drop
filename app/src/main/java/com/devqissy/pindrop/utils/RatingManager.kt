package com.devqissy.pindrop.utils

import android.content.Context
import android.content.SharedPreferences

class RatingManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("rating_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_RATED = "user_has_rated"
        private const val KEY_DOWNLOAD_COUNT = "download_count"
        private const val KEY_RATING_DIALOG_DISMISSED = "rating_dialog_dismissed_count"
        private const val DOWNLOADS_BEFORE_RATING = 3
    }

    fun recordSuccessfulDownload() {
        if (!hasUserRated()) {
            val currentCount = getDownloadCount()
            prefs.edit().putInt(KEY_DOWNLOAD_COUNT, currentCount + 1).apply()
        }
    }

    fun shouldShowRatingDialog(): Boolean {
        if (hasUserRated()) return false

        val downloadCount = getDownloadCount()
        val dismissCount = getDialogDismissCount()

        val requiredDownloads = DOWNLOADS_BEFORE_RATING * (dismissCount + 1)
        return downloadCount >= requiredDownloads
    }

    fun markUserRated() {
        prefs.edit()
            .putBoolean(KEY_USER_RATED, true)
            .apply()
    }

    fun markDialogDismissed() {
        if (!hasUserRated()) {
            val currentDismissCount = getDialogDismissCount()
            prefs.edit()
                .putInt(KEY_RATING_DIALOG_DISMISSED, currentDismissCount + 1)
                .putInt(KEY_DOWNLOAD_COUNT, 0)
                .apply()
        }
    }

    fun resetRatingSystem() {
        prefs.edit().clear().apply()
    }

    private fun hasUserRated(): Boolean = prefs.getBoolean(KEY_USER_RATED, false)
    private fun getDownloadCount(): Int = prefs.getInt(KEY_DOWNLOAD_COUNT, 0)
    private fun getDialogDismissCount(): Int = prefs.getInt(KEY_RATING_DIALOG_DISMISSED, 0)
}