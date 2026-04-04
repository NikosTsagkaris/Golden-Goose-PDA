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
            BottomActionPanel(
                draftItems = draftCart,
                noteText = noteText,
                onNoteChange = { noteText = it },
                onSubmit = { viewModel.submitOrder(orderId, noteText) },
                gooseColor = gooseGold
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
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
                // Add spacer at bottom for scrolling past the FAB-like panel if needed
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
            placeholder = { Text("Σχόλιο") },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
        )

        Spacer(Modifier.height(8.dp))

        if (draftItems.isEmpty()) {
            Text(
                "Καμία γραμμή παραγγελίας ακόμη.",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Start
            )
        } else {
            val total = draftItems.sumOf { (it.unitPrice * it.quantity) + it.optionsPrice }
            Text(
                "${draftItems.size} είδη - Σύνολο: €${String.format("%.2f", total)}",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = gooseColor
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = gooseColor),
            enabled = draftItems.isNotEmpty()
        ) {
            Text("Ολοκλήρωση", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
    
    val isChocolate = product.name?.contains("Σοκολάτα", ignoreCase = true) == true
    val isTea = product.name?.contains("Lipton", ignoreCase = true) == true

    // Default extras for Coffee
    // Convert server options to initialsExtras format
    val listOptions = if (product?.optionGroup ?: -1 >= 2) {
        serverOptions.map { it.name to it.price }
    } else when (categoryName) {
        "ΦΑΓΗΤΟ" -> listOf("Πατάτες" to 1.0)
        "HOTLY", "Teabox" -> listOf(
            "Μέλι" to 0.3,
            "Stevia" to 0.0,
            "Μαύρη ζάχαρη" to 0.0
        )
        "ΚΑΦΕΣ" -> listOf(
            "Μαύρη ζάχαρη" to 0.0,
            "Stevia" to 0.0,
            "Κανέλα" to 0.0,
            "Σοκολάτα" to 0.0,
            "Μέλι" to 0.3,
            "Μαύρος πάγος" to 0.3,
            "Decaf" to 0.3
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
                                    if (categoryName != "ΦΑΓΗΤΟ") {
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

                        // Specific for Option Group 1: Hot/Cold (or specific isChocolate check)
                        if (product.optionGroup == 1 || isChocolate) {
                            item {
                                OptionSection(title = "Θερμοκρασία:") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("Κρύα", "Ζεστή").forEach { label ->
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

                        // Specific for Lipton: Flavor
                        if (isTea) {
                            item {
                                OptionSection(title = "Γεύση:") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("Ροδάκινο", "Λεμόνι").forEach { label ->
                                            OptionPill(
                                                text = label,
                                                selected = selectedFlavor == label,
                                                onClick = { selectedFlavor = label },
                                                gooseGold = gooseGold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Group 0: Full Coffee UI (Sugar, Milk, Syrup)
                        if (product.optionGroup == 0) {
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
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("Όχι", "Εβαπορέ", "Φρέσκο").forEach { label ->
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

                            // Syrups (Coffee only)
                            item {
                                OptionSection(title = "Σιρόπι (+0.50):") {
                                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(listOf("Φουντούκι", "Φράουλα", "Καραμέλα")) { label ->
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

                        // Group 2+: Dynamic Toggle List (Custom Extras)
                        if (product.optionGroup >= 2) {
                            item {
                                OptionSection(title = "Επιλογές:") {
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
                                                    Text(if (price == 0.0) "δωρεάν" else "+$price", fontSize = 14.sp, color = Color.Gray)
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

                        // Fallback/Common Extras for Coffee (only if group 0 and no specific isChocolate check)
                        if (product.optionGroup == 0 && initialsExtras.isNotEmpty()) {
                            item {
                                OptionSection(title = "Extras:") {
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
                                                    Text(if (price == 0.0) "δωρεάν" else "+$price", fontSize = 14.sp, color = Color.Gray)
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
                        val syrupCost = if (selectedSyrup != null) 0.5 else 0.0
                        val totalPrice = (product.price + extraCost + syrupCost) * quantity

                        Button(
                            onClick = {
                                val optionsList = mutableListOf<String>()
                                if (product.optionGroup != -1) {
                                    if (isPlastic) optionsList.add("ΠΛΑΣΤΙΚΟ")
                                    
                                    if (product.optionGroup == 1 || isChocolate) {
                                        optionsList.add(selectedTemp)
                                    } else if (isTea) {
                                        optionsList.add(selectedFlavor)
                                    } else if (product.optionGroup == 0) {
                                        optionsList.add(selectedSugar)
                                        if (selectedMilk != "Όχι") {
                                            optionsList.add(selectedMilk)
                                        }
                                        if (selectedSyrup != null) {
                                            optionsList.add("Σιρόπι $selectedSyrup")
                                        }
                                    }
                                }
                                
                                extras.forEach { (name, selected) -> if (selected) optionsList.add(name) }
                                
                                onConfirm(DraftItem(
                                    id = product.id,
                                    name = product.name ?: "Unknown",
                                    quantity = quantity,
                                    unitPrice = product.price,
                                    optionsPrice = extraCost + syrupCost,
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
