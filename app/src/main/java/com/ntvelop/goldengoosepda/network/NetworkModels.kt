package com.ntvelop.goldengoosepda.network

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("jwt") val jwt: String? = null,
    @SerializedName("jwt_token") val jwtToken: String? = null,
    @SerializedName("session_token") val sessionToken: String? = null,
    @SerializedName("session_id") val sessionId: String? = null,
    @SerializedName("session") val session: String? = null,
    @SerializedName("token_type") val tokenType: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("user_role") val userRole: String? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("user_name") val userName: String? = null,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("pin") val pin: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("user") val user: UserResponse? = null,
    @SerializedName("waiter") val waiter: WaiterInfo? = null
) {
    val effectiveToken: String?
        get() = (accessToken ?: token ?: jwt ?: jwtToken ?: sessionToken ?: sessionId ?: session)
            ?.takeIf { it.isNotBlank() && it != "null" }

    val effectiveRole: String?
        get() = (role ?: userRole ?: user?.role ?: waiter?.role)
            ?.takeIf { it.isNotBlank() && it != "null" } ?: "WAITER"

    val effectiveUserId: String?
        get() = (userId ?: id ?: user?.id ?: waiter?.id)
            ?.takeIf { it.isNotBlank() && it != "null" }
}

data class LoginRequest(
    @SerializedName("pin") val pin: String? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("password") val password: String? = null,
    @SerializedName("hashed_password") val hashedPassword: String? = null
)

data class TableSyncRequest(
    val count: Int = 0
)

data class OpenTableResponse(
    @SerializedName("order_id") val orderId: String = "",
    val status: String = ""
)

data class TableResponse(
    @SerializedName("id") val rawId: Any? = null,
    @SerializedName("number") val number: Any? = null,
    @SerializedName("display_name") val rawDisplayName: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("area") val area: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("is_active") val isActive: Boolean? = true,
    @SerializedName("active_order_id") val activeOrderId: Any? = null,
    @SerializedName("open_order_id") val openOrderId: Any? = null,
    @SerializedName("waiter") val waiter: String? = null,
    @SerializedName("waiter_id") val waiterId: String? = null,
    @SerializedName("waiter_username") val waiterUsername: String? = null,
    @SerializedName("unpaid_total") val unpaidTotal: Double = 0.0
) {
    val resolvedActiveOrderId: String?
        get() = (openOrderId ?: activeOrderId)?.toString()?.takeIf { it.isNotBlank() && it != "null" }
    val safeId: Int
        get() = when (rawId) {
            is Number -> rawId.toInt()
            is String -> rawId.toDoubleOrNull()?.toInt() ?: rawId.toIntOrNull() ?: 0
            else -> 0
        }

    val id: String
        get() = safeId.toString()

    val displayName: String
        get() {
            val numVal: String = when (number) {
                is Double -> number.toInt().toString()
                is Float -> number.toInt().toString()
                is Number -> number.toInt().toString()
                is String -> {
                    val d = number.toDoubleOrNull()
                    if (d != null) d.toInt().toString() else number.trim()
                }
                else -> {
                    val rawStr = (rawDisplayName ?: name)?.trim()
                    if (!rawStr.isNullOrEmpty() && rawStr != "null") {
                        val d = rawStr.toDoubleOrNull()
                        if (d != null) d.toInt().toString() else rawStr
                    } else {
                        safeId.toString()
                    }
                }
            }
            return if (numVal.startsWith("T", ignoreCase = true)) numVal else "T$numVal"
        }

    val displayLabel: String
        get() = displayName
}

typealias TableDto = TableResponse

data class OrderResponse(
    val id: String = "",
    @SerializedName("table_id") val tableId: String? = null,
    @SerializedName("table_number") val tableNumber: Any? = null,
    @SerializedName("waiter_id") val waiterId: String? = null,
    @SerializedName("waiter_name") val waiterName: String? = null,
    val status: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    val table: TableResponse? = null,
    @SerializedName("products") val products: List<OrderLineResponse>? = null,
    @SerializedName("lines") val lines: List<OrderLineResponse> = emptyList()
) {
    val effectiveLines: List<OrderLineResponse>
        get() = lines.ifEmpty { products ?: emptyList() }

    val effectiveWaiterName: String
        get() = (waiterName ?: waiterId)?.takeIf { it.isNotBlank() && it != "null" } ?: "Waiter"

    val displayTableNumber: String
        get() {
            val tblName = table?.displayName
            if (!tblName.isNullOrEmpty() && tblName != "null") return tblName
            val numStr = tableNumber?.toString()?.trim()
            if (!numStr.isNullOrEmpty() && numStr != "null") {
                val d = numStr.toDoubleOrNull()
                val valStr = if (d != null) d.toInt().toString() else numStr
                return if (valStr.startsWith("T", ignoreCase = true)) valStr else "T$numStr"
            }
            val tId = tableId?.trim()
            if (!tId.isNullOrEmpty() && tId != "null") {
                val d = tId.toDoubleOrNull()
                val valStr = if (d != null) d.toInt().toString() else tId
                return "T$valStr"
            }
            return "T$id"
        }

    val unpaidBalance: Double
        get() = effectiveLines.filter { !it.paidStatus }.sumOf { (it.unitPrice * it.quantity) + it.optionsPrice }
}

data class OrderLineResponse(
    @SerializedName("line_id") val lineIdRaw: Any? = null,
    @SerializedName("id") val idRaw: Any? = null,
    @SerializedName("order_id") val orderId: String = "",
    @SerializedName("product_name") val productNameRaw: String? = null,
    @SerializedName("name") val nameRaw: String? = null,
    val quantity: Int = 1,
    @SerializedName("unit_price") val unitPriceRaw: Double? = null,
    @SerializedName("price") val priceRaw: Double? = null,
    @SerializedName("options_text") val optionsText: String = "",
    @SerializedName("options_price") val optionsPrice: Double = 0.0,
    val note: String? = null,
    @SerializedName("paid_status") val paidStatusRaw: Any? = null,
    @SerializedName("paid") val paidRaw: Boolean? = null,
    @SerializedName("payment_method") val paymentMethod: String? = null
) {
    val id: String
        get() {
            val valStr = (lineIdRaw ?: idRaw)?.toString()?.trim()
            if (valStr.isNullOrEmpty() || valStr == "null") return ""
            val d = valStr.toDoubleOrNull()
            return if (d != null) d.toInt().toString() else valStr
        }

    val productName: String
        get() = (productNameRaw ?: nameRaw)?.trim() ?: "Unknown"

    val unitPrice: Double
        get() = unitPriceRaw ?: priceRaw ?: 0.0

    val paidStatus: Boolean
        get() {
            if (paidRaw == true) return true
            if (paidStatusRaw == true) return true
            val str = paidStatusRaw?.toString()?.uppercase()?.trim()
            return str == "PAID" || str == "TRUE" || str == "1"
        }
}

data class OrderLineCreateRequest(
    @SerializedName("item_id") val itemId: String = "",
    @SerializedName("product_name") val productName: String = "",
    val quantity: Int = 1,
    @SerializedName("unit_price") val unitPrice: Double = 0.0,
    @SerializedName("options_json") val optionsJson: Map<String, Any> = emptyMap(),
    @SerializedName("options_text") val optionsText: String = "",
    @SerializedName("options_price") val optionsPrice: Double = 0.0,
    val note: String? = null
)

data class PayLinesRequest(
    @SerializedName("line_ids") val lineIds: List<String> = emptyList(),
    val method: String = "CASH" // CASH / CARD
)

data class PaymentRequest(
    @SerializedName("line_id") val lineId: String = "",
    val amount: Double = 0.0,
    val method: String = "CASH" // CASH / CARD
)

data class PaymentResponse(
    val status: String = "",
    @SerializedName("line_id") val lineId: String = "",
    @SerializedName("payment_method") val paymentMethod: String? = null,
    val paid: Boolean = false
)

data class ProductResponse(
    val id: String = "",
    val name: String? = null,
    val price: Double = 0.0,
    @SerializedName("is_available") val isAvailable: Boolean = true,
    @SerializedName("category_id") val categoryId: String? = null,
    @SerializedName("option_group") val optionGroup: Int = -1
)

data class CategoryResponse(
    val id: String = "",
    val name: String? = null,
    @SerializedName("display_order") val displayOrder: Int = 0,
    val products: List<ProductResponse> = emptyList()
)

data class ShiftTotalsResponse(
    val cash: Double = 0.0,
    val card: Double = 0.0,
    val total: Double = 0.0,
    @SerializedName("order_count") val orderCount: Int = 0
)

data class DeleteResponse(
    val status: String = "",
    @SerializedName("order_id") val orderId: String = ""
)

data class ActionLogResponse(
    val id: String = "",
    @SerializedName("waiter_id") val waiterId: String? = null,
    @SerializedName("action_type") val actionType: String = "",
    val details: String = "",
    @SerializedName("created_at") val createdAt: String = "",
    val waiter: WaiterInfo? = null
)

data class WaiterInfo(
    @SerializedName("id") val id: String? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("role") val role: String? = null
)

data class UserResponse(
    @SerializedName("id") val id: String? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("pin") val pin: String? = null,
    @SerializedName("hashed_password") val hashedPassword: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("is_active") val isActive: Boolean? = null
)

data class WaiterTotalResponse(
    @SerializedName("waiter_name") val waiterName: String = "",
    val cash: Double = 0.0,
    val card: Double = 0.0,
    val total: Double = 0.0,
    @SerializedName("unpaid_amount") val unpaidTotal: Double = 0.0,
    @SerializedName("order_count") val orderCount: Int = 0
)

data class DeviceActivationRequest(
    @SerializedName("device_id") val deviceId: String = "",
    @SerializedName("activation_code") val activationCode: String = ""
)

data class LicenseStatusResponse(
    @SerializedName("device_id") val deviceId: String = "",
    @SerializedName("is_active") val isActive: Boolean = false,
    @SerializedName("expiry_date") val expiryDate: String? = null,
    @SerializedName("days_remaining") val daysRemaining: Int = 0,
    val message: String = ""
)

data class OptionResponse(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0
)
