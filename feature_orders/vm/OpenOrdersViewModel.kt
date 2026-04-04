package com.ntvelop.goldengoosepda.feature_orders.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ntvelop.goldengoosepda.feature_orders.data.OrdersRepository
import com.ntvelop.goldengoosepda.network.OrderResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OpenOrdersViewModel @Inject constructor(
    private val repository: OrdersRepository
) : ViewModel() {

    private val _orders = MutableStateFlow<List<OrderResponse>>(emptyList())
    val orders: StateFlow<List<OrderResponse>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getOpenOrders()
                .onSuccess { _orders.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun payLine(orderId: String, lineId: String, paymentMethod: String) {
        viewModelScope.launch {
            repository.payOrderLine(orderId, lineId, paymentMethod)
                .onSuccess { loadOrders() } // Refresh list after payment
                .onFailure { _error.value = it.message }
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
