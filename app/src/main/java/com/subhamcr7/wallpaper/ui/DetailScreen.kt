package com.subhamcr7.wallpaper.ui

import android.Manifest
import android.app.WallpaperManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.subhamcr7.wallpaper.data.Wallpaper
import com.subhamcr7.wallpaper.util.WallpaperUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(wallpaper: Wallpaper, onBack: () -> Unit) {
    val context = LocalContext.current
    var message by remember { mutableStateOf<String?>(null) }

    val storagePermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            WallpaperUtils.downloadWallpaper(context, wallpaper.url, wallpaper.title) { ok ->
                message = if (ok) "Saved to Pictures/Wallpapers" else "Download failed"
            }
        } else {
            message = "Storage permission needed to download"
        }
    }

    fun requestDownload() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            WallpaperUtils.downloadWallpaper(context, wallpaper.url, wallpaper.title) { ok ->
                message = if (ok) "Saved to Pictures/Wallpapers" else "Download failed"
            }
        } else {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                WallpaperUtils.downloadWallpaper(context, wallpaper.url, wallpaper.title) { ok ->
                    message = if (ok) "Saved to Pictures/Wallpapers" else "Download failed"
                }
            } else {
                storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(wallpaper.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = wallpaper.url,
                contentDescription = wallpaper.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )

            message?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Button(onClick = {
                    WallpaperUtils.setWallpaper(context, wallpaper.url, WallpaperManager.FLAG_SYSTEM)
                }) {
                    Text("Set Home")
                }
                Button(onClick = {
                    WallpaperUtils.setWallpaper(context, wallpaper.url, WallpaperManager.FLAG_LOCK)
                }) {
                    Text("Set Lock")
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                FilledTonalButton(onClick = {
                    WallpaperUtils.setWallpaper(
                        context, wallpaper.url,
                        WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                    )
                }) {
                    Text("Set Both")
                }
                OutlinedButton(onClick = { requestDownload() }) {
                    Text("Download")
                }
            }
        }
    }
}
