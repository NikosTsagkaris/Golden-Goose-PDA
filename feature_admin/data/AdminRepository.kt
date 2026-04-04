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
            } else {
                Result.failure(Exception("Error fetching waiter totals: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActionLogs(): Result<List<ActionLogResponse>> {
        return try {
            val response = apiService.getActionLogs()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error fetching logs: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearLogs(): Result<Unit> {
        return try {
            val response = apiService.clearLogs()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error clearing logs: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
