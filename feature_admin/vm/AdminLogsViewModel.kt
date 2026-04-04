package com.ntvelop.goldengoosepda.feature_admin.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ntvelop.goldengoosepda.feature_admin.data.AdminRepository
import com.ntvelop.goldengoosepda.network.ActionLogResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminLogsViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    private val _logs = MutableStateFlow<List<ActionLogResponse>>(emptyList())
    val logs: StateFlow<List<ActionLogResponse>> = _logs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadLogs()
    }

    fun loadLogs() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getActionLogs()
                .onSuccess { _logs.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.clearLogs()
                .onSuccess { loadLogs() }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }
}
