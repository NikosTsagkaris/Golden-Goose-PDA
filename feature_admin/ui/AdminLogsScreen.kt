package com.ntvelop.goldengoosepda.feature_admin.ui

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
import com.ntvelop.goldengoosepda.feature_admin.vm.AdminLogsViewModel
import com.ntvelop.goldengoosepda.network.ActionLogResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLogsScreen(
    onBack: () -> Unit,
    viewModel: AdminLogsViewModel = hiltViewModel()
) {
    val logs by viewModel.logs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var showClearConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ιστορικό Ενεργειών") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Πίσω")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadLogs() }) {
                        Icon(Icons.Default.Refresh, "Ανανέωση")
                    }
                    IconButton(onClick = { showClearConfirmation = true }) {
                        Icon(Icons.Default.Delete, "Καθαρισμός", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading && logs.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Σφάλμα: $error", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadLogs() }) {
                            Text("Επανάληψη")
                        }
                    }
                }
                logs.isEmpty() -> {
                    Text(
                        "Δεν υπάρχει ιστορικό ενεργειών",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(logs) { log ->
                            LogItem(log = log)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Εκκαθάριση Ιστορικού") },
            text = { Text("Είστε σίγουρος για τον μηδενισμό του ιστορικού ενεργειών;") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearLogs()
                        showClearConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Συνέχεια")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Άκυρο")
                }
            }
        )
    }
}

@Composable
fun LogItem(log: ActionLogResponse) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = log.actionType,
                style = MaterialTheme.typography.labelMedium,
                color = when (log.actionType) {
                    "DELETE_ORDER" -> MaterialTheme.colorScheme.error
                    "END_SHIFT" -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.secondary
                },
                fontWeight = FontWeight.Bold
            )
            Text(
                text = log.createdAt.substringAfter("T").substringBefore("."),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        log.waiter?.let { waiter ->
            Text(
                text = "Από: ${waiter.username}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
        }
        Text(
            text = log.details,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
