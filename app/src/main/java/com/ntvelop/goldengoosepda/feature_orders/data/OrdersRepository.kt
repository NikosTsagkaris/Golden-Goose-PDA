package com.ntvelop.goldengoosepda.feature_orders.data

import com.ntvelop.goldengoosepda.network.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrdersRepository @Inject constructor(
    private val apiService: GoldenGooseApiService
) {
    suspend fun getOrder(orderId: String): Result<OrderResponse> {
        return try {
            val response = apiService.getOrder(orderId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Order not found or error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addOrderLine(orderId: String, line: OrderLineCreateRequest): Result<OrderLineResponse> {
        return try {
            val response = apiService.addOrderLine(orderId, line)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to add line: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitOrder(
        orderId: String, 
        note: String? = null, 
        waiterName: String? = null,
        printerIp: String? = null
    ): Result<Unit> {
        return try {
            val response = apiService.submitOrder(orderId, note, waiterName, printerIp)
            if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Submit failed: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOpenOrders(): Result<List<OrderResponse>> {
        return try {
            val response = apiService.getOpenOrders()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else if (response.code() == 422 || response.code() == 404) {
                android.util.Log.w("OrdersRepository", "getOpenOrders returned ${response.code()}. Returning empty list.")
                Result.success(emptyList())
            } else {
                Result.failure(Exception("Failed to fetch open orders: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.w("OrdersRepository", "getOpenOrders exception: ${e.message}")
            Result.success(emptyList())
        }
    }

    suspend fun getPaidOrders(): Result<List<OrderResponse>> {
        return try {
            val response = apiService.getPaidOrders()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else if (response.code() == 422 || response.code() == 404) {
                android.util.Log.w("OrdersRepository", "getPaidOrders returned ${response.code()}. Returning empty list.")
                Result.success(emptyList())
            } else {
                Result.failure(Exception("Failed to fetch paid orders: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.w("OrdersRepository", "getPaidOrders exception: ${e.message}")
            Result.success(emptyList())
        }
    }

    suspend fun payOrderLine(orderId: String, lineId: String, paymentMethod: String): Result<Unit> {
        return payOrderLines(orderId, listOf(lineId), paymentMethod)
    }

    suspend fun payOrderLines(orderId: String, lineIds: List<String>, paymentMethod: String): Result<Unit> {
        return try {
            val request = PayLinesRequest(
                lineIds = lineIds,
                method = paymentMethod
            )
            val response = apiService.payOrderLines(orderId, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Payment failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMenuCategories(): Result<List<CategoryResponse>> {
        return try {
            val response = apiService.getCategories()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch menu: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteOrder(orderId: String): Result<Unit> {
        return try {
            val response = apiService.deleteOrder(orderId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete order: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun openTable(tableId: String): Result<String> {
        return try {
            val response = apiService.openTable(tableId)
            if (response.isSuccessful && response.body() != null) {
                val returnedOrderId = response.body()!!.orderId
                android.util.Log.d("OrdersRepository", "openTable for tableId '$tableId' returned real orderId: '$returnedOrderId'")
                Result.success(if (!returnedOrderId.isNullOrEmpty()) returnedOrderId else tableId)
            } else {
                android.util.Log.w("OrdersRepository", "openTable returned HTTP ${response.code()} for tableId: '$tableId'")
                Result.failure(Exception("Failed to open table: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("OrdersRepository", "openTable exception for tableId: '$tableId'", e)
            Result.failure(e)
        }
    }

    suspend fun getOptions(groupCode: Int): Result<List<OptionResponse>> {
        return try {
            val response = apiService.getOptions(groupCode)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch options: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
