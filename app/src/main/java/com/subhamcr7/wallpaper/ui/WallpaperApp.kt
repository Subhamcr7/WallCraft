package com.subhamcr7.wallpaper.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.subhamcr7.wallpaper.data.Catalog

@Composable
fun WallpaperApp() {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }

    val wallpaper = selectedId?.let { Catalog.byId(it) }

    if (wallpaper == null) {
        HomeScreen(onOpen = { selectedId = it.id })
    } else {
        DetailScreen(wallpaper = wallpaper, onBack = { selectedId = null })
    }
}
