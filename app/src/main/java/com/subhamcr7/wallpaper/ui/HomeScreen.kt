package com.subhamcr7.wallpaper.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.subhamcr7.wallpaper.data.Catalog
import com.subhamcr7.wallpaper.data.ThemeMode
import com.subhamcr7.wallpaper.data.Wallpaper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    wallpapers: List<Wallpaper>,
    themeMode: ThemeMode,
    onSetTheme: (ThemeMode) -> Unit,
    onOpen: (Wallpaper) -> Unit
) {
    var selectedCategory by rememberSaveable { mutableStateOf("All") }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "WallCraft",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 16.dp, bottom = 12.dp)
                    )
                    HorizontalDivider()

                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 4.dp)
                    )
                    Catalog.categories.forEach { cat ->
                        NavigationDrawerItem(
                            selected = cat == selectedCategory,
                            onClick = {
                                selectedCategory = cat
                                scope.launch { drawerState.close() }
                            },
                            label = { Text(cat, style = MaterialTheme.typography.bodyMedium) },
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .height(36.dp)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(top = 6.dp))

                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(start = 24.dp, top = 10.dp, bottom = 4.dp)
                    )
                    ThemeRow("Light", ThemeMode.LIGHT, themeMode, onSetTheme)
                    ThemeRow("Dark", ThemeMode.DARK, themeMode, onSetTheme)
                    ThemeRow("System", ThemeMode.SYSTEM, themeMode, onSetTheme)
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("WallCraft") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { padding ->
            if (selectedCategory == Catalog.devicePhotosCategory) {
                DevicePhotosSection(
                    onOpen = onOpen,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            } else {
                val shown = Catalog.forCategory(wallpapers, selectedCategory)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    items(shown, key = { it.id }) { wp ->
                        WallpaperCard(wp) { onOpen(wp) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeRow(
    label: String,
    mode: ThemeMode,
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(mode) }
            .padding(horizontal = 20.dp, vertical = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(
                    if (current == mode) MaterialTheme.colorScheme.primary else Color.Transparent
                )
                .border(
                    1.5.dp,
                    if (current == mode) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
                    CircleShape
                )
        )
        Spacer(Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun DevicePhotosSection(
    onOpen: (Wallpaper) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val photos = remember { mutableStateOf<List<Wallpaper>>(emptyList()) }
    var denied by remember { mutableStateOf(false) }

    val perms = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    fun loadGallery() {
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME
        )
        val list = mutableListOf<Wallpaper>()
        runCatching {
            context.contentResolver.query(
                uri, projection, null, null,
                "${MediaStore.Images.Media._ID} DESC"
            )?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val name = c.getString(nameCol) ?: "Photo"
                    list += Wallpaper(
                        id = "dev_$id",
                        title = name,
                        category = Catalog.devicePhotosCategory,
                        accent = 0xFF3F51B5,
                        source = Uri.withAppendedPath(uri, id.toString()).toString()
                    )
                }
            }
        }
        photos.value = list
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) {
            denied = false
            loadGallery()
        } else {
            denied = true
        }
    }

    fun ensureAccess() {
        val missing = perms.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) loadGallery() else launcher.launch(perms)
    }

    LaunchedEffect(Unit) { ensureAccess() }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when {
            denied -> Text("Storage permission needed to view device photos.")
            photos.value.isEmpty() && !denied -> CircularProgressIndicator()
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(photos.value, key = { it.id }) { wp ->
                    WallpaperCard(wp) { onOpen(wp) }
                }
            }
        }
    }
}

@Composable
private fun WallpaperCard(wallpaper: Wallpaper, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AsyncImage(
            model = wallpaper.source,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
        )
    }
}
