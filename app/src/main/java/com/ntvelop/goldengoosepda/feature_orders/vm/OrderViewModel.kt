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
            _draftCart.value = emptyList()
            
            // Always ensure Menu categories are loaded and selectedCategory is set
            if (_menuCategories.value.isEmpty()) {
                val menuResult = repository.getMenuCategories()
                if (menuResult.isSuccess) {
                    val cats = menuResult.getOrThrow().toMutableList()
                    if (cats.none { it.name == "CUSTOM" }) {
                        cats.add(CategoryResponse(id = "custom-id", name = "CUSTOM", displayOrder = 99, products = emptyList()))
                    }
                    _menuCategories.value = cats
                    _selectedCategory.value = cats.firstOrNull()
                } else {
                    android.util.Log.w("OrderViewModel", "Failed to fetch menu categories: ${menuResult.exceptionOrNull()?.message}")
                }
            } else if (_selectedCategory.value == null) {
                _selectedCategory.value = _menuCategories.value.firstOrNull()
            }

            val result = repository.getOrder(orderId)
            if (result.isSuccess) {
                _orderState.value = OrderUiState.Success(result.getOrThrow())
            } else {
                android.util.Log.w("OrderViewModel", "getOrder returned error for orderId '$orderId'. Applying fallback empty OrderResponse for table.")
                val fallbackOrder = OrderResponse(
                    id = orderId,
                    tableId = orderId,
                    status = "OPEN",
                    lines = emptyList()
                )
                _orderState.value = OrderUiState.Success(fallbackOrder)
            }
        }
    }

    fun selectCategory(category: CategoryResponse) {
        _selectedCategory.value = category
    }

    fun startCustomizing(product: ProductResponse) {
        val currentCat = _selectedCategory.value?.name ?: ""
        
        // Specific exclusions: "Φυσικός χυμός" doesn't need a dialog
        if (product.name?.contains("Φυσικός χυμός", ignoreCase = true) == true) {
            addToDraft(DraftItem(product.id, product.name ?: "Unknown", 1, product.price))
            return
        }

        val isCoffeeProduct = currentCat.contains("ΚΑΦΕ", ignoreCase = true) ||
                product.optionGroup == 1 ||
                product.name?.contains("Espresso", ignoreCase = true) == true ||
                product.name?.contains("Cappuccino", ignoreCase = true) == true ||
                product.name?.contains("Freddo", ignoreCase = true) == true ||
                product.name?.contains("Nes", ignoreCase = true) == true ||
                product.name?.contains("Φραπέ", ignoreCase = true) == true ||
                product.name?.contains("Ελληνικός", ignoreCase = true) == true ||
                product.name?.contains("Latte", ignoreCase = true) == true ||
                product.name?.contains("Americano", ignoreCase = true) == true ||
                product.name?.contains("Φίλτρου", ignoreCase = true) == true ||
                product.name?.contains("Filter", ignoreCase = true) == true ||
                product.name?.contains("Καφέ", ignoreCase = true) == true ||
                product.name?.contains("Καφές", ignoreCase = true) == true

        val isCustomizable = isCoffeeProduct ||
                currentCat.contains("ΡΟΦΗΜΑΤΑ", ignoreCase = true) ||
                currentCat.contains("ΣΟΚΟΛΑΤΑ", ignoreCase = true) ||
                currentCat.contains("HOTLY", ignoreCase = true) ||
                currentCat.contains("Teabox", ignoreCase = true) ||
                currentCat.contains("ΦΑΓΗΤΟ", ignoreCase = true) ||
                currentCat.contains("SNACKS", ignoreCase = true) ||
                currentCat.contains("BRUNCH", ignoreCase = true) ||
                currentCat.contains("ΣΑΛΑΤΕΣ", ignoreCase = true) ||
                currentCat.contains("ΠΙΤΣΕΣ", ignoreCase = true) ||
                currentCat.contains("ΠΟΙΚΙΛΙΕΣ", ignoreCase = true) ||
                currentCat.contains("ΓΛΥΚΑ", ignoreCase = true) ||
                currentCat.contains("PANCAKES", ignoreCase = true) ||
                currentCat.contains("ΒΑΦΛΕΣ", ignoreCase = true) ||
                currentCat.contains("GIAGIAMAS", ignoreCase = true) ||
                currentCat.contains("ΓΙΑΓΙΑΜΑΣ", ignoreCase = true) ||
                product.name?.contains("Σοκολάτα", ignoreCase = true) == true ||
                product.name?.contains("Chocolate", ignoreCase = true) == true ||
                product.name?.contains("Mojito", ignoreCase = true) == true ||
                product.name?.contains("Corona", ignoreCase = true) == true ||
                product.name?.contains("Κορώνα", ignoreCase = true) == true ||
                product.name?.contains("BARISTA PRO", ignoreCase = true) == true ||
                product.name?.contains("Amita", ignoreCase = true) == true ||
                product.name?.contains("Schweppes", ignoreCase = true) == true ||
                product.name?.contains("Γρανίτα", ignoreCase = true) == true ||
                product.name?.contains("Granita", ignoreCase = true) == true ||
                product.name?.contains("Τσάι", ignoreCase = true) == true ||
                product.name?.contains("ΤΣΑΙ", ignoreCase = true) == true ||
                product.name?.contains("Tea", ignoreCase = true) == true ||
                product.name?.contains("Milkshake", ignoreCase = true) == true ||
                product.name?.contains("ΠΑΓΩΤΟ", ignoreCase = true) == true ||
                product.name?.contains("Παγωτό", ignoreCase = true) == true ||
                product.name?.contains("180ml", ignoreCase = true) == true ||
                product.optionGroup >= 0

        if (isCustomizable) {
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

    fun submitOrder(orderId: String, commonNote: String = "", onSuccess: (() -> Unit)? = null) {
        if (_draftCart.value.isEmpty()) return

        viewModelScope.launch {
            _orderState.value = OrderUiState.Loading

            // 1. Ensure an active order record exists in table 'orders'
            var targetOrderId = orderId
            android.util.Log.d("OrderViewModel", "Submitting order lines. Initial targetOrderId passed: '$targetOrderId'")

            val openRes = repository.openTable(targetOrderId)
            if (openRes.isSuccess) {
                val realId = openRes.getOrNull()
                if (!realId.isNullOrBlank()) {
                    targetOrderId = realId
                    android.util.Log.d("OrderViewModel", "Active order_id in table 'orders' resolved: '$targetOrderId'")
                }
            } else {
                android.util.Log.w("OrderViewModel", "openTable failed before submitting lines, proceeding with '$targetOrderId'")
            }

            // 2. Send all lines to POST /orders/{targetOrderId}/lines
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
                val res = repository.addOrderLine(targetOrderId, line)
                if (res.isFailure) {
                    android.util.Log.e("OrderViewModel", "Failed to add order line to order '$targetOrderId'", res.exceptionOrNull())
                    allSucceeded = false
                }
            }

            if (allSucceeded) {
                // 3. Submit order (Triggers Print & Occupies Table)
                val waiterName = settingsManager.getWaiterName()
                val printerIp = settingsManager.getPrinterIp()
                repository.submitOrder(targetOrderId, commonNote, waiterName, printerIp)

                if (!printerIp.isNullOrBlank()) {
                    val ticketText = buildOrderTicketText(targetOrderId, waiterName, lines, commonNote)
                    com.ntvelop.goldengoosepda.print.PrinterService.printViaIp(printerIp, 9100, ticketText)
                }

                _draftCart.value = emptyList()
                onSuccess?.invoke()
                loadOrder(targetOrderId)
            } else {
                _orderState.value = OrderUiState.Error("Failed to sync items for order #$targetOrderId")
            }
        }
    }

    private fun buildOrderTicketText(
        orderId: String,
        waiterName: String?,
        lines: List<OrderLineCreateRequest>,
        note: String?
    ): String {
        val sb = StringBuilder()
        sb.append("================================\n")
        sb.append("      GOLDEN GOOSE POS\n")
        sb.append("================================\n")
        sb.append("ΠΑΡΑΓΓΕΛΙΑ #$orderId\n")
        sb.append("Σερβιτόρος: ${waiterName ?: "—"}\n")
        val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        sb.append("Ημερομηνία: $dateStr\n")
        sb.append("--------------------------------\n")
        for (l in lines) {
            sb.append("${l.quantity}x ${l.productName}\n")
            if (l.optionsText.isNotBlank()) {
                sb.append("   [ ${l.optionsText} ]\n")
            }
            if (!l.note.isNullOrBlank()) {
                sb.append("   * Σημείωση: ${l.note}\n")
            }
        }
        if (!note.isNullOrBlank()) {
            sb.append("--------------------------------\n")
            sb.append("ΣΗΜΕΙΩΣΗ: $note\n")
        }
        sb.append("================================\n")
        return sb.toString()
    }

    fun payFullOrder(orderId: String, method: String, onSuccess: (() -> Unit)? = null, onAllPaid: (() -> Unit)? = null) {
        viewModelScope.launch {
            val currentOrder = (_orderState.value as? OrderUiState.Success)?.order
            val unpaidLines = currentOrder?.lines?.filter { !it.paidStatus } ?: emptyList()
            val lineIds = unpaidLines.map { it.id }.filter { it.isNotBlank() }

            val result = repository.payOrderLines(orderId, lineIds, method)
            if (result.isSuccess) {
                onSuccess?.invoke()
                loadOrder(orderId)
                val refreshedOrder = (_orderState.value as? OrderUiState.Success)?.order
                if (refreshedOrder != null && (refreshedOrder.lines.isEmpty() || refreshedOrder.unpaidBalance <= 0)) {
                    onAllPaid?.invoke()
                }
            } else {
                android.util.Log.e("OrderViewModel", "Full order payment failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun payLine(orderId: String, lineId: String, method: String, onSuccess: (() -> Unit)? = null, onAllPaid: (() -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.payOrderLine(orderId, lineId, method)
            if (result.isSuccess) {
                onSuccess?.invoke()
                loadOrder(orderId)
                val refreshedOrder = (_orderState.value as? OrderUiState.Success)?.order
                if (refreshedOrder != null && (refreshedOrder.lines.isNotEmpty() && refreshedOrder.unpaidBalance <= 0)) {
                    onAllPaid?.invoke()
                }
            } else {
                android.util.Log.e("OrderViewModel", "Line payment failed: ${result.exceptionOrNull()?.message}")
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
