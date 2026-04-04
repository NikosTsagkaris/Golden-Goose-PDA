package com.ntvelop.goldengoosepda.network

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    val role: String,
    val username: String,
    @SerializedName("user_id") val userId: String
)

data class LoginRequest(
    val pin: String
)

data class TableSyncRequest(
    val count: Int
)

data class OpenTableResponse(
    @SerializedName("order_id") val orderId: String,
    val status: String
)

data class TableResponse(
    val id: String,
    @SerializedName("display_name") val name: String?,
    val area: String?,
    val status: String?,
    @SerializedName("active_order_id") val activeOrderId: String?,
    @SerializedName("waiter_id") val waiterId: String? = null,
    @SerializedName("waiter_username") val waiterUsername: String? = null,
    @SerializedName("unpaid_total") val unpaidTotal: Double = 0.0
)

data class OrderResponse(
    val id: String,
    @SerializedName("table_id") val tableId: String?,
    @SerializedName("waiter_id") val waiterId: String?,
    val status: String?,
    @SerializedName("created_at") val createdAt: String?,
    val table: TableResponse? = null,
    val lines: List<OrderLineResponse> = emptyList()
)

data class OrderLineResponse(
    val id: String,
    @SerializedName("order_id") val orderId: String,
    @SerializedName("product_name") val productName: String,
    val quantity: Int,
    @SerializedName("unit_price") val unitPrice: Double,
    @SerializedName("options_text") val optionsText: String,
    @SerializedName("options_price") val optionsPrice: Double,
    val note: String?,
    @SerializedName("paid_status") val paidStatus: Boolean,
    @SerializedName("payment_method") val paymentMethod: String? = null
)

data class OrderLineCreateRequest(
    @SerializedName("item_id") val itemId: String = "",
    @SerializedName("product_name") val productName: String,
    val quantity: Int,
    @SerializedName("unit_price") val unitPrice: Double,
    @SerializedName("options_json") val optionsJson: Map<String, Any> = emptyMap(),
    @SerializedName("options_text") val optionsText: String = "",
    @SerializedName("options_price") val optionsPrice: Double = 0.0,
    val note: String? = null
)

data class PayLinesRequest(
    @SerializedName("line_ids") val lineIds: List<String>,
    val method: String // CASH / CARD
)

data class PaymentRequest(
    @SerializedName("line_id") val lineId: String,
    val amount: Double,
    val method: String // CASH / CARD
)

data class PaymentResponse(
    val status: String,
    @SerializedName("line_id") val lineId: String,
    @SerializedName("payment_method") val paymentMethod: String,
    val paid: Boolean
)

data class ProductResponse(
    val id: String,
    val name: String?,
    val price: Double = 0.0,
    @SerializedName("is_available") val isAvailable: Boolean = true,
    @SerializedName("category_id") val categoryId: String? = null,
    @SerializedName("option_group") val optionGroup: Int = -1
)

data class CategoryResponse(
    val id: String,
    val name: String?,
    @SerializedName("display_order") val displayOrder: Int = 0,
    val products: List<ProductResponse> = emptyList()
)

data class ShiftTotalsResponse(
    val cash: Double,
    val card: Double,
    val total: Double,
    @SerializedName("order_count") val orderCount: Int
)

data class DeleteResponse(
    val status: String,
    @SerializedName("order_id") val orderId: String
)

data class ActionLogResponse(
    val id: String,
    @SerializedName("waiter_id") val waiterId: String?,
    @SerializedName("action_type") val actionType: String,
    val details: String,
    @SerializedName("created_at") val createdAt: String,
    val waiter: WaiterInfo?
)

data class WaiterInfo(
    val username: String
)

data class WaiterTotalResponse(
    @SerializedName("waiter_name") val waiterName: String,
    val cash: Double,
    val card: Double,
    val total: Double,
    @SerializedName("unpaid_amount") val unpaidTotal: Double,
    @SerializedName("order_count") val orderCount: Int
)

data class DeviceActivationRequest(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("activation_code") val activationCode: String
)

data class LicenseStatusResponse(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("expiry_date") val expiryDate: String?,
    @SerializedName("days_remaining") val daysRemaining: Int = 0,
    val message: String
)

data class OptionResponse(
    val id: String,
    val name: String,
    val price: Double
)
