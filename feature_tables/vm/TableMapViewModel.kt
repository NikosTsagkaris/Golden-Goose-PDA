package com.ntvelop.goldengoosepda.feature_tables.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ntvelop.goldengoosepda.feature_tables.data.TablesRepository
import com.ntvelop.goldengoosepda.network.TableResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TableMapViewModel @Inject constructor(
    private val repository: TablesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TableMapUiState>(TableMapUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                refreshTables()
                delay(5000) // Refresh every 5s
            }
        }
    }

    fun refreshTables() {
        viewModelScope.launch {
            val result = repository.getTables()
            if (result.isSuccess) {
                _uiState.value = TableMapUiState.Success(result.getOrDefault(emptyList()))
            } else {
                if (_uiState.value !is TableMapUiState.Success) {
                    _uiState.value = TableMapUiState.Error(result.exceptionOrNull()?.message ?: "Unknown Error")
                }
            }
        }
    }

    fun onTableClick(tableId: String, onTableOpened: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.openTable(tableId)
            if (result.isSuccess) {
                onTableOpened(result.getOrThrow())
            }
        }
    }
}

sealed class TableMapUiState {
    object Loading : TableMapUiState()
    data class Success(val tables: List<TableResponse>) : TableMapUiState()
    data class Error(val message: String) : TableMapUiState()
}
