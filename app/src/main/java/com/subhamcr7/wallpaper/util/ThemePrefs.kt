package com.subhamcr7.wallpaper.util

import android.content.Context
import com.subhamcr7.wallpaper.data.ThemeMode

object ThemePrefs {

    private const val PREFS = "theme_prefs"
    private const val KEY_MODE = "mode"

    fun getMode(context: Context): ThemeMode {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_MODE, null) ?: return ThemeMode.SYSTEM
        return runCatching { ThemeMode.valueOf(name) }.getOrDefault(ThemeMode.SYSTEM)
    }

    fun setMode(context: Context, mode: ThemeMode) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MODE, mode.name)
            .apply()
    }
}
