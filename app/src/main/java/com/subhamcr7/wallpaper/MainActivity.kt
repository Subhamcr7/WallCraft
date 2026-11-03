package com.subhamcr7.wallpaper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.subhamcr7.wallpaper.ui.WallpaperApp
import com.subhamcr7.wallpaper.ui.theme.WallpaperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WallpaperTheme {
                WallpaperApp()
            }
        }
    }
}
