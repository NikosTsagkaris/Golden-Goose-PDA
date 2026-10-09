package com.ntvelop.goldengoosepda.feature_tables.data

enum class SpotShape {
    PILL,
    CIRCLE,
    ROUNDED_RECT
}

sealed class MapSpot(
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val shape: SpotShape
) {
    class Table(
        val tableId: Int,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        shape: SpotShape
    ) : MapSpot(x, y, w, h, shape)

    class Label(
        val text: String,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        shape: SpotShape
    ) : MapSpot(x, y, w, h, shape)
}

object MapConfig {
    fun defaultMapSpots(): List<MapSpot> = listOf(
        // Pills (περιοχές)
        MapSpot.Label(text = "ΕΞΩ", x = 100f, y = 80f, w = 180f, h = 100f, shape = SpotShape.PILL),
        MapSpot.Label(text = "ΜΠΡΟΣΤΑ", x = 100f, y = 450f, w = 250f, h = 100f, shape = SpotShape.PILL),
        MapSpot.Label(text = "ΜΕΣΑ", x = 100f, y = 1100f, w = 180f, h = 100f, shape = SpotShape.PILL),
        MapSpot.Label(text = "ΠΙΣΩ", x = 100f, y = 1770f, w = 180f, h = 100f, shape = SpotShape.PILL),
        MapSpot.Label(text = "ΑΥΛΗ", x = 1300f, y = 1090f, w = 180f, h = 100f, shape = SpotShape.PILL),

        // Plain texts (χώροι)
        MapSpot.Label(text = "ΕΙΣΟΔΟΣ", x = 650f, y = 500f, w = 250f, h = 100f, shape = SpotShape.ROUNDED_RECT),
        MapSpot.Label(text = "ΜΠΑΡ", x = 1150f, y = 680f, w = 250f, h = 100f, shape = SpotShape.ROUNDED_RECT),
        MapSpot.Label(text = "ΚΟΥΖΙΝΑ", x = 1000f, y = 2050f, w = 280f, h = 100f, shape = SpotShape.ROUNDED_RECT),
        MapSpot.Label(text = "WC", x = 900f, y = 2400f, w = 180f, h = 100f, shape = SpotShape.ROUNDED_RECT),

        // Tables (outline grid feeling)
        MapSpot.Table(tableId = 4, x = 150f, y = 230f, w = 200f, h = 180f, shape = SpotShape.ROUNDED_RECT),
        MapSpot.Table(tableId = 3, x = 400f, y = 230f, w = 200f, h = 180f, shape = SpotShape.ROUNDED_RECT),
        MapSpot.Table(tableId = 2, x = 900f, y = 230f, w = 200f, h = 180f, shape = SpotShape.ROUNDED_RECT),
        MapSpot.Table(tableId = 1, x = 1150f, y = 230f, w = 200f, h = 180f, shape = SpotShape.ROUNDED_RECT),
        MapSpot.Table(tableId = 6, x = 100f, y = 570f, w = 240f, h = 200f, shape = SpotShape.ROUNDED_RECT),
        MapSpot.Table(tableId = 5, x = 400f, y = 570f, w = 200f, h = 200f, shape = SpotShape.ROUNDED_RECT),
        MapSpot.Table(tableId = 7, x = 100f, y = 880f, w = 250f, h = 200f, shape = SpotShape.ROUNDED_RECT),
        MapSpot.Table(tableId = 8, x = 520f, y = 780f, w = 180f, h = 260f, shape = SpotShape.ROUNDED_RECT),
        MapSpot.Table(tableId = 9, x = 100f, y = 1220f, w = 250f, h = 200f, shape = SpotShape.ROUNDED_RECT),
        MapSpot.Table(tableId = 10, x = 100f, y = 1530f, w = 250f, h = 200f, shape = SpotShape.ROUNDED_RECT),
        MapSpot.Table(tableId = 20, x = 100f, y = 1900f, w = 260f, h = 190f, shape = SpotShape.ROUNDED_RECT),
        MapSpot.Table(tableId = 11, x = 620f, y = 1400f, w = 200f, h = 180f, shape = SpotShape.ROUNDED_RECT),
        MapSpot.Table(tableId = 17, x = 620f, y = 1900f, w = 200f, h = 180f, shape = SpotShape.ROUNDED_RECT),
        MapSpot.Table(tableId = 16, x = 980f, y = 1100f, w = 300f, h = 180f, shape = SpotShape.ROUNDED_RECT),
        MapSpot.Table(tableId = 12, x = 980f, y = 1350f, w = 220f, h = 180f, shape = SpotShape.ROUNDED_RECT),
        MapSpot.Table(tableId = 13, x = 980f, y = 1580f, w = 220f, h = 180f, shape = SpotShape.ROUNDED_RECT),
        MapSpot.Table(tableId = 15, x = 1260f, y = 1350f, w = 180f, h = 180f, shape = SpotShape.CIRCLE),
        MapSpot.Table(tableId = 14, x = 1260f, y = 1580f, w = 180f, h = 180f, shape = SpotShape.ROUNDED_RECT),
        MapSpot.Table(tableId = 19, x = 100f, y = 2150f, w = 260f, h = 190f, shape = SpotShape.ROUNDED_RECT),
        MapSpot.Table(tableId = 18, x = 460f, y = 2150f, w = 200f, h = 200f, shape = SpotShape.ROUNDED_RECT),
    )
}
