package com.ntvelop.goldengoosepda.feature_tables.data

import com.ntvelop.goldengoosepda.network.GoldenGooseApiService
import com.ntvelop.goldengoosepda.network.TableResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TablesRepository @Inject constructor(
    private val apiService: GoldenGooseApiService
) {
    suspend fun getTables(): Result<List<TableResponse>> {
        return try {
            val response = apiService.getTables()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch tables: ${response.code()}"))
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
                Result.success(if (!returnedOrderId.isNullOrEmpty()) returnedOrderId else tableId)
            } else {
                android.util.Log.w("TablesRepository", "openTable returned HTTP ${response.code()} for tableId: '$tableId'. Falling back to tableId.")
                Result.success(tableId)
            }
        } catch (e: Exception) {
            android.util.Log.w("TablesRepository", "openTable exception for tableId: '$tableId': ${e.message}. Falling back to tableId.")
            Result.success(tableId)
        }
    }
}
