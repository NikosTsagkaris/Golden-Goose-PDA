package com.ntvelop.goldengoosepda.feature_orders.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ntvelop.goldengoosepda.feature_orders.data.OrdersRepository
import com.ntvelop.goldengoosepda.feature_shifts.data.ShiftsRepository
import com.ntvelop.goldengoosepda.network.OrderResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaidOrdersViewModel @Inject constructor(
    private val repository: OrdersRepository,
    private val shiftRepository: ShiftsRepository
) : ViewModel() {

    private val _orders = MutableStateFlow<List<OrderResponse>>(emptyList())
    val orders: StateFlow<List<OrderResponse>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadOrders()
        
        // Clear list when shift ends
        viewModelScope.launch {
            shiftRepository.totals.collect { totals ->
                if (totals != null && totals.total == 0.0 && totals.orderCount == 0) {
                    _orders.value = emptyList()
                }
            }
        }
    }

    fun loadOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getPaidOrders()
                .onSuccess { _orders.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun deleteOrder(orderId: String) {
        viewModelScope.launch {
            repository.deleteOrder(orderId)
                .onSuccess { loadOrders() } // Refresh list after deletion
                .onFailure { _error.value = it.message }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
