package com.subhamcr7.wallpaper.ui

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.subhamcr7.wallpaper.data.Wallpaper
import com.subhamcr7.wallpaper.util.WallpaperUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    wallpaper: Wallpaper,
    flag: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var base by remember { mutableStateOf<Bitmap?>(null) }
    var rotation by remember { mutableIntStateOf(0) }
    var flipH by remember { mutableStateOf(false) }
    var flipV by remember { mutableStateOf(false) }
    var cropEnabled by remember { mutableStateOf(false) }
    var cropRect by remember { mutableStateOf(RectF(0f, 0f, 1f, 1f)) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(wallpaper.source) {
        base = withContext(Dispatchers.IO) { WallpaperUtils.decodeBitmap(context, wallpaper.source) }
    }

    val bmp = base

    fun applyAndSet() {
        if (bmp == null) return
        scope.launch {
            message = "Applying…"
            val finalBmp = withContext(Dispatchers.IO) {
                buildFinalBitmap(bmp, rotation, flipH, flipV, cropRect.takeIf { cropEnabled })
            }
            WallpaperUtils.setBitmapWallpaper(context, finalBmp, flag)
            message = "Wallpaper set"
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Wallpaper") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            if (bmp == null) {
                androidx.compose.material3.CircularProgressIndicator()
                return@Box
            }

            val swap = rotation % 180 != 0
            val dispW = if (swap) bmp.height else bmp.width
            val dispH = if (swap) bmp.width else bmp.height

            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                        .aspectRatio(dispW.toFloat() / dispH)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (cropEnabled) {
                        CropOverlay(
                            rect = cropRect,
                            onCropChange = { cropRect = it },
                            modifier = Modifier.matchParentSize()
                        )
                    }
                }

                message?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    FilledTonalButton(
                        onClick = { rotation = (rotation + 90) % 360 },
                        modifier = Modifier.weight(1f)
                    ) { Text("⟳ 90°") }
                    FilledTonalButton(
                        onClick = { rotation = (rotation - 90 + 360) % 360 },
                        modifier = Modifier.weight(1f)
                    ) { Text("⟲ 90°") }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    FilledTonalButton(
                        onClick = { flipH = !flipH },
                        modifier = Modifier.weight(1f)
                    ) { Text("Flip H") }
                    FilledTonalButton(
                        onClick = { flipV = !flipV },
                        modifier = Modifier.weight(1f)
                    ) { Text("Flip V") }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Button(
                        onClick = {
                            cropEnabled = !cropEnabled
                            if (cropEnabled) cropRect = RectF(0f, 0f, 1f, 1f)
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text(if (cropEnabled) "Done Cropping" else "Crop") }
                    Button(
                        onClick = { applyAndSet() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Set Wallpaper") }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CropOverlay(
    rect: RectF,
    onCropChange: (RectF) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentRect = remember { mutableStateOf(rect) }
    LaunchedEffect(rect) { currentRect.value = rect }

    Box(
        modifier = modifier.pointerInput(Unit) {
            var resizing = false
            detectDragGestures(
                onDragStart = { offset ->
                    val r = currentRect.value
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    val right = r.right * w
                    val bottom = r.bottom * h
                    val handleDist = (offset - Offset(right, bottom)).getDistance()
                    resizing = handleDist < 28.dp.toPx()
                },
                onDrag = { change, dragAmount ->
                    val r = currentRect.value
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    val dx = dragAmount.x / w
                    val dy = dragAmount.y / h
                    val next = if (resizing) {
                        RectF(r.left, r.top, r.right + dx, r.bottom + dy)
                    } else {
                        RectF(r.left + dx, r.top + dy, r.right + dx, r.bottom + dy)
                    }
                    onCropChange(clampCrop(next))
                    change.consume()
                }
            )
        }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val r = Rect(
                offset = Offset(rect.left * w, rect.top * h),
                size = Size(rect.width() * w, rect.height() * h)
            )
            val scrim = Color.Black.copy(alpha = 0.45f)
            drawRect(color = scrim, topLeft = Offset(0f, 0f), size = Size(w, r.top))
            drawRect(color = scrim, topLeft = Offset(0f, r.bottom), size = Size(w, h - r.bottom))
            drawRect(color = scrim, topLeft = Offset(0f, r.top), size = Size(r.left, r.height))
            drawRect(color = scrim, topLeft = Offset(r.right, r.top), size = Size(w - r.right, r.height))

            drawRect(
                color = Color.White,
                style = Stroke(width = 2.dp.toPx()),
                topLeft = r.topLeft,
                size = r.size
            )
            drawCircle(
                color = Color.White,
                radius = 7.dp.toPx(),
                center = Offset(r.right, r.bottom)
            )
        }
    }
}

private fun clampCrop(rect: RectF): RectF {
    val min = 0.03f
    val left = rect.left.coerceIn(0f, 1f - min)
    val top = rect.top.coerceIn(0f, 1f - min)
    val right = rect.right.coerceIn(left + min, 1f)
    val bottom = rect.bottom.coerceIn(top + min, 1f)
    return RectF(left, top, right, bottom)
}

private fun buildFinalBitmap(
    base: Bitmap,
    rotation: Int,
    flipH: Boolean,
    flipV: Boolean,
    crop: RectF?
): Bitmap {
    var bmp = base

    if (flipH || flipV) {
        val m = Matrix()
        m.postScale(if (flipH) -1f else 1f, if (flipV) -1f else 1f)
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
    }

    if (rotation != 0) {
        val m = Matrix()
        m.postRotate(rotation.toFloat())
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
    }

    if (crop != null) {
        val width = bmp.width
        val height = bmp.height
        val left = (crop.left * width).toInt().coerceIn(0, width - 1)
        val top = (crop.top * height).toInt().coerceIn(0, height - 1)
        val right = (crop.right * width).toInt().coerceIn(left + 1, width)
        val bottom = (crop.bottom * height).toInt().coerceIn(top + 1, height)
        bmp = Bitmap.createBitmap(bmp, left, top, right - left, bottom - top)
    }

    return bmp
}
