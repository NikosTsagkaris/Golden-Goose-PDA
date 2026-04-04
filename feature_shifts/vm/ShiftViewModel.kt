package com.ntvelop.goldengoosepda.feature_shifts.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ntvelop.goldengoosepda.feature_shifts.data.ShiftsRepository
import com.ntvelop.goldengoosepda.network.ShiftTotalsResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShiftViewModel @Inject constructor(
    private val repository: ShiftsRepository
) : ViewModel() {

    val totals: StateFlow<ShiftTotalsResponse?> = repository.totals

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadTotals()
    }

    fun loadTotals() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getShiftTotals()
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun endShift(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.endShift()
                .onSuccess {
                    onSuccess()
                }
                .onFailure { _error.value = it.message }
        }
    }

    fun printSummary() {
        viewModelScope.launch {
            repository.printShiftSummary()
                .onSuccess { /* Print job queued */ }
                .onFailure { _error.value = it.message }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
