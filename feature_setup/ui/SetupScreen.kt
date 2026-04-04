package com.ntvelop.goldengoosepda.feature_setup.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ntvelop.goldengoosepda.feature_setup.vm.SetupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    viewModel: SetupViewModel,
    onBack: () -> Unit
) {
    val serverIp by viewModel.serverIp.collectAsState()
    val printerIp by viewModel.printerIp.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ρυθμίσεις static IP") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Εισάγετε τη DNS/IP του Server",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = serverIp,
                onValueChange = { viewModel.updateIp(it) },
                label = { Text("Server IP Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Εισάγετε τη DNS/IP του Εκτυπωτή",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = printerIp,
                onValueChange = { viewModel.updatePrinterIp(it) },
                label = { Text("Printer IP Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    viewModel.saveIp()
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Αποθήκευση")
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Σημείωση: Η εφαρμογή ίσως χρειαστεί επανεκκίνηση για να εφαρμοστεί η νέα IP.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
