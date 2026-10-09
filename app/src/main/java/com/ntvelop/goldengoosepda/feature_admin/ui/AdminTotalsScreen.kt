package com.ntvelop.goldengoosepda.feature_admin.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ntvelop.goldengoosepda.feature_admin.vm.AdminTotalsViewModel
import com.ntvelop.goldengoosepda.network.WaiterTotalResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTotalsScreen(
    onBack: () -> Unit,
    viewModel: AdminTotalsViewModel = hiltViewModel()
) {
    val globalTotals by viewModel.globalTotals.collectAsState()
    val waiterTotals by viewModel.waiterTotals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Σύνολα Διαχείρισης") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Πίσω")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAll() }) {
                        Icon(Icons.Default.Refresh, "Ανανέωση")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading && globalTotals == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Σφάλμα: $error", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadAll() }) {
                            Text("Επανάληψη")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Global Totals Card
                        item {
                            globalTotals?.let { t ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            "ΓΕΝΙΚΟ ΣΥΝΟΛΟ ΒΑΡΔΙΑΣ",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        TotalRow("Μετρητά", t.cash)
                                        TotalRow("Κάρτα (POS)", t.card)
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                        TotalRow("ΣΥΝΟΛΟ", t.total, isMain = true)
                                    }
                                }
                            }
                        }

                        // Waiter Breakdown Header
                        item {
                            Text(
                                "Ανάλυση ανά Σερβιτόρο",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        // Waiter Breakdown List
                        items(waiterTotals) { wt ->
                            WaiterTotalCard(wt = wt)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WaiterTotalCard(wt: WaiterTotalResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = wt.waiterName.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "${wt.orderCount} παρ.",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            TotalRow("Μετρητά", wt.cash, style = MaterialTheme.typography.bodySmall)
            TotalRow("Κάρτα", wt.card, style = MaterialTheme.typography.bodySmall)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Εκκρεμεί:", style = MaterialTheme.typography.bodySmall, color = Color(0xFFE65100))
                Text("€${String.format("%.2f", wt.unpaidTotal)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Σύνολο:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text("€${String.format("%.2f", wt.total)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TotalRow(
    label: String,
    amount: Double,
    isMain: Boolean = false,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isMain) MaterialTheme.typography.titleLarge else style,
            fontWeight = if (isMain) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = "€${String.format("%.2f", amount)}",
            style = if (isMain) MaterialTheme.typography.titleLarge else style,
            fontWeight = FontWeight.Bold,
            color = if (isMain) MaterialTheme.colorScheme.primary else Color.Unspecified
        )
    }
}
