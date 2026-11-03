package com.subhamcr7.wallpaper.data

data class Wallpaper(
    val id: String,
    val title: String,
    val category: String,
    val accent: Long
) {
    val url: String get() = "https://picsum.photos/id/$id/1080/1920"
}

object Catalog {

    val categories = listOf("All", "Nature", "City", "Abstract", "Animals", "Dark", "Space")

    private val nature = listOf(
        Wallpaper("10", "Misty Forest", "Nature", 0xFF2E7D32),
        Wallpaper("1015", "River Valley", "Nature", 0xFF33691E),
        Wallpaper("1016", "Canyon View", "Nature", 0xFF8D6E63),
        Wallpaper("1018", "Alpine Peak", "Nature", 0xFF455A64),
        Wallpaper("1036", "Snowy Range", "Nature", 0xFF90A4AE),
        Wallpaper("1011", "Calm Lake", "Nature", 0xFF0277BD),
        Wallpaper("1005", "Green Hills", "Nature", 0xFF558B2F),
        Wallpaper("1043", "Riverside", "Nature", 0xFF00695C)
    )

    private val city = listOf(
        Wallpaper("104", "City Lights", "City", 0xFF37474F),
        Wallpaper("237", "Street Corner", "City", 0xFF5D4037),
        Wallpaper("238", "Urban Night", "City", 0xFF283593),
        Wallpaper("372", "Skyline", "City", 0xFF263238),
        Wallpaper("429", "Old Town", "City", 0xFF795548),
        Wallpaper("823", "Downtown", "City", 0xFF455A64)
    )

    private val abstract = listOf(
        Wallpaper("122", "Fluid", "Abstract", 0xFF6A1B9A),
        Wallpaper("176", "Lines", "Abstract", 0xFF00838F),
        Wallpaper("180", "Geometry", "Abstract", 0xFFAD1457),
        Wallpaper("201", "Flow", "Abstract", 0xFF1565C0),
        Wallpaper("249", "Prism", "Abstract", 0xFF4527A0),
        Wallpaper("366", "Marbles", "Abstract", 0xFF2E7D32),
        Wallpaper("592", "Hue", "Abstract", 0xFFE65100),
        Wallpaper("674", "Curves", "Abstract", 0xFFC2185B)
    )

    private val animals = listOf(
        Wallpaper("1024", "Feathers", "Animals", 0xFF4E342E),
        Wallpaper("1062", "Panda", "Animals", 0xFF37474F),
        Wallpaper("1074", "Big Cat", "Animals", 0xFFBF360C),
        Wallpaper("1084", "Walrus", "Animals", 0xFF5D4037),
        Wallpaper("1050", "Wildlife", "Animals", 0xFF33691E),
        Wallpaper("433", "Giraffe", "Animals", 0xFFEF6C00)
    )

    private val dark = listOf(
        Wallpaper("1080", "Night Sky", "Dark", 0xFF1A237E),
        Wallpaper("1069", "Midnight", "Dark", 0xFF311B92),
        Wallpaper("973", "Shadow", "Dark", 0xFF212121),
        Wallpaper("870", "Blackout", "Dark", 0xFF000000),
        Wallpaper("964", "Eclipse", "Dark", 0xFF3E2723),
        Wallpaper("895", "Gloom", "Dark", 0xFF263238)
    )

    private val space = listOf(
        Wallpaper("1055", "Deep Field", "Space", 0xFF1A237E),
        Wallpaper("1065", "Orbit", "Space", 0xFF4A148C),
        Wallpaper("903", "Nebula", "Space", 0xFF880E4F),
        Wallpaper("883", "Stars", "Space", 0xFF0D47A1),
        Wallpaper("932", "Galaxy", "Space", 0xFF4E342E),
        Wallpaper("908", "Comet", "Space", 0xFF01579B)
    )

    val all: List<Wallpaper> = nature + city + abstract + animals + dark + space

    private val byIdMap = all.associateBy { it.id }

    fun byId(id: String): Wallpaper? = byIdMap[id]

    fun forCategory(category: String): List<Wallpaper> =
        if (category == "All") all else all.filter { it.category == category }
}
