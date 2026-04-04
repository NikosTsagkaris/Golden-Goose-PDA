package com.ntvelop.goldengoosepda.feature_orders.data

data class MenuItem(
    val name: String,
    val price: Double,
    val category: String,
    val options: List<MenuOption> = emptyList()
)

data class MenuOption(
    val name: String,
    val price: Double
)

object LocalMenu {
    val items = listOf(
        MenuItem("FREDDO ESPRESSO", 2.50, "COFFEE"),
        MenuItem("FREDDO CAPPUCCINO", 3.00, "COFFEE"),
        MenuItem("ESPRESSO", 1.80, "COFFEE"),
        MenuItem("CAPPUCCINO", 2.20, "COFFEE"),
        MenuItem("GREEK COFFEE", 1.50, "COFFEE"),
        MenuItem("WATER 0.5L", 0.50, "DRINKS"),
        MenuItem("COCA COLA", 1.80, "DRINKS"),
        MenuItem("ORANGE JUICE", 3.50, "JUICE"),
        MenuItem("CHEESE PIE", 2.00, "SNACKS"),
        MenuItem("BOUGATSA", 2.50, "SNACKS")
    )
}
