package com.ntvelop.goldengoosepda.feature_orders.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ntvelop.goldengoosepda.feature_orders.vm.OrderViewModel
import com.ntvelop.goldengoosepda.network.OrderResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersListScreen(
    title: String,
    orders: List<OrderResponse>,
    onOrderClick: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←") }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            items(orders) { order ->
                ListItem(
                    headlineContent = { Text("Τραπέζι ${order.tableId}") },
                    supportingContent = { Text("Waiter: ${order.waiterId} | ${order.createdAt}") },
                    trailingContent = { Text("${order.lines.size} items") },
                    modifier = Modifier.clickable { onOrderClick(order.id) }
                )
                HorizontalDivider()
            }
        }
    }
}
