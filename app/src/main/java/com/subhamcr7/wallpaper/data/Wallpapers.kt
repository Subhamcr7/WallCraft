package com.subhamcr7.wallpaper.data

import android.content.Context
import org.json.JSONObject

data class Wallpaper(
    val id: String,
    val title: String,
    val category: String,
    val accent: Long,
    val source: String
)

object Catalog {

    val devicePhotosCategory = "Device Photos"

    val categories = listOf(
        "All",
        "Minimalist", "Solid", "Abstract", "Fluid", "OLED", "Dark Mode", "Retro", "Anime", "Manga",
        "Album Art", "Supercars", "Bikes", "Cyberpunk", "Sci-Fi", "Beaches", "Oceans", "Space", "Cosmos",
        "Flora", "Macro", "Wildlife", "Pets", "Typography", "Patterns", "Textures", "Seasonal", "Festive", "Art",
        devicePhotosCategory
    )

    fun load(context: Context): List<Wallpaper> {
        val catJson = runCatching {
            context.assets.open("wallpapers_categories.json")
                .bufferedReader()
                .use { it.readText() }
        }.getOrDefault("{}")

        val catObj = runCatching { JSONObject(catJson) }.getOrNull()

        fun catFor(fn: String): String =
            catObj?.optString(fn, "")?.takeIf { it.isNotEmpty() } ?: "Minimalist"

        val assetFiles = runCatching { context.assets.list("wallpapers") ?: emptyArray() }
            .getOrDefault(emptyArray())
            .filter { it.endsWith(".jpg") || it.endsWith(".gif") }

        return assetFiles.map { fn ->
            Wallpaper(
                id = "pin_$fn",
                title = "",
                category = catFor(fn),
                accent = 0xFF3F51B5,
                source = "file:///android_asset/wallpapers/$fn"
            )
        }
    }

    fun forCategory(all: List<Wallpaper>, category: String): List<Wallpaper> =
        if (category == "All") all else all.filter { it.category == category }
}
