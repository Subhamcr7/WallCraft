package com.subhamcr7.wallpaper.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.subhamcr7.wallpaper.data.Catalog
import com.subhamcr7.wallpaper.data.ThemeMode
import com.subhamcr7.wallpaper.ui.theme.WallpaperTheme
import com.subhamcr7.wallpaper.util.ThemePrefs

@Composable
fun WallpaperApp() {
    val context = LocalContext.current
    val wallpapers = remember { Catalog.load(context) }

    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }

    var themeMode by remember { mutableStateOf(ThemePrefs.getMode(context)) }
    fun setTheme(mode: ThemeMode) {
        themeMode = mode
        ThemePrefs.setMode(context, mode)
    }

    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    WallpaperTheme(darkTheme = darkTheme) {
        val wallpaper = selectedId?.let { id -> wallpapers.find { it.id == id } }

        if (wallpaper == null) {
            HomeScreen(
                wallpapers = wallpapers,
                themeMode = themeMode,
                onSetTheme = ::setTheme,
                onOpen = { selectedId = it.id }
            )
        } else {
            DetailScreen(wallpaper = wallpaper, onBack = { selectedId = null })
        }
    }
}
