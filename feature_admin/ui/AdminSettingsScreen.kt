package com.ntvelop.goldengoosepda.feature_admin.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ntvelop.goldengoosepda.feature_admin.vm.AdminSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    viewModel: AdminSettingsViewModel,
    onBack: () -> Unit
) {
    val tableCount by viewModel.tableCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ρυθμίσεις Διαχειριστή") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Πίσω")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Table Management Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Διαχείριση Τραπεζιών",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Ορίστε τον αριθμό των τραπεζιών στο εστιατόριο",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = tableCount.toString(),
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) {
                                    viewModel.updateTableCount(it.toIntOrNull() ?: 0)
                                }
                            },
                            label = { Text("Αριθμός Τραπεζιών") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            enabled = !isLoading
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.syncTables() },
                            enabled = !isLoading && tableCount > 0
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Συγχρονισμός")
                            }
                        }
                    }

                    // Error message
                    error?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Success message
                    successMessage?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "ℹ️ Σημείωση",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Ο συγχρονισμός τραπεζιών θα προσθέσει ή θα αφαιρέσει τραπέζια από τη βάση δεδομένων για να ταιριάζει με τον αριθμό που ορίσατε.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    // Clear messages after showing
    LaunchedEffect(error, successMessage) {
        if (error != null || successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }
}
