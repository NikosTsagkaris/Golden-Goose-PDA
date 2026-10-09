package com.ntvelop.goldengoosepda.feature_orders.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ntvelop.goldengoosepda.feature_orders.vm.DraftItem
import com.ntvelop.goldengoosepda.feature_orders.vm.OrderUiState
import com.ntvelop.goldengoosepda.feature_orders.vm.OrderViewModel
import com.ntvelop.goldengoosepda.network.CategoryResponse
import com.ntvelop.goldengoosepda.network.OptionResponse
import com.ntvelop.goldengoosepda.network.OrderResponse
import com.ntvelop.goldengoosepda.network.ProductResponse

val gooseGold = Color(0xFFD4AF37)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    orderId: String,
    viewModel: OrderViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.orderState.collectAsState()
    val draftCart by viewModel.draftCart.collectAsState()
    val categories by viewModel.menuCategories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val customizingProduct by viewModel.customizingProduct.collectAsState()
    val dynamicOptions by viewModel.dynamicOptions.collectAsState()
    val (showCategoryDialog, setShowCategoryDialog) = remember { mutableStateOf(false) }
    val (showCustomProductDialog, setShowCustomProductDialog) = remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }


    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state) {
        if (state is OrderUiState.Error) {
            snackbarHostState.showSnackbar(
                message = "Σφάλμα: ${(state as OrderUiState.Error).message}",
                duration = SnackbarDuration.Long
            )
        }
    }

    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val tableNum = (state as? OrderUiState.Success)?.order?.table?.name ?: ""
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$tableNum – Νέα",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Παραγγελία",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    TextButton(onClick = { setShowCategoryDialog(true) }) {
                        Text("Μενού", color = gooseGold, fontWeight = FontWeight.Medium)
                    }
                },
                actions = {
                    TextButton(onClick = onBack) {
                        Text("Πίσω", color = gooseGold, fontWeight = FontWeight.Medium)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            val context = androidx.compose.ui.platform.LocalContext.current

            BottomActionPanel(
                draftItems = draftCart,
                noteText = noteText,
                onNoteChange = { noteText = it },
                onSubmit = {
                    viewModel.submitOrder(
                        orderId = orderId,
                        commonNote = noteText,
                        onSuccess = {
                            android.widget.Toast.makeText(context, "Η παραγγελία καταχωρήθηκε!", android.widget.Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    )
                },
                gooseColor = gooseGold
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (categories.isNotEmpty()) {
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5))
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = cat.id == selectedCategory?.id
                        Surface(
                            onClick = { viewModel.selectCategory(cat) },
                            shape = CircleShape,
                            color = if (isSelected) gooseGold else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) gooseGold else Color.LightGray
                            ),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            ) {
                                Text(
                                    text = cat.name ?: "",
                                    color = if (isSelected) Color.White else Color.Black,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedCategory?.name == "CUSTOM") {
                    item {
                        Button(
                            onClick = { setShowCustomProductDialog(true) },
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = gooseGold)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Προσθήκη Custom Προϊόντος", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(selectedCategory?.products ?: emptyList()) { product ->
                    ProductRow(
                        product = product,
                        onAdd = { viewModel.startCustomizing(product) }
                    )
                }
                item { Spacer(Modifier.height(150.dp)) }
            }

            CategoryDialog(
                show = showCategoryDialog,
                categories = categories,
                selectedCategory = selectedCategory,
                onSelect = {
                    viewModel.selectCategory(it)
                    setShowCategoryDialog(false)
                },
                onDismiss = { setShowCategoryDialog(false) }
            )

            ProductOptionsDialog(
                product = customizingProduct,
                categoryName = selectedCategory?.name,
                serverOptions = dynamicOptions,
                onDismiss = { viewModel.stopCustomizing() },
                onConfirm = { draftItem -> 
                    viewModel.addToDraft(draftItem)
                },
                gooseGold = gooseGold
            )

            CustomProductDialog(
                show = showCustomProductDialog,
                onDismiss = { setShowCustomProductDialog(false) },
                onConfirm = { name, price ->
                    viewModel.addCustomItem(name, price)
                    setShowCustomProductDialog(false)
                },
                gooseGold = gooseGold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductRow(
    product: ProductResponse,
    onAdd: (ProductResponse) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onAdd(product) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name ?: "Unknown", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("${product.price}€", color = gooseGold, fontSize = 18.sp)
            }

            IconButton(
                onClick = { onAdd(product) },
                modifier = Modifier
                    .size(56.dp)
                    .background(gooseGold.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add",
                    tint = gooseGold,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun BottomActionPanel(
    draftItems: List<DraftItem>,
    noteText: String,
    onNoteChange: (String) -> Unit,
    onSubmit: () -> Unit,
    gooseColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White)
    ) {
        OutlinedTextField(
            value = noteText,
            onValueChange = onNoteChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Σχόλιο παραγγελίας...") },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
        )

        Spacer(Modifier.height(8.dp))

        if (draftItems.isNotEmpty()) {
            val total = draftItems.sumOf { (it.unitPrice * it.quantity) + it.optionsPrice }
            Text(
                "${draftItems.size} είδη στο καλάθι – Σύνολο: €${String.format("%.2f", total)}",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = gooseColor
            )
        } else {
            Text(
                "Επιλέξτε προϊόντα από το μενού παραπάνω.",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 13.sp,
                color = Color.Gray
            )
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onSubmit,
            enabled = draftItems.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = gooseColor)
        ) {
            Text("Ολοκλήρωση Παραγγελίας", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CategoryDialog(
    show: Boolean,
    categories: List<CategoryResponse>,
    selectedCategory: CategoryResponse?,
    onSelect: (CategoryResponse) -> Unit,
    onDismiss: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Επιλογή Κατηγορίας", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(categories) { category ->
                        Surface(
                            onClick = { onSelect(category) },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (category.id == selectedCategory?.id)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = category.name ?: "Unknown",
                                modifier = Modifier.padding(16.dp),
                                fontSize = 18.sp,
                                fontWeight = if (category.id == selectedCategory?.id) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("Άκυρο") }
            }
        )
    }
}

fun parseSlashOptions(productName: String?): List<String> {
    if (productName.isNullOrBlank() || !productName.contains("/")) return emptyList()

    val rawSegments = productName.split("/")
    if (rawSegments.size < 2) return emptyList()

    val choices = mutableListOf<String>()

    for (i in rawSegments.indices) {
        val seg = rawSegments[i]

        val cleaned = if (i == 0) {
            val beforePart = if (seg.contains(":")) seg.substringAfterLast(":") else seg
            val words = beforePart.trim().split(Regex("""\s+""")).filter { it.isNotBlank() }
            val lastWord = words.lastOrNull() ?: ""
            val cleanedLast = if (lastWord.contains(",")) lastWord.substringAfterLast(",").trim() else lastWord
            
            if (words.size >= 2 && words[words.size - 2].equals("COCA", ignoreCase = true)) {
                "${words[words.size - 2]} $cleanedLast"
            } else {
                cleanedLast
            }
        } else if (i == rawSegments.lastIndex) {
            val afterPart = seg.split(Regex("""[,(+]""")).firstOrNull() ?: seg
            val words = afterPart.trim().split(Regex("""\s+""")).filter { it.isNotBlank() }
            val filtered = words.filterNot { w ->
                w.lowercase().matches(Regex("""\d+ml|\d+l|ποτήρι|καραφάκι|μεζέ|meze|\+"""))
            }
            if (filtered.size >= 2 && (filtered[0].equals("RED", ignoreCase = true) || filtered[0].equals("ZERO", ignoreCase = true))) {
                "${filtered[0]} ${filtered[1]}"
            } else {
                filtered.firstOrNull() ?: ""
            }
        } else {
            val midPart = seg.split(Regex("""[,(+]""")).firstOrNull() ?: seg
            midPart.trim()
        }

        val finalWord = cleaned.replace(Regex("""[():,]"""), "").trim()
        if (finalWord.isNotBlank() && finalWord.length < 30) {
            choices.add(finalWord)
        }
    }

    if (choices.size >= 2) {
        return choices.distinct()
    }
    return emptyList()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductOptionsDialog(
    product: ProductResponse?,
    categoryName: String?,
    serverOptions: List<OptionResponse> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (DraftItem) -> Unit,
    gooseGold: Color
) {
    if (product == null) return

    var quantity by remember { mutableStateOf(1) }
    var isPlastic by remember { mutableStateOf(false) }
    var selectedSugar by remember { mutableStateOf("M") }
    var selectedMilk by remember { mutableStateOf("Όχι") }
    var selectedTemp by remember { mutableStateOf("Κρύα") }
    var selectedFlavor by remember { mutableStateOf("Ροδάκινο") }
    
    val slashOptions = remember(product.name) { parseSlashOptions(product.name) }
    var selectedSlashOption by remember(product.id) { mutableStateOf(slashOptions.firstOrNull()) }

    val isCoffee = categoryName?.contains("ΚΑΦΕ", ignoreCase = true) == true ||
            categoryName?.contains("ΚΑΦΕΔΕΣ", ignoreCase = true) == true ||
            product.optionGroup == 1 ||
            product.name?.contains("Espresso", ignoreCase = true) == true ||
            product.name?.contains("Cappuccino", ignoreCase = true) == true ||
            product.name?.contains("Freddo", ignoreCase = true) == true ||
            product.name?.contains("Nes", ignoreCase = true) == true ||
            product.name?.contains("Φραπέ", ignoreCase = true) == true ||
            product.name?.contains("Ελληνικός", ignoreCase = true) == true ||
            product.name?.contains("Latte", ignoreCase = true) == true ||
            product.name?.contains("Americano", ignoreCase = true) == true ||
            product.name?.contains("Φίλτρου", ignoreCase = true) == true ||
            product.name?.contains("Filter", ignoreCase = true) == true ||
            product.name?.contains("Καφέ", ignoreCase = true) == true ||
            product.name?.contains("Καφές", ignoreCase = true) == true

    val isPlainIceCream = (product.name?.equals("ΠΑΓΩΤΟ", ignoreCase = true) == true ||
            product.name?.equals("ΠΑΓΩΤΟ ΜΠΑΛΑ", ignoreCase = true) == true ||
            product.name?.contains("ΠΑΓΩΤΟ", ignoreCase = true) == true ||
            product.name?.contains("Παγωτό", ignoreCase = true) == true) &&
            product.name?.contains("Βάφλα", ignoreCase = true) != true &&
            product.name?.contains("Waffle", ignoreCase = true) != true

    val isPlainMilkshake = (product.name?.equals("MILKSHAKE", ignoreCase = true) == true ||
            product.name?.contains("Milkshake", ignoreCase = true) == true) &&
            product.name?.contains("GIAGIAMAS", ignoreCase = true) != true &&
            product.name?.contains("BARISTA PRO", ignoreCase = true) != true

    val isIceCreamOrMilkshake = isPlainIceCream || isPlainMilkshake

    val isChocolate = !isCoffee && !isIceCreamOrMilkshake && (
            categoryName?.contains("ΡΟΦΗΜΑΤΑ", ignoreCase = true) == true ||
            categoryName?.contains("ΣΟΚΟΛΑΤΑ", ignoreCase = true) == true ||
            product.name?.contains("Σοκολάτα", ignoreCase = true) == true ||
            product.name?.contains("Chocolate", ignoreCase = true) == true ||
            product.name?.contains("Mochaccino", ignoreCase = true) == true ||
            product.name?.contains("Κακάο", ignoreCase = true) == true
    )
    val isTea = product.name?.contains("Lipton", ignoreCase = true) == true ||
            product.name?.contains("Τσάι", ignoreCase = true) == true ||
            product.name?.contains("ΤΣΑΙ", ignoreCase = true) == true ||
            product.name?.contains("Tea", ignoreCase = true) == true ||
            product.optionGroup == 12 ||
            categoryName?.contains("HOTLY", ignoreCase = true) == true ||
            categoryName?.contains("Teabox", ignoreCase = true) == true

    val isFood = !isIceCreamOrMilkshake && (categoryName?.contains("ΦΑΓΗΤΟ", ignoreCase = true) == true ||
            categoryName?.contains("SNACKS", ignoreCase = true) == true ||
            categoryName?.contains("BRUNCH", ignoreCase = true) == true ||
            categoryName?.contains("PANCAKES", ignoreCase = true) == true ||
            categoryName?.contains("CLUB", ignoreCase = true) == true ||
            categoryName?.contains("BURGER", ignoreCase = true) == true ||
            categoryName?.contains("PIZZA", ignoreCase = true) == true ||
            categoryName?.contains("ΠΙΤΣΕΣ", ignoreCase = true) == true ||
            categoryName?.contains("ΣΑΛΑΤΕΣ", ignoreCase = true) == true ||
            categoryName?.contains("ΠΟΙΚΙΛΙΕΣ", ignoreCase = true) == true ||
            categoryName?.contains("ΓΛΥΚΑ", ignoreCase = true) == true ||
            categoryName?.contains("ΒΑΦΛΕΣ", ignoreCase = true) == true ||
            product.name?.contains("Κρέπα", ignoreCase = true) == true ||
            product.name?.contains("Βάφλα", ignoreCase = true) == true ||
            product.name?.contains("Pancakes", ignoreCase = true) == true ||
            product.name?.contains("Club", ignoreCase = true) == true ||
            product.name?.contains("Burger", ignoreCase = true) == true ||
            product.name?.contains("Σάντουιτς", ignoreCase = true) == true ||
            product.name?.contains("Τοστ", ignoreCase = true) == true ||
            product.name?.contains("Πίτσα", ignoreCase = true) == true ||
            product.name?.contains("Pizza", ignoreCase = true) == true ||
            product.name?.contains("Σαλάτα", ignoreCase = true) == true ||
            product.name?.contains("Ομελέτα", ignoreCase = true) == true)

    val isMojito = product.name?.contains("Mojito", ignoreCase = true) == true
    val mojitoFlavors = remember { listOf("Strawberry", "Mango", "Forest Fruits") }
    var selectedMojitoFlavor by remember(product.id) { mutableStateOf(mojitoFlavors.firstOrNull()) }

    val isGiagiamasSimple = (categoryName?.contains("GIAGIAMAS", ignoreCase = true) == true || categoryName?.contains("ΓΙΑΓΙΑΜΑΣ", ignoreCase = true) == true) &&
            categoryName?.contains("Milkshake", ignoreCase = true) != true
    val giagiamasMixers = remember { listOf("Σόδα", "Νερό") }
    var selectedGiagiamasMixer by remember(product.id) { mutableStateOf(giagiamasMixers.firstOrNull()) }

    val isCorona = product.name?.contains("Corona", ignoreCase = true) == true ||
            product.name?.contains("Κορώνα", ignoreCase = true) == true ||
            product.optionGroup == 7
    val coronaExtras = remember { listOf("Αλάτι", "Λεμόνι") }
    var selectedCoronaExtra by remember(product.id) { mutableStateOf<String?>(null) }

    val isMilkshakeBaristaPro = product.name?.contains("BARISTA PRO", ignoreCase = true) == true || product.optionGroup == 8
    val baristaProFlavors = remember { listOf("Love it", "Bueno", "Red Velvet") }
    var selectedBaristaProFlavor by remember(product.id) { mutableStateOf(baristaProFlavors.firstOrNull()) }

    val isAmita = product.name?.contains("Amita", ignoreCase = true) == true || product.optionGroup == 9
    val amitaFlavors = remember { listOf("Ροδάκινο", "Πορτοκάλι", "Βύσσινο", "Ανανάς", "4 Φρούτα", "Λεμόνι") }
    var selectedAmitaFlavor by remember(product.id) { mutableStateOf(amitaFlavors.firstOrNull()) }

    val isSchweppes = product.name?.contains("Schweppes", ignoreCase = true) == true || product.optionGroup == 10
    val schweppesFlavors = remember { listOf("Orange", "Pink Grapefruit", "Lemonade") }
    var selectedSchweppesFlavor by remember(product.id) { mutableStateOf(schweppesFlavors.firstOrNull()) }

    val isWineGlass = product.name?.contains("180ml", ignoreCase = true) == true || product.optionGroup == 13
    val wineFlavors = remember { listOf("Λευκό Ξηρό", "Λευκό Ημίγλυκο", "Κόκκινο Ξηρό", "Κόκκινο Ημίγλυκο") }
    var selectedWineFlavor by remember(product.id) { mutableStateOf(wineFlavors.firstOrNull()) }

    val isDrink = isCoffee || isChocolate || isTea || isPlainMilkshake || isMilkshakeBaristaPro ||
            categoryName?.contains("ΚΑΦΕ", ignoreCase = true) == true ||
            categoryName?.contains("ΡΟΦΗΜΑΤΑ", ignoreCase = true) == true ||
            categoryName?.contains("GIAGIAMAS", ignoreCase = true) == true ||
            categoryName?.contains("ΓΙΑΓΙΑΜΑΣ", ignoreCase = true) == true

    val isPancake = product.name?.contains("Pancake", ignoreCase = true) == true
    val isSweetPancake = isPancake && (
        product.name?.contains("Πραλίνα", ignoreCase = true) == true ||
        product.name?.contains("Μπισκότο", ignoreCase = true) == true ||
        product.name?.contains("Σαντιγί", ignoreCase = true) == true ||
        product.name?.contains("Μέλι", ignoreCase = true) == true ||
        product.name?.contains("Σφενδάμου", ignoreCase = true) == true ||
        product.name?.contains("Φρούτα", ignoreCase = true) == true ||
        product.name?.contains("Σοκολάτα", ignoreCase = true) == true
    )
    val isSweetFood = isSweetPancake || product.name?.contains("Βάφλα", ignoreCase = true) == true ||
            categoryName?.contains("ΒΑΦΛΕΣ", ignoreCase = true) == true ||
            (categoryName?.contains("ΓΛΥΚΑ", ignoreCase = true) == true && !isPancake)

    val effectiveOptionGroup = when {
        isFood -> 0
        isCoffee -> 1
        isChocolate -> 2
        product.optionGroup in 1..2 -> product.optionGroup
        else -> 0
    }

    val isCustomPillsProduct = isCorona || isMojito || isGiagiamasSimple || isMilkshakeBaristaPro || isAmita || isSchweppes || isWineGlass || slashOptions.isNotEmpty()

fun formatPancakeExclusion(ingredient: String): String {
    val clean = ingredient.trim()
    val lower = clean.lowercase()
    return when {
        lower == "μαρούλη" || lower == "μαρούλι" -> "Χωρίς μαρούλι"
        lower.contains("bbq") -> "Χωρίς BBQ sauce"
        lower.contains("caesar") -> "Χωρίς Caesar sauce"
        lower.contains("τηγανητό αυγό") || lower.contains("τηγανητο αυγο") -> "Χωρίς αυγό"
        lower == "αυγό" || lower == "αυγο" -> "Χωρίς αυγό"
        lower.contains("σφενδάμου") || lower.contains("σφενδαμου") -> "Χωρίς σιρόπι σφενδάμου"
        lower == "μέλι" || lower == "μελι" -> "Χωρίς μέλι"
        lower == "πραλίνα" || lower == "πραλινα" -> "Χωρίς πραλίνα"
        lower == "μπισκότο" || lower == "μπισκοτο" -> "Χωρίς μπισκότο"
        lower == "σαντιγί" || lower == "σαντιγι" -> "Χωρίς σαντιγί"
        lower == "φρούτα" || lower == "φρουτα" -> "Χωρίς φρούτα"
        lower == "τυρί" || lower == "τυρι" -> "Χωρίς τυρί"
        lower == "ζαμπόν" || lower == "ζαμπον" -> "Χωρίς ζαμπόν"
        lower == "γαλοπούλα" || lower == "γαλοπουλα" -> "Χωρίς γαλοπούλα"
        lower == "ντομάτα" || lower == "ντοματα" -> "Χωρίς ντομάτα"
        lower == "μπέικον" || lower == "μπεικον" -> "Χωρίς μπέικον"
        lower == "κοτόπουλο" || lower == "κοτοπουλο" -> "Χωρίς κοτόπουλο"
        lower == "παρμεζάνα" || lower == "παρμεζανα" -> "Χωρίς παρμεζάνα"
        else -> "Χωρίς ${clean.lowercase()}"
    }
}

fun getPancakeExclusions(productName: String?): List<String> {
    if (productName == null || !productName.contains(":")) {
        return emptyList()
    }
    val ingredientsPart = productName.substringAfter(":").trim()
    val rawItems = ingredientsPart.split(",").map { it.trim() }
    val result = mutableListOf<String>()
    for (raw in rawItems) {
        if (raw.contains("/")) {
            val subItems = raw.split("/").map { it.trim() }
            for (sub in subItems) {
                result.add(formatPancakeExclusion(sub))
            }
        } else {
            result.add(formatPancakeExclusion(raw))
        }
    }
    return result.distinct()
}

fun getFoodExclusions(productName: String?): List<Pair<String, Double>> {
    val nameUpper = (productName ?: "").uppercase()
    val saladCommonExclusions = listOf(
        "Χωρίς ντομάτα",
        "Χωρίς αγγούρι",
        "Χωρίς κρεμμύδι",
        "Χωρίς πιπεριά",
        "Χωρίς φέτα",
        "Χωρίς κάπαρη",
        "Χωρίς αυγό",
        "Χωρίς τυρί",
        "Χωρίς ζαμπόν",
        "Χωρίς μαρούλι",
        "Χωρίς μπέικον",
        "Χωρίς παρμεζάνα",
        "Χωρίς κρουτόν"
    )
    val clubCommonExclusions = listOf(
        "Χωρίς τυρί",
        "Χωρίς γαλοπούλα",
        "Χωρίς ντομάτα",
        "Χωρίς μαγιονέζα",
        "Χωρίς μαρούλι",
        "Χωρίς πατάτες",
        "Χωρίς ζαμπόν",
        "Χωρίς μπέικον",
        "Χωρίς αυγό"
    )
    val pizzaCommonExclusions = listOf(
        "Χωρίς βασιλικό",
        "Χωρίς μανιτάρια",
        "Χωρίς μπέικον",
        "Χωρίς τυρί",
        "Χωρίς ζαμπόν",
        "Χωρίς ντομάτα",
        "Χωρίς κοτόπουλο",
        "Χωρίς πιπεριά",
        "Χωρίς κρέμα γάλακτος"
    )
    return when {
        // BRUNCH
        nameUpper.contains("ART ΟΜΕΛΕΤΑ") || nameUpper.contains("ΟΜΕΛΕΤΑ ART") -> listOf("Χωρίς πιπεριές", "Χωρίς μπέικον", "Χωρίς ντομάτα")
        nameUpper.contains("GREEK BREAKFAST") -> listOf("Χωρίς λουκάνικα", "Χωρίς πίτα", "Χωρίς πατάτες", "Χωρίς σαλάτα", "Χωρίς φέτα", "Χωρίς αγγούρι", "Χωρίς ντομάτα")

        // ΣΑΛΑΤΕΣ
        nameUpper.contains("ΧΩΡΙΆΤΙΚΗ") || nameUpper.contains("ΧΩΡΙΑΤΙΚΗ") -> {
            val base = listOf("Χωρίς ντομάτα", "Χωρίς αγγούρι", "Χωρίς κρεμμύδι", "Χωρίς πιπεριά", "Χωρίς φέτα", "Χωρίς λάδι")
            (base + saladCommonExclusions).distinct()
        }
        nameUpper.contains("CHEF") -> {
            val base = listOf("Χωρίς αυγό", "Χωρίς τυρί", "Χωρίς ζαμπόν", "Χωρίς μαρούλι", "Χωρίς cocktail sauce")
            (base + saladCommonExclusions).distinct()
        }
        nameUpper.contains("CAESAR") -> {
            val base = listOf("Χωρίς κοτόπουλο", "Χωρίς μπέικον", "Χωρίς παρμεζάνα", "Χωρίς κρουτόν", "Χωρίς Caesar sauce")
            (base + saladCommonExclusions).distinct()
        }
        nameUpper.contains("ΣΑΛΑΤΑ") || nameUpper.contains("ΣΑΛΆΤΑ") || (nameUpper.contains("SALAD") && !nameUpper.contains("TUTTI FRUTTI")) -> {
            saladCommonExclusions.distinct()
        }

        // SNACKS / ΤΟΣΤ
        nameUpper.contains("ΓΑΛΟΠΟΥΛΑ") && nameUpper.contains("ΤΟΣΤ") -> listOf("Χωρίς τυρί", "Χωρίς γαλοπούλα", "Χωρίς πατατάκια")
        nameUpper.contains("ΖΑΜΠΟΝ") && nameUpper.contains("ΤΟΣΤ") || nameUpper.contains("ΖΑΜΠΌΝ") && nameUpper.contains("ΤΟΣΤ") -> listOf("Χωρίς τυρί", "Χωρίς ζαμπόν", "Χωρίς πατατάκια")

        // ΤΟΡΤΙΓΙΕΣ
        nameUpper.contains("ΤΟΡΤΙΓΙΑ") && nameUpper.contains("ΓΑΛΟΠΟΥΛΑ") -> listOf("Χωρίς gouda", "Χωρίς γαλοπούλα", "Χωρίς μαρούλι", "Χωρίς μαγιονέζα", "Χωρίς ντομάτα", "Χωρίς πατατάκια")
        nameUpper.contains("ΤΟΡΤΙΓΙΑ") && (nameUpper.contains("ΚΟΤΟΠΟΥΛΟ") || nameUpper.contains("ΦΙΛΕΤΟ")) -> listOf("Χωρίς gouda", "Χωρίς κοτόπουλο", "Χωρίς μαρούλι", "Χωρίς Caesar sauce", "Χωρίς ντομάτα", "Χωρίς πατατάκια")
        nameUpper.contains("ΤΟΡΤΙΓΙΑ") && nameUpper.contains("ΤΟΝΟΣ") || nameUpper.contains("ΤΟΡΤΙΓΙΑ") && nameUpper.contains("ΤΌΝΟΣ") -> listOf("Χωρίς τυρί", "Χωρίς τόνο", "Χωρίς μαγιονέζα", "Χωρίς αγγούρι", "Χωρίς καλαμπόκι", "Χωρίς πατατάκια")

        // CLUB SANDWICHES
        nameUpper.contains("CLUB") || nameUpper.contains("ΚΛΑΜΠ") -> {
            val base = when {
                nameUpper.contains("ΚΟΤΟΠΟΥΛΟ") -> listOf("Χωρίς κοτόπουλο", "Χωρίς Caesar sauce")
                else -> emptyList()
            }
            (base + clubCommonExclusions).distinct()
        }

        // BURGERS
        nameUpper.contains("CHEESEBURGER") || (nameUpper.contains("BURGER") && nameUpper.contains("CHEESE")) -> listOf("Χωρίς μαρούλι", "Χωρίς μπιφτέκι", "Χωρίς τυρί", "Χωρίς μουστάρδα", "Χωρίς κέτσαπ", "Χωρίς πατάτες")
        nameUpper.contains("BARBEQUE") || nameUpper.contains("BBQ") -> listOf("Χωρίς μαρούλι", "Χωρίς μπιφτέκι", "Χωρίς τυρί", "Χωρίς BBQ sauce", "Χωρίς μπέικον", "Χωρίς πατάτες")
        nameUpper.contains("ART BURGER") || (nameUpper.contains("BURGER") && nameUpper.contains("ART")) -> listOf("Χωρίς μαρούλι", "Χωρίς μπιφτέκι", "Χωρίς τυρί", "Χωρίς μπέικον", "Χωρίς αυγό", "Χωρίς cocktail sauce", "Χωρίς πατάτες")

        // PIZZA
        nameUpper.contains("PIZZA") || nameUpper.contains("ΠΙΤΣΑ") || nameUpper.contains("ΠΊΤΣΑ") -> {
            val base = when {
                nameUpper.contains("AM\"ART\"IA") || nameUpper.contains("AMARTIA") || nameUpper.contains("AM ART IA") -> listOf("Χωρίς σάλτσα ντομάτας", "Χωρίς παρμεζάνα", "Χωρίς BBQ sauce")
                else -> listOf("Χωρίς σάλτσα ντομάτας")
            }
            (base + pizzaCommonExclusions).distinct()
        }

        // ΠΟΙΚΙΛΙΕΣ & ΜΕΡΙΔΕΣ
        nameUpper.contains("ΛΟΥΚΑΝΙΚΟ") -> listOf("Χωρίς πατάτες", "Χωρίς πίτα", "Χωρίς κέτσαπ", "Χωρίς μουστάρδα", "Χωρίς σως", "Χωρίς σαλάτα")
        nameUpper.contains("ΜΠΙΦΤΕΚΙ") -> listOf("Χωρίς πατάτες", "Χωρίς πίτα", "Χωρίς κέτσαπ", "Χωρίς μουστάρδα", "Χωρίς σως", "Χωρίς σαλάτα")
        nameUpper.contains("ΠΛΑΤΟ") || nameUpper.contains("ΠΛΑΤΌ") || nameUpper.contains("PLATTER") -> listOf("Χωρίς κριτσίνια")
        nameUpper.contains("ΠΟΙΚΙΛΙΑ") || nameUpper.contains("ΠΟΙΚΙΛΊΑ") -> listOf("Χωρίς μουστάρδα", "Χωρίς κέτσαπ", "Χωρίς BBQ sauce", "Χωρίς Caesar sauce", "Χωρίς ντομάτα", "Χωρίς πίτες", "Χωρίς πατάτες")

        // LIGHT & HEALTHY
        nameUpper.contains("TUTTI FRUTTI") -> listOf("Χωρίς γιαούρτι", "Χωρίς μέλι")
        nameUpper.contains("MUESLI") -> listOf("Χωρίς γιαούρτι", "Χωρίς φρούτα", "Χωρίς δημητριακά", "Χωρίς μέλι")

        // ΒΑΦΛΕΣ
        nameUpper.contains("ΒΆΦΛΑ") || nameUpper.contains("ΒΑΦΛΑ") -> {
            val list = mutableListOf<String>()
            if (nameUpper.contains("ΠΑΓΩΤΟ") || nameUpper.contains("ΠΑΓΩΤΌ")) list.add("Χωρίς παγωτό")
            list.add("Χωρίς πραλίνα")
            if (nameUpper.contains("ΜΠΙΣΚΟΤΟ") || nameUpper.contains("ΜΠΙΣΚΌΤΟ")) list.add("Χωρίς μπισκότο")
            if (nameUpper.contains("ΜΠΑΝΑΝΑ") || nameUpper.contains("ΦΡΑΟΥΛΑ") || nameUpper.contains("ΦΡΟΥΤΟ") || nameUpper.contains("ΦΡΟΎΤΟ")) list.add("Χωρίς φρούτο")
            list.add("Χωρίς σαντιγί")
            list.add("Χωρίς σιρόπι")
            list
        }

        // PANCAKES
        nameUpper.contains("PANCAKES") || nameUpper.contains("PANCAKE") -> {
            val parsed = getPancakeExclusions(productName)
            if (parsed.isNotEmpty()) {
                parsed
            } else when {
                nameUpper.contains("ΜΠΙΣΚΟΤΟ") || nameUpper.contains("ΣΑΝΤΙΓΙ") -> listOf("Χωρίς πραλίνα", "Χωρίς μπισκότο", "Χωρίς σαντιγί")
                nameUpper.contains("ΜΕΛΙ") || nameUpper.contains("ΣΦΕΝΔΑΜΟΥ") -> listOf("Χωρίς μέλι", "Χωρίς σιρόπι σφενδάμου", "Χωρίς φρούτα")
                nameUpper.contains("ΓΑΛΟΠΟΥΛΑ") || (nameUpper.contains("ΜΑΡΟΥΛΙ") && !nameUpper.contains("CAESAR")) -> listOf("Χωρίς τυρί", "Χωρίς ζαμπόν", "Χωρίς γαλοπούλα", "Χωρίς ντομάτα", "Χωρίς μαρούλι")
                nameUpper.contains("BBQ") || nameUpper.contains("ΑΥΓΟ") -> listOf("Χωρίς τυρί", "Χωρίς ζαμπόν", "Χωρίς μπέικον", "Χωρίς ντομάτα", "Χωρίς BBQ sauce", "Χωρίς αυγό")
                nameUpper.contains("CAESAR") || nameUpper.contains("ΚΟΤΟΠΟΥΛΟ") -> listOf("Χωρίς τυρί", "Χωρίς κοτόπουλο", "Χωρίς μαρούλι", "Χωρίς παρμεζάνα", "Χωρίς μπέικον", "Χωρίς Caesar sauce")
                else -> listOf("Χωρίς τυρί", "Χωρίς αλλαντικό", "Χωρίς ντομάτα")
            }
        }

        else -> emptyList()
    }.map { it to 0.0 }
}

    val commonFoodExtras = listOf(
        "Έξτρα Ντομάτα" to 0.5,
        "Έξτρα Αυγό" to 1.0,
        "Έξτρα Μπέικον" to 0.7,
        "Κέτσαπ" to 0.7,
        "Μαγιονέζα" to 0.7,
        "Μουστάρδα" to 0.7,
        "Μπάρμπεκιου" to 0.7,
        "Σίζαρ" to 0.7
    )

    val iceCreamFlavors = listOf(
        "Σοκολάτα" to 0.0,
        "Φράουλα" to 0.0,
        "Βανίλια" to 0.0,
        "Στρατσιατέλα" to 0.0,
        "Φυστίκι" to 0.0,
        "Μπανάνα" to 0.0
    )

    // Default extras for Coffee / Chocolate / Food / Granita / Tea / Ice cream / Milkshake
    val listOptions = if (isIceCreamOrMilkshake) {
        iceCreamFlavors
    } else if (isFood) {
        val exclusions = getFoodExclusions(product.name)
        if (isSweetFood) exclusions else (exclusions + commonFoodExtras)
    } else if (product?.optionGroup ?: -1 >= 3 && serverOptions.isNotEmpty() && !isCustomPillsProduct) {
        serverOptions.map { it.name to it.price }
    } else when {
        product.name?.contains("Γρανίτα", ignoreCase = true) == true || product.name?.contains("Granita", ignoreCase = true) == true || product.optionGroup == 11 -> listOf(
            "Lemon" to 0.0,
            "Strawberry" to 0.0,
            "Peach" to 0.0,
            "Green Apple" to 0.0,
            "Pandesia" to 0.0,
            "Mandarin" to 0.0,
            "Cherry" to 0.0
        )
        isTea -> listOf(
            "Ροδάκινο" to 0.0,
            "Λεμόνι" to 0.0,
            "Βουνού" to 0.0,
            "Λεμόνι-Τζίντζερ" to 0.0,
            "Χαμομήλι" to 0.0,
            "Μέντα" to 0.0,
            "Βανίλια Καραμέλα" to 0.0,
            "Πράσινο" to 0.0,
            "Κλασσικό" to 0.0,
            "Μαύρο" to 0.0,
            "Φρούτα" to 0.0,
            "Μέλι" to 0.7,
            "Stevia" to 0.0,
            "Μαύρη ζάχαρη" to 0.0
        )
        categoryName?.contains("HOTLY", ignoreCase = true) == true || categoryName?.contains("Teabox", ignoreCase = true) == true -> listOf(
            "Ροδάκινο" to 0.0,
            "Λεμόνι" to 0.0,
            "Βουνού" to 0.0,
            "Λεμόνι-Τζίντζερ" to 0.0,
            "Χαμομήλι" to 0.0,
            "Μέντα" to 0.0,
            "Βανίλια Καραμέλα" to 0.0,
            "Πράσινο" to 0.0,
            "Κλασσικό" to 0.0,
            "Μαύρο" to 0.0,
            "Φρούτα" to 0.0,
            "Μέλι" to 0.7,
            "Stevia" to 0.0,
            "Μαύρη ζάχαρη" to 0.0
        )
        isCoffee -> listOf(
            "Μαύρη ζάχαρη" to 0.0,
            "Stevia" to 0.0,
            "Ζαχαρίνη" to 0.0,
            "Κανέλα" to 0.0,
            "Σοκολάτα" to 0.0,
            "Extra Εβαπορέ" to 0.5,
            "Σαντιγί" to 0.7,
            "Μέλι" to 0.7,
            "Μαύρος πάγος" to 0.5,
            "Decaf" to 0.0
        )
        isChocolate -> listOf(
            "Extra Σαντιγί" to 0.7
        )
        else -> emptyList()
    }
    
    val initialsExtras = listOptions
    
    var extras by remember(product.id) { 
        mutableStateOf(initialsExtras.associate { it.first to false }) 
    }
    
    var selectedSyrup by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.85f),
        content = {
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                color = Color(0xFFFAFAFA)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Text(
                        text = product.name ?: "Unknown",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(24.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f).padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Quantity
                        item {
                            OptionSection(title = "Ποσότητα:") {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        IconButton(
                                            onClick = { if (quantity > 1) quantity-- },
                                            modifier = Modifier.size(48.dp).background(gooseGold, CircleShape)
                                        ) { Text("-", color = Color.White, fontSize = 24.sp) }
                                        
                                        Text(quantity.toString(), fontSize = 20.sp)
                                        
                                        IconButton(
                                            onClick = { quantity++ },
                                            modifier = Modifier.size(48.dp).background(gooseGold, CircleShape)
                                        ) { Text("+", color = Color.White, fontSize = 24.sp) }
                                    }

                                    // Plastic Checkbox (Only for drinks)
                                    if (!isFood) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { isPlastic = !isPlastic }
                                        ) {
                                            Checkbox(
                                                checked = isPlastic,
                                                onCheckedChange = { isPlastic = it },
                                                colors = CheckboxDefaults.colors(checkedColor = gooseGold)
                                            )
                                            Text("Πλαστικό", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }

                        // Option Group 3+ / Slash Options (e.g. Ζαμπόν/Γαλοπούλα, COCA COLA/ZERO)
                        if (slashOptions.isNotEmpty()) {
                            item {
                                OptionSection(title = "Επιλογή:") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        slashOptions.forEach { choice ->
                                            OptionPill(
                                                text = choice,
                                                selected = selectedSlashOption == choice,
                                                onClick = { selectedSlashOption = choice },
                                                gooseGold = gooseGold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Mojito Options (Strawberry, Mango, Forest Fruits)
                        if (isMojito) {
                            item {
                                OptionSection(title = "Γεύση Mojito:") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        mojitoFlavors.forEach { choice ->
                                            OptionPill(
                                                text = choice,
                                                selected = selectedMojitoFlavor == choice,
                                                onClick = { selectedMojitoFlavor = choice },
                                                gooseGold = gooseGold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // GIAGIAMAS Simple Options (Σόδα, Νερό)
                        if (isGiagiamasSimple) {
                            item {
                                OptionSection(title = "Βάση / Αναμεικτικό:") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        giagiamasMixers.forEach { choice ->
                                            OptionPill(
                                                text = choice,
                                                selected = selectedGiagiamasMixer == choice,
                                                onClick = { selectedGiagiamasMixer = choice },
                                                gooseGold = gooseGold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Corona Beer Options (Αλάτι, Λεμόνι)
                        if (isCorona) {
                            item {
                                OptionSection(title = "Επιλογή:") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        coronaExtras.forEach { choice ->
                                            OptionPill(
                                                text = choice,
                                                selected = selectedCoronaExtra == choice,
                                                onClick = { selectedCoronaExtra = if (selectedCoronaExtra == choice) null else choice },
                                                gooseGold = gooseGold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Milkshake Barista Pro Options (LOVE IT, BUENO, RED VELVET)
                        if (isMilkshakeBaristaPro) {
                            item {
                                OptionSection(title = "Γεύση Barista Pro:") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        baristaProFlavors.forEach { choice ->
                                            OptionPill(
                                                text = choice,
                                                selected = selectedBaristaProFlavor == choice,
                                                onClick = { selectedBaristaProFlavor = choice },
                                                gooseGold = gooseGold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Amita Juice Options (Ροδάκινο, Πορτοκάλι, Βύσσινο, Ανανάς, 4 Φρούτα, Λεμόνι)
                        if (isAmita) {
                            item {
                                OptionSection(title = "Γεύση Amita:") {
                                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(amitaFlavors) { choice ->
                                            OptionPill(
                                                text = choice,
                                                selected = selectedAmitaFlavor == choice,
                                                onClick = { selectedAmitaFlavor = choice },
                                                gooseGold = gooseGold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Schweppes Options (Orange, Pink Grapefruit, Lemonade)
                        if (isSchweppes) {
                            item {
                                OptionSection(title = "Γεύση Schweppes:") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        schweppesFlavors.forEach { choice ->
                                            OptionPill(
                                                text = choice,
                                                selected = selectedSchweppesFlavor == choice,
                                                onClick = { selectedSchweppesFlavor = choice },
                                                gooseGold = gooseGold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Wine Glass 180ml Options (Λευκό Ξηρό, Λευκό Ημίγλυκο, Κόκκινο Ξηρό, Κόκκινο Ημίγλυκο)
                        if (isWineGlass) {
                            item {
                                OptionSection(title = "Επιλογή Κρασιού:") {
                                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(wineFlavors) { choice ->
                                            OptionPill(
                                                text = choice,
                                                selected = selectedWineFlavor == choice,
                                                onClick = { selectedWineFlavor = choice },
                                                gooseGold = gooseGold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Option Group 2: Chocolate Options (Hot/Cold Temperature)
                        if (effectiveOptionGroup == 2) {
                            item {
                                OptionSection(title = "Θερμοκρασία:") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("Ζεστή", "Κρύα").forEach { label ->
                                            OptionPill(
                                                text = label,
                                                selected = selectedTemp == label,
                                                onClick = { selectedTemp = label },
                                                gooseGold = gooseGold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Specific for Tea: Temperature (Ζεστό / Κρύο)
                        if (isTea) {
                            item {
                                OptionSection(title = "Θερμοκρασία:") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("Ζεστό", "Κρύο").forEach { label ->
                                            OptionPill(
                                                text = label,
                                                selected = selectedTemp == label,
                                                onClick = { selectedTemp = label },
                                                gooseGold = gooseGold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Option Group 1: Full Coffee UI (Sugar, Milk, Syrups)
                        if (effectiveOptionGroup == 1) {
                            // Sugar
                            item {
                                OptionSection(title = "Ζάχαρη:") {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf("ΣΚ", "Ο", "Μ", "ΜΓ", "Γ").forEach { label ->
                                            OptionPill(
                                                text = label,
                                                selected = selectedSugar == label,
                                                onClick = { selectedSugar = label },
                                                gooseGold = gooseGold
                                            )
                                        }
                                    }
                                }
                            }

                            // Milk
                            item {
                                OptionSection(title = "Γάλα:") {
                                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(listOf("Όχι", "Εβαπορέ", "Φρέσκο", "Αμυγδάλου (+€0.80)")) { label ->
                                            OptionPill(
                                                text = label,
                                                selected = selectedMilk == label,
                                                onClick = { selectedMilk = label },
                                                gooseGold = gooseGold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Almond Milk for other drinkable items
                        if (isDrink && effectiveOptionGroup != 1) {
                            item {
                                OptionSection(title = "Γάλα:") {
                                    OptionPill(
                                        text = "Γάλα Αμυγδάλου (+€0.80)",
                                        selected = selectedMilk.contains("Αμυγδάλου"),
                                        onClick = {
                                            selectedMilk = if (selectedMilk.contains("Αμυγδάλου")) "Όχι" else "Αμυγδάλου (+€0.80)"
                                        },
                                        gooseGold = gooseGold
                                    )
                                }
                            }
                        }

                        // Syrups (for all drinkable items: Coffee, Chocolate, Milkshake, Tea, Giagiamas, etc.)
                        if (isDrink) {
                            item {
                                OptionSection(title = "Σιρόπι (+€0.60):") {
                                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(listOf("Καραμέλα", "Φουντούκι", "Σοκολάτα", "Φράουλα")) { label ->
                                            OptionPill(
                                                text = label,
                                                selected = selectedSyrup == label,
                                                onClick = { selectedSyrup = if (selectedSyrup == label) null else label },
                                                gooseGold = gooseGold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Toggle List for Extras (Coffee Extras, Food Extras, Chocolate Extras, Ice Cream / Milkshake Flavors)
                        if (initialsExtras.isNotEmpty()) {
                            item {
                                OptionSection(title = if (isIceCreamOrMilkshake) "Γεύσεις:" else "Extras / Επιλογές:") {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        for ((name, price) in initialsExtras) {
                                            val isSelected = extras[name] ?: false
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable { extras = extras + (name to !isSelected) },
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(name, fontSize = 16.sp)
                                                    Text(if (price == 0.0) "δωρεάν" else "+€${String.format("%.2f", price)}", fontSize = 14.sp, color = Color.Gray)
                                                }
                                                Switch(
                                                    checked = isSelected,
                                                    onCheckedChange = { extras = extras + (name to it) },
                                                    colors = SwitchDefaults.colors(checkedThumbColor = gooseGold, checkedTrackColor = gooseGold.copy(alpha = 0.5f))
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        item { Spacer(Modifier.height(16.dp)) }
                    }

                    // Footer
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Άκυρο", color = Color.Gray, fontSize = 18.sp)
                        }
                        
                        val extraCost = initialsExtras.sumOf { if (extras[it.first] == true) it.second else 0.0 }
                        val syrupCost = if (selectedSyrup != null) 0.6 else 0.0
                        val milkCost = if (selectedMilk.contains("Αμυγδάλου")) 0.8 else 0.0
                        val totalPrice = (product.price + extraCost + syrupCost + milkCost) * quantity

                        Button(
                            onClick = {
                                val optionsList = mutableListOf<String>()
                                if (isPlastic) optionsList.add("ΠΛΑΣΤΙΚΟ")
                                
                                if (effectiveOptionGroup == 2) {
                                    optionsList.add(selectedTemp)
                                } else if (isTea) {
                                    optionsList.add(selectedFlavor)
                                } else if (effectiveOptionGroup == 1) {
                                    val sugarText = when(selectedSugar) {
                                        "ΣΚ" -> "Σκέτος"
                                        "Ο" -> "Ολίγη"
                                        "Μ" -> "Μέτριος"
                                        "ΜΓ" -> "Μέτριος Γλυκός"
                                        "Γ" -> "Γλυκός"
                                        else -> selectedSugar
                                    }
                                    optionsList.add(sugarText)
                                }

                                if (selectedMilk != "Όχι") {
                                    if (selectedMilk.contains("Αμυγδάλου")) {
                                        optionsList.add("Γάλα Αμυγδάλου")
                                    } else {
                                        optionsList.add("Γάλα $selectedMilk")
                                    }
                                }
                                if (selectedSyrup != null) {
                                    optionsList.add("Σιρόπι $selectedSyrup")
                                }
                                
                                if (isMojito && !selectedMojitoFlavor.isNullOrBlank()) {
                                    optionsList.add(selectedMojitoFlavor!!)
                                }

                                if (isGiagiamasSimple && !selectedGiagiamasMixer.isNullOrBlank()) {
                                    optionsList.add(selectedGiagiamasMixer!!)
                                }

                                if (isCorona && !selectedCoronaExtra.isNullOrBlank()) {
                                    optionsList.add(selectedCoronaExtra!!)
                                }

                                if (isMilkshakeBaristaPro && !selectedBaristaProFlavor.isNullOrBlank()) {
                                    optionsList.add(selectedBaristaProFlavor!!)
                                }

                                if (isAmita && !selectedAmitaFlavor.isNullOrBlank()) {
                                    optionsList.add(selectedAmitaFlavor!!)
                                }

                                if (isSchweppes && !selectedSchweppesFlavor.isNullOrBlank()) {
                                    optionsList.add(selectedSchweppesFlavor!!)
                                }

                                if (isWineGlass && !selectedWineFlavor.isNullOrBlank()) {
                                    optionsList.add(selectedWineFlavor!!)
                                }
                                
                                if (slashOptions.isNotEmpty() && !selectedSlashOption.isNullOrBlank()) {
                                    optionsList.add(selectedSlashOption!!)
                                }
                                
                                extras.forEach { (name, selected) -> if (selected) optionsList.add(name) }
                                
                                onConfirm(DraftItem(
                                    id = product.id,
                                    name = product.name ?: "Unknown",
                                    quantity = quantity,
                                    unitPrice = product.price,
                                    optionsPrice = extraCost + syrupCost + milkCost,
                                    optionsText = optionsList.joinToString(", ")
                                ))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                "Προσθήκη — €${String.format("%.2f", totalPrice)}",
                                color = gooseGold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun OptionSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontSize = 16.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
        content()
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
    }
}

@Composable
fun OptionPill(text: String, selected: Boolean, onClick: () -> Unit, gooseGold: Color) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) gooseGold else Color.LightGray),
        color = if (selected) gooseGold.copy(alpha = 0.1f) else Color.Transparent,
        modifier = Modifier.height(40.dp).padding(horizontal = 4.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(text = text, color = if (selected) gooseGold else Color.Black, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
fun CustomProductDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit,
    gooseGold: Color
) {
    if (show) {
        var name by remember { mutableStateOf("") }
        var priceStr by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Νέο Custom Προϊόν", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Όνομα Προϊόντος (π.χ. Τσίπουρο)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null || it.endsWith(".")) priceStr = it },
                        label = { Text("Τιμή (€)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        val price = priceStr.toDoubleOrNull() ?: 0.0
                        onConfirm(name, price)
                    },
                    enabled = name.isNotBlank() && priceStr.toDoubleOrNull() != null,
                    colors = ButtonDefaults.buttonColors(containerColor = gooseGold)
                ) {
                    Text("Προσθήκη")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Άκυρο", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun OrderLinePaymentCard(
    line: com.ntvelop.goldengoosepda.network.OrderLineResponse,
    onPayCash: () -> Unit,
    onPayCard: () -> Unit
) {
    val lineTotalPrice = (line.unitPrice + line.optionsPrice) * line.quantity
    val isPaid = line.paidStatus

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        color = if (isPaid) Color(0xFFE8F5E9) else Color(0xFFFAFAFA),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isPaid) Color(0xFF81C784) else Color(0xFFE0E0E0)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${line.quantity}x ${line.productName}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaid) Color(0xFF2E7D32) else Color.Black,
                        style = if (isPaid) androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else androidx.compose.ui.text.TextStyle()
                    )

                    if (line.optionsText.isNotBlank()) {
                        Text(
                            text = line.optionsText,
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }

                    if (!line.note.isNullOrBlank()) {
                        Text(
                            text = "Σημείωση: ${line.note}",
                            fontSize = 13.sp,
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = "€${String.format("%.2f", lineTotalPrice)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPaid) Color(0xFF2E7D32) else Color.Black
                )
            }

            Spacer(Modifier.height(8.dp))

            if (isPaid) {
                Surface(
                    color = Color(0xFF4CAF50),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(
                            text = "ΠΛΗΡΩΘΗΚΕ (${when (line.paymentMethod) { "CASH" -> "Μετρητά"; "CARD" -> "Κάρτα"; else -> "Εξοφλήθη" }})",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFFFF3E0),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = "ΑΝΕΞΟΦΛΗΤΟ",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color(0xFFE65100),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    Button(
                        onClick = onPayCash,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
                    ) {
                        Text("💵 Μετρητά", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onPayCard,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
                    ) {
                        Text("💳 Κάρτα", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
