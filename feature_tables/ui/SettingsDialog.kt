package com.ntvelop.goldengoosepda.feature_tables.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ntvelop.goldengoosepda.feature_shifts.vm.ShiftViewModel

@Composable
fun SettingsDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    shiftViewModel: ShiftViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf(initialName) }
    var showEndShiftConfirmation by remember { mutableStateOf(false) }
    
    val totals by shiftViewModel.totals.collectAsState()

    LaunchedEffect(Unit) {
        shiftViewModel.loadTotals()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ρυθμίσεις", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Waiter name section
                Text("Όνομα Σερβιτόρου (για εκτύπωση):", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("π.χ. Νίκος") }
                )
                
                Spacer(Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
                
                // Shift totals section
                Text("Σύνολα Βάρδιας", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                
                totals?.let { t ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Μετρητά:", style = MaterialTheme.typography.bodyMedium)
                        Text("€${String.format("%.2f", t.cash)}", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Κάρτα:", style = MaterialTheme.typography.bodyMedium)
                        Text("€${String.format("%.2f", t.card)}", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("ΣΥΝΟΛΟ:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("€${String.format("%.2f", t.total)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                } ?: run {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Shift control buttons
                Button(
                    onClick = { shiftViewModel.printSummary() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Εκτύπωση Συνόλου")
                }
                
                Spacer(Modifier.height(8.dp))
                
                Button(
                    onClick = { showEndShiftConfirmation = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Τέλος Βάρδιας")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name) }) {
                Text("Αποθήκευση")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Κλείσιμο")
            }
        }
    )
    
    // End shift confirmation dialog
    if (showEndShiftConfirmation) {
        EndShiftConfirmationDialog(
            onConfirm = {
                shiftViewModel.endShift {
                    showEndShiftConfirmation = false
                }
            },
            onDismiss = { showEndShiftConfirmation = false }
        )
    }
}

@Composable
fun EndShiftConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Τέλος Βάρδιας") },
        text = { 
            Text("Είστε σίγουρος για αυτήν τον μηδενισμό;\n\nΘα διαγραφούν όλες οι πληρωμένες παραγγελίες και θα μηδενιστούν τα σύνολα.") 
        },
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
