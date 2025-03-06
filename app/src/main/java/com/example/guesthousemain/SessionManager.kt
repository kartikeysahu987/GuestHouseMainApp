package com.example.guesthousemain.util

import android.content.Context

object SessionManager {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_ACCESS_TOKEN = "accessToken"
    private const val KEY_REFRESH_TOKEN = "refreshToken"

    var accessToken: String = ""
    var refreshToken: String = ""

    fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            apply()
        }
    }

    fun loadTokens(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        accessToken = prefs.getString(KEY_ACCESS_TOKEN, "") ?: ""
        refreshToken = prefs.getString(KEY_REFRESH_TOKEN, "") ?: ""
    }

    fun clearTokens(context: Context) {
        accessToken = ""
        refreshToken = ""
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }
}
