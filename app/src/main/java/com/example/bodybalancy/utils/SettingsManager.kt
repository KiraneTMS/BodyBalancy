package com.example.bodybalancy.utils


import android.content.Context
import android.content.SharedPreferences

class SettingsManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

    fun isAlarmEnabled(): Boolean {
        return sharedPreferences.getBoolean("AlarmEnabled", true)
    }

    fun setAlarmEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("AlarmEnabled", enabled).apply()
    }
}
