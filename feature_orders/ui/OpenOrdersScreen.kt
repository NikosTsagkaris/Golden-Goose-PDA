package com.ntvelop.goldengoosepda.feature_orders.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ntvelop.goldengoosepda.feature_orders.vm.OpenOrdersViewModel
import com.ntvelop.goldengoosepda.network.OrderLineResponse
import com.ntvelop.goldengoosepda.network.OrderResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenOrdersScreen(
    onBack: () -> Unit,
    viewModel: OpenOrdersViewModel = hiltViewModel()
) {
    val orders by viewModel.orders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var orderToDelete by remember { mutableStateOf<OrderResponse?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ανοιχτές Παραγγελίες") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Πίσω")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadOrders() }) {
                        Icon(Icons.Default.Refresh, "Ανανέωση")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Σφάλμα: $error", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadOrders() }) {
                            Text("Επανάληψη")
                        }
                    }
                }
                orders.isEmpty() -> {
                    Text(
                        "Δεν υπάρχουν ανοιχτές παραγγελίες",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(orders) { order ->
                            OrderCard(
                                order = order,
                                onPayLine = { lineId, method ->
                                    viewModel.payLine(order.id, lineId, method)
                                },
                                onDelete = { orderToDelete = order }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    orderToDelete?.let { order ->
        DeleteConfirmationDialog(
            orderNumber = order.table?.name ?: order.tableId ?: "Unknown",
            onConfirm = {
                viewModel.deleteOrder(order.id)
                orderToDelete = null
            },
            onDismiss = { orderToDelete = null }
        )
    }
}

@Composable
fun OrderCard(
    order: OrderResponse,
    onPayLine: (String, String) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${order.table?.name ?: order.tableId}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = order.table?.area ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Διαγραφή",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Order lines
            order.lines.forEach { line ->
                OrderLineItem(
                    line = line,
                    onPayLine = onPayLine
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun OrderLineItem(
    line: OrderLineResponse,
    onPayLine: (String, String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${line.quantity}x ${line.productName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (line.paidStatus) FontWeight.Normal else FontWeight.Bold
            )
            if (line.optionsText.isNotEmpty()) {
                Text(
                    text = line.optionsText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            if (!line.note.isNullOrEmpty()) {
                Text(
                    text = "Σημείωση: ${line.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Text(
                text = "€${String.format("%.2f", line.unitPrice + line.optionsPrice)}",
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (line.paidStatus) {
            // Show payment method badge
            Surface(
                color = when (line.paymentMethod) {
                    "CASH" -> Color(0xFF4CAF50)
                    "CARD" -> Color(0xFF2196F3)
                    else -> Color.Gray
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = when (line.paymentMethod) {
                        "CASH" -> "Μετρητά"
                        "CARD" -> "Κάρτα"
                        else -> "Πληρώθηκε"
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        } else {
            // Payment buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onPayLine(line.id, "CASH") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Μετρητά", style = MaterialTheme.typography.labelSmall)
                }
                Button(
                    onClick = { onPayLine(line.id, "CARD") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Κάρτα", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    orderNumber: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Διαγραφή Παραγγελίας") },
        text = { Text("Είστε σίγουρος για αυτήν την διαγραφή;\n\nΤραπέζι: $orderNumber") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Συνέχεια")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Άκυρο")
            }
        }
    )
}
