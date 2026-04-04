package com.ntvelop.goldengoosepda.feature_tables.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ntvelop.goldengoosepda.feature_tables.vm.TableMapUiState
import com.ntvelop.goldengoosepda.feature_tables.vm.TableMapViewModel
import com.ntvelop.goldengoosepda.network.TableResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableMapScreen(
    viewModel: TableMapViewModel,
    onMenuClick: () -> Unit,
    onTableSelected: (String) -> Unit,
    isReadOnly: Boolean = false,
    currentUserId: String? = null
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isReadOnly) "Τραπέζια (Προβολή)" else "Τραπέζια") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshTables() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                is TableMapUiState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                is TableMapUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text((state as TableMapUiState.Error).message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.refreshTables() }) {
                            Text("Retry")
                        }
                    }
                }
                is TableMapUiState.Success -> {
                    TableGridLayout(
                        tables = (state as TableMapUiState.Success).tables,
                        onTableClick = { id -> 
                            if (!isReadOnly) {
                                viewModel.onTableClick(id, onTableSelected) 
                            }
                        },
                        isReadOnly = isReadOnly,
                        currentUserId = currentUserId
                    )
                }
            }
        }
    }
}

@Composable
fun TableGridLayout(
    tables: List<TableResponse>,
    onTableClick: (String) -> Unit,
    isReadOnly: Boolean,
    currentUserId: String?
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(tables) { table ->
            TableCard(
                table = table,
                onClick = { if (!isReadOnly) onTableClick(table.id) },
                currentUserId = currentUserId
            )
        }
    }
}

@Composable
fun TableCard(
    table: TableResponse,
    onClick: () -> Unit,
    currentUserId: String?
) {
    val status = table.status
    val isOwner = table.waiterId == currentUserId || currentUserId == null
    
    val backgroundColor = when {
        status == "OPEN" && isOwner -> Color(0xFFFFEBEE) // Light Red
        status == "OPEN" && !isOwner -> Color(0xFFFFF3E0) // Light Orange
        else -> Color.White
    }
    
    val borderColor = when {
        status == "OPEN" && isOwner -> Color(0xFFEF5350) // Red
        status == "OPEN" && !isOwner -> Color(0xFFFF9800) // Orange
        else -> Color.LightGray
    }

    val primaryTextColor = when {
        status == "OPEN" && isOwner -> Color(0xFFC62828)
        status == "OPEN" && !isOwner -> Color(0xFFE65100)
        else -> Color.Black
    }

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = table.name ?: "T?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor,
                textAlign = TextAlign.Center
            )
            
            if (status == "OPEN") {
                if (!isOwner && table.waiterUsername != null) {
                    Text(
                        text = table.waiterUsername,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE65100),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = String.format(java.util.Locale.getDefault(), "%.2f€", table.unpaidTotal),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }
            } else {
                Text(
                    text = "FREE",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}
