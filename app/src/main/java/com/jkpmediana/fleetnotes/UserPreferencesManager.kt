package com.jkpmediana.fleetnotes

import android.content.Context

object UserPreferencesManager {
    private const val PREFS_NAME = "fleetnotes_prefs"
    private const val KEY_USER_NAME = "user_name"

    fun saveUserName(context: Context, name: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun getUserName(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_NAME, "Android") ?: "Android"
    }
}
