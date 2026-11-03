package com.subhamcr7.wallpaper.util

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

object WallpaperUtils {

    private val executor = Executors.newFixedThreadPool(2)

    private const val ASSET_PREFIX = "file:///android_asset/"
    private const val CONTENT_PREFIX = "content://"

    private fun openStream(context: Context, source: String): InputStream {
        return when {
            source.startsWith(ASSET_PREFIX) ->
                context.assets.open(source.removePrefix(ASSET_PREFIX))
            source.startsWith(CONTENT_PREFIX) ->
                context.contentResolver.openInputStream(Uri.parse(source))!!
            else -> {
                val conn = URL(source).openConnection() as HttpURLConnection
                conn.connectTimeout = 15000
                conn.readTimeout = 15000
                conn.requestMethod = "GET"
                conn.inputStream
            }
        }
    }

    fun decodeBitmap(context: Context, source: String): Bitmap? = try {
        openStream(context, source).use { BitmapFactory.decodeStream(it) }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    fun setBitmapWallpaper(
        context: Context,
        bitmap: Bitmap,
        flags: Int,
        onResult: (Boolean, String?) -> Unit
    ) {
        val prepared = prepareBitmap(bitmap)
        val main = Handler(Looper.getMainLooper())
        main.post {
            val wm = WallpaperManager.getInstance(context)
            var anyOk = false
            var lastError: String? = null

            if (flags and WallpaperManager.FLAG_SYSTEM != 0) {
                val r = trySet(wm, prepared, WallpaperManager.FLAG_SYSTEM)
                if (r.first) anyOk = true else lastError = r.second
            }
            if (flags and WallpaperManager.FLAG_LOCK != 0) {
                val r = trySet(wm, prepared, WallpaperManager.FLAG_LOCK)
                if (r.first) anyOk = true else lastError = r.second
            }

            if (anyOk) {
                onResult(true, null)
            } else {
                // Fallback: hand the image to the system wallpaper setter.
                val launched = launchSystemSetter(context, prepared)
                onResult(launched, if (launched) "Opening system setter" else lastError)
            }
        }
    }

    private fun trySet(wm: WallpaperManager, bitmap: Bitmap, flag: Int): Pair<Boolean, String?> {
        return try {
            wm.setBitmap(bitmap, null, true, flag)
            true to null
        } catch (e: Exception) {
            e.printStackTrace()
            false to (e.message ?: e.javaClass.simpleName)
        }
    }

    private fun launchSystemSetter(context: Context, bitmap: Bitmap): Boolean {
        return try {
            val file = File(context.cacheDir, "wallpaper_set.jpg")
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            file.writeBytes(out.toByteArray())
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_ATTACH_DATA).apply {
                setDataAndType(uri, "image/jpeg")
                putExtra("mimeType", "image/jpeg")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun prepareBitmap(src: Bitmap): Bitmap {
        val MAX_DIM = 2160
        var bmp = src
        val maxDim = maxOf(bmp.width, bmp.height)
        if (maxDim > MAX_DIM) {
            val scale = MAX_DIM.toFloat() / maxDim
            bmp = Bitmap.createScaledBitmap(
                bmp,
                (bmp.width * scale).toInt(),
                (bmp.height * scale).toInt(),
                true
            )
        }
        if (bmp.config != Bitmap.Config.ARGB_8888) {
            bmp = bmp.copy(Bitmap.Config.ARGB_8888, false)
        }
        return bmp
    }

    fun downloadWallpaper(context: Context, source: String, title: String, onDone: (Boolean) -> Unit) {
        executor.execute {
            try {
                val bytes = openStream(context, source).use { it.readBytes() }

                val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val displayName = "${title.replace(Regex("[^A-Za-z0-9]+"), "_")}_$stamp.jpg"

                val ok: Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(
                            MediaStore.Images.Media.RELATIVE_PATH,
                            Environment.DIRECTORY_PICTURES + "/Wallpapers"
                        )
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                    val collection =
                        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    val uri = context.contentResolver.insert(collection, values)
                    if (uri != null) {
                        context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                        context.contentResolver.update(
                            uri,
                            ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
                            null, null
                        )
                    }
                    uri != null
                } else {
                    val file = File(context.cacheDir, displayName)
                    file.writeBytes(bytes)
                    val legacy = try {
                        MediaStore.Images.Media.insertImage(
                            context.contentResolver,
                            file.absolutePath,
                            displayName,
                            title
                        )
                    } finally {
                        file.delete()
                    }
                    legacy != null
                }

                onDone(ok)
            } catch (e: Exception) {
                e.printStackTrace()
                onDone(false)
            }
        }
    }
}
