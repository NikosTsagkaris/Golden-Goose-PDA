package com.ntvelop.goldengoosepda.feature_orders.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ntvelop.goldengoosepda.feature_orders.data.OrdersRepository
import com.ntvelop.goldengoosepda.network.CategoryResponse
import com.ntvelop.goldengoosepda.network.OptionResponse
import com.ntvelop.goldengoosepda.network.OrderLineCreateRequest
import com.ntvelop.goldengoosepda.network.OrderResponse
import com.ntvelop.goldengoosepda.network.ProductResponse
import com.ntvelop.goldengoosepda.network.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val repository: OrdersRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _orderState = MutableStateFlow<OrderUiState>(OrderUiState.Loading)
    val orderState = _orderState.asStateFlow()

    private val _draftCart = MutableStateFlow<List<DraftItem>>(emptyList())
    val draftCart = _draftCart.asStateFlow()

    private val _menuCategories = MutableStateFlow<List<CategoryResponse>>(emptyList())
    val menuCategories = _menuCategories.asStateFlow()

    private val _selectedCategory = MutableStateFlow<CategoryResponse?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _customizingProduct = MutableStateFlow<ProductResponse?>(null)
    val customizingProduct = _customizingProduct.asStateFlow()

    private val _dynamicOptions = MutableStateFlow<List<OptionResponse>>(emptyList<OptionResponse>())
    val dynamicOptions = _dynamicOptions.asStateFlow()

    fun loadOrder(orderId: String) {
        viewModelScope.launch {
            _orderState.value = OrderUiState.Loading
            
            // Fetch Menu first if empty
            if (_menuCategories.value.isEmpty()) {
                val menuResult = repository.getMenuCategories()
                if (menuResult.isSuccess) {
                    val cats = menuResult.getOrThrow().toMutableList()
                    
                    // Add hardcoded CUSTOM category if not present
                    if (cats.none { it.name == "CUSTOM" }) {
                        cats.add(CategoryResponse(id = "custom-id", name = "CUSTOM", displayOrder = 99, products = emptyList()))
                    }
                    
                    _menuCategories.value = cats
                    if (_selectedCategory.value == null) {
                        _selectedCategory.value = cats.firstOrNull()
                    }
                }
            }

            val result = repository.getOrder(orderId)
            if (result.isSuccess) {
                _orderState.value = OrderUiState.Success(result.getOrThrow())
            } else {
                _orderState.value = OrderUiState.Error(result.exceptionOrNull()?.message ?: "Error")
            }
        }
    }

    fun selectCategory(category: CategoryResponse) {
        _selectedCategory.value = category
    }

    fun startCustomizing(product: ProductResponse) {
        // Categories that require a customization dialog
        val customizableCategories = listOf("ΚΑΦΕΣ", "ΡΟΦΗΜΑΤΑ", "HOTLY", "Teabox", "ΦΑΓΗΤΟ")
        val currentCat = _selectedCategory.value?.name ?: ""
        
        // Specific exclusions: "Φυσικός χυμός" doesn't need a dialog
        if (product.name?.contains("Φυσικός χυμός", ignoreCase = true) == true) {
            addToDraft(DraftItem(product.id, product.name ?: "Unknown", 1, product.price))
            return
        }

        if (customizableCategories.contains(currentCat) || product.optionGroup >= 0) {
            viewModelScope.launch {
                fetchOptions(product.optionGroup)
                _customizingProduct.value = product
            }
        } else {
            addToDraft(DraftItem(product.id, product.name ?: "Unknown", 1, product.price))
        }
    }

    fun stopCustomizing() {
        _customizingProduct.value = null
    }

    fun addToDraft(item: DraftItem) {
        // We split the quantity into individual items so that each one appears as a separate line
        val newItems = List(item.quantity) {
            item.copy(quantity = 1)
        }
        _draftCart.value += newItems
        _customizingProduct.value = null
    }

    fun removeFromDraft(index: Int) {
        val current = _draftCart.value.toMutableList()
        current.removeAt(index)
        _draftCart.value = current
    }

    fun submitOrder(orderId: String, commonNote: String = "") {
        if (_draftCart.value.isEmpty()) return

        viewModelScope.launch {
            _orderState.value = OrderUiState.Loading
            
            // 1. Send all lines
            val lines = _draftCart.value.map {
                OrderLineCreateRequest(
                    itemId = it.id,
                    productName = it.name,
                    quantity = it.quantity,
                    unitPrice = it.unitPrice,
                    optionsText = it.optionsText,
                    optionsPrice = it.optionsPrice,
                    note = if (it.note.isNullOrBlank()) commonNote else it.note
                )
            }
            
            var allSucceeded = true
            for (line in lines) {
                 val res = repository.addOrderLine(orderId, line)
                 if (res.isFailure) allSucceeded = false
            }
            
            if (allSucceeded) {
                // 2. Submit (Triggers Print)
                val waiterName = settingsManager.getWaiterName()
                val printerIp = settingsManager.getPrinterIp()
                repository.submitOrder(orderId, commonNote, waiterName, printerIp)
                _draftCart.value = emptyList()
                loadOrder(orderId)
            } else {
                _orderState.value = OrderUiState.Error("Some lines failed to sync")
            }
        }
    }

    fun payLine(orderId: String, lineId: String, method: String) {
        viewModelScope.launch {
            val result = repository.payOrderLine(orderId, lineId, method)
            if (result.isSuccess) {
                loadOrder(orderId)
            }
        }
    }

    fun addCustomItem(name: String, price: Double) {
        if (name.isBlank()) return
        addToDraft(DraftItem(id = "custom-item-id", name = name, quantity = 1, unitPrice = price))
    }

    fun fetchOptions(groupCode: Int) {
        if (groupCode < 2) {
            _dynamicOptions.value = emptyList<OptionResponse>()
            return
        }
        viewModelScope.launch {
            val result = repository.getOptions(groupCode)
            if (result.isSuccess) {
                _dynamicOptions.value = result.getOrThrow()
            } else {
                _dynamicOptions.value = emptyList<OptionResponse>()
            }
        }
    }
}

data class DraftItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
    val optionsPrice: Double = 0.0,
    val optionsText: String = "",
    val note: String? = null
)

sealed class OrderUiState {
    object Loading : OrderUiState()
    data class Success(val order: OrderResponse) : OrderUiState()
    data class Error(val message: String) : OrderUiState()
}
