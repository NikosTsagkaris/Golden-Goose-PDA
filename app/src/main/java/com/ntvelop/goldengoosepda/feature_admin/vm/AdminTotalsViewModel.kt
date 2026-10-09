package com.ntvelop.goldengoosepda.feature_admin.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ntvelop.goldengoosepda.feature_admin.data.AdminRepository
import com.ntvelop.goldengoosepda.feature_shifts.data.ShiftsRepository
import com.ntvelop.goldengoosepda.network.ShiftTotalsResponse
import com.ntvelop.goldengoosepda.network.WaiterTotalResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminTotalsViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val shiftRepository: ShiftsRepository
) : ViewModel() {

    val globalTotals: StateFlow<ShiftTotalsResponse?> = shiftRepository.totals

    private val _waiterTotals = MutableStateFlow<List<WaiterTotalResponse>>(emptyList())
    val waiterTotals: StateFlow<List<WaiterTotalResponse>> = _waiterTotals.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadAll()
        
        // Observe shared totals to clear waiter breakdown on shift end
        viewModelScope.launch {
            shiftRepository.totals.collect { totals ->
                if (totals != null && totals.total == 0.0 && totals.orderCount == 0) {
                    _waiterTotals.value = emptyList()
                }
            }
        }
    }

    fun loadAll() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // Fetch global totals - this updates the shared repo flow
            shiftRepository.getShiftTotals()
                .onFailure { _error.value = it.message }
            
            // Fetch waiter breakdown
            adminRepository.getWaiterTotals()
                .onSuccess { _waiterTotals.value = it }
                .onFailure { _error.value = it.message }
            
            _isLoading.value = false
        }
    }
}
