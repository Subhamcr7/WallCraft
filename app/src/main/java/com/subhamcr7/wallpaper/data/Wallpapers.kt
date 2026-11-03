package com.subhamcr7.wallpaper.data

import android.content.Context

data class Wallpaper(
    val id: String,
    val title: String,
    val category: String,
    val accent: Long,
    val source: String
)

object Catalog {

    val categories = listOf("All", "My Pins", "Nature", "City", "Abstract", "Animals", "Dark", "Space")

    private fun remote(id: String, title: String, cat: String, accent: Long) =
        Wallpaper(id, title, cat, accent, "https://picsum.photos/id/$id/1080/1920")

    private val nature = listOf(
        remote("10", "Misty Forest", "Nature", 0xFF2E7D32),
        remote("1015", "River Valley", "Nature", 0xFF33691E),
        remote("1016", "Canyon View", "Nature", 0xFF8D6E63),
        remote("1018", "Alpine Peak", "Nature", 0xFF455A64),
        remote("1036", "Snowy Range", "Nature", 0xFF90A4AE),
        remote("1011", "Calm Lake", "Nature", 0xFF0277BD),
        remote("1005", "Green Hills", "Nature", 0xFF558B2F),
        remote("1043", "Riverside", "Nature", 0xFF00695C)
    )

    private val city = listOf(
        remote("104", "City Lights", "City", 0xFF37474F),
        remote("237", "Street Corner", "City", 0xFF5D4037),
        remote("238", "Urban Night", "City", 0xFF283593),
        remote("372", "Skyline", "City", 0xFF263238),
        remote("429", "Old Town", "City", 0xFF795548),
        remote("823", "Downtown", "City", 0xFF455A64)
    )

    private val abstract = listOf(
        remote("122", "Fluid", "Abstract", 0xFF6A1B9A),
        remote("176", "Lines", "Abstract", 0xFF00838F),
        remote("180", "Geometry", "Abstract", 0xFFAD1457),
        remote("201", "Flow", "Abstract", 0xFF1565C0),
        remote("249", "Prism", "Abstract", 0xFF4527A0),
        remote("366", "Marbles", "Abstract", 0xFF2E7D32),
        remote("592", "Hue", "Abstract", 0xFFE65100),
        remote("674", "Curves", "Abstract", 0xFFC2185B)
    )

    private val animals = listOf(
        remote("1024", "Feathers", "Animals", 0xFF4E342E),
        remote("1062", "Panda", "Animals", 0xFF37474F),
        remote("1074", "Big Cat", "Animals", 0xFFBF360C),
        remote("1084", "Walrus", "Animals", 0xFF5D4037),
        remote("1050", "Wildlife", "Animals", 0xFF33691E),
        remote("433", "Giraffe", "Animals", 0xFFEF6C00)
    )

    private val dark = listOf(
        remote("1080", "Night Sky", "Dark", 0xFF1A237E),
        remote("1069", "Midnight", "Dark", 0xFF311B92),
        remote("973", "Shadow", "Dark", 0xFF212121),
        remote("870", "Blackout", "Dark", 0xFF000000),
        remote("964", "Eclipse", "Dark", 0xFF3E2723),
        remote("895", "Gloom", "Dark", 0xFF263238)
    )

    private val space = listOf(
        remote("1055", "Deep Field", "Space", 0xFF1A237E),
        remote("1065", "Orbit", "Space", 0xFF4A148C),
        remote("903", "Nebula", "Space", 0xFF880E4F),
        remote("883", "Stars", "Space", 0xFF0D47A1),
        remote("932", "Galaxy", "Space", 0xFF4E342E),
        remote("908", "Comet", "Space", 0xFF01579B)
    )

    private val remoteAll = nature + city + abstract + animals + dark + space

    fun load(context: Context): List<Wallpaper> {
        val pins = runCatching { context.assets.list("wallpapers") ?: emptyArray() }
            .getOrDefault(emptyArray())
            .filter { it.endsWith(".jpg") || it.endsWith(".gif") }
            .map { fn ->
                val title = fn.substringBeforeLast('.').take(12)
                Wallpaper(
                    id = "pin_$fn",
                    title = title,
                    category = "My Pins",
                    accent = 0xFF3F51B5,
                    source = "file:///android_asset/wallpapers/$fn"
                )
            }
        return pins + remoteAll
    }

    fun forCategory(all: List<Wallpaper>, category: String): List<Wallpaper> =
        if (category == "All") all else all.filter { it.category == category }
}
