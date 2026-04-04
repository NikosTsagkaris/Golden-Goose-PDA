package com.ntvelop.goldengoosepda.feature_admin.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ntvelop.goldengoosepda.network.GoldenGooseApiService
import com.ntvelop.goldengoosepda.network.TableSyncRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminSettingsViewModel @Inject constructor(
    private val apiService: GoldenGooseApiService
) : ViewModel() {

    private val _tableCount = MutableStateFlow(0)
    val tableCount = _tableCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    init {
        fetchCurrentTableCount()
    }

    private fun fetchCurrentTableCount() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val res = apiService.getTables()
                if (res.isSuccessful) {
                    _tableCount.value = res.body()?.size ?: 0
                }
            } catch (e: Exception) {
                _error.value = "Failed to load tables: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTableCount(count: Int) {
        _tableCount.value = count
    }

    fun syncTables() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _successMessage.value = null
                val target = _tableCount.value
                val res = apiService.syncTables(TableSyncRequest(target))
                if (res.isSuccessful) {
                    _successMessage.value = "Επιτυχής συγχρονισμός τραπεζιών!"
                    fetchCurrentTableCount() // Refresh
                } else {
                    _error.value = "Σφάλμα συγχρονισμού: ${res.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Αποτυχία συγχρονισμού: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }
}
