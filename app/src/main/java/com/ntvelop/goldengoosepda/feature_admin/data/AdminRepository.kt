package com.ntvelop.goldengoosepda.feature_admin.data

import com.ntvelop.goldengoosepda.network.ActionLogResponse
import com.ntvelop.goldengoosepda.network.GoldenGooseApiService
import com.ntvelop.goldengoosepda.network.WaiterTotalResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val apiService: GoldenGooseApiService
) {
    suspend fun getWaiterTotals(): Result<List<WaiterTotalResponse>> {
        return try {
            val response = apiService.getWaiterTotals()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else if (response.code() == 404) {
                android.util.Log.w("AdminRepository", "getWaiterTotals endpoint returned 404. Returning empty list.")
                Result.success(emptyList())
            } else {
                Result.failure(Exception("Error fetching waiter totals: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.w("AdminRepository", "getWaiterTotals exception: ${e.message}")
            Result.success(emptyList())
        }
    }

    suspend fun getActionLogs(): Result<List<ActionLogResponse>> {
        return try {
            val response = apiService.getActionLogs()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else if (response.code() == 404) {
                android.util.Log.w("AdminRepository", "getActionLogs endpoint returned 404. Returning empty list.")
                Result.success(emptyList())
            } else {
                Result.failure(Exception("Error fetching logs: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.w("AdminRepository", "getActionLogs exception: ${e.message}")
            Result.success(emptyList())
        }
    }

    suspend fun clearLogs(): Result<Unit> {
        return try {
            val response = apiService.clearLogs()
            if (response.isSuccessful || response.code() == 404) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error clearing logs: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    suspend fun syncTables(count: Int): Result<Unit> {
        return try {
            val response = apiService.syncTables(com.ntvelop.goldengoosepda.network.TableSyncRequest(count = count))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Sync failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
