package com.ntvelop.goldengoosepda.feature_auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.ntvelop.goldengoosepda.feature_auth.vm.LoginUiState
import com.ntvelop.goldengoosepda.feature_auth.vm.LoginViewModel
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.ntvelop.goldengoosepda.R

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onSetupClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var pin by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        if (state is LoginUiState.Success) {
            onLoginSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onSetupClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Settings,
                contentDescription = "Setup",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.briki_logo),
                contentDescription = "Golden Goose Logo",
                modifier = Modifier.size(180.dp)
            )
            Spacer(Modifier.height(24.dp))
            Text("GOLDEN GOOSE POS", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text("Enter your PIN to login", fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)
            
            Spacer(Modifier.height(48.dp))
            
            // PIN Display (dots)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { index ->
                    val char = pin.getOrNull(index)
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = if (char != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ) {}
                }
            }

            Spacer(Modifier.height(48.dp))

            if (state is LoginUiState.Error) {
                Text((state as LoginUiState.Error).message, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
            }

            if (state is LoginUiState.Loading) {
                CircularProgressIndicator()
            } else {
                // Numpad
                Numpad(
                    onNumberClick = { num ->
                        if (pin.length < 4) {
                            pin += num
                            if (pin.length == 4) {
                                viewModel.onPinEntered(pin)
                            }
                        }
                    },
                    onDeleteClick = {
                        if (pin.isNotEmpty()) pin = pin.dropLast(1)
                    }
                )
            }
        }
    }
}

@Composable
fun Numpad(onNumberClick: (String) -> Unit, onDeleteClick: () -> Unit) {
    val numbers = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in numbers) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (item in row) {
                    if (item.isEmpty()) {
                        Spacer(Modifier.size(80.dp))
                    } else {
                        Button(
                            onClick = { if (item == "DEL") onDeleteClick() else onNumberClick(item) },
                            modifier = Modifier.size(80.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            colors = if (item == "DEL") ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                                     else ButtonDefaults.buttonColors()
                        ) {
                            Text(item, fontSize = 24.sp)
                        }
                    }
                }
            }
        }
    }
}
