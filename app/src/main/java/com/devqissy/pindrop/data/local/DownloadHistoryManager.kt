package com.devqissy.pindrop.data.local

import android.content.Context
import android.content.SharedPreferences
import com.devqissy.pindrop.data.model.DownloadHistoryItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DownloadHistoryManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("download_history", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveHistory(history: List<DownloadHistoryItem>) {
        val json = gson.toJson(history)
        prefs.edit().putString("history", json).apply()
    }

    fun loadHistory(): List<DownloadHistoryItem> {
        val json = prefs.getString("history", null) ?: return emptyList()
        val type = object : TypeToken<List<DownloadHistoryItem>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addHistoryItem(item: DownloadHistoryItem) {
        val currentHistory = loadHistory().toMutableList()
        currentHistory.add(0, item)
        if (currentHistory.size > 100) {
            currentHistory.removeAt(currentHistory.size - 1)
        }
        saveHistory(currentHistory)
    }

    fun clearHistory() {
        prefs.edit().remove("history").apply()
    }
}