package com.ntvelop.goldengoosepda.feature_shifts.data

import com.ntvelop.goldengoosepda.network.GoldenGooseApiService
import com.ntvelop.goldengoosepda.network.ShiftTotalsResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShiftsRepository @Inject constructor(
    private val apiService: GoldenGooseApiService
) {
    private val _totals = MutableStateFlow<ShiftTotalsResponse?>(null)
    val totals: StateFlow<ShiftTotalsResponse?> = _totals.asStateFlow()

    suspend fun getShiftTotals(): Result<ShiftTotalsResponse> {
        return try {
            val response = apiService.getShiftTotals()
            if (response.isSuccessful && response.body() != null) {
                val totals = response.body()!!
                _totals.value = totals
                Result.success(totals)
            } else {
                Result.failure(Exception("Failed to fetch shift totals"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun endShift(): Result<Unit> {
        return try {
            val response = apiService.endShift()
            if (response.isSuccessful) {
                _totals.value = ShiftTotalsResponse(0.0, 0.0, 0.0, 0)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to end shift"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun printShiftSummary(): Result<Unit> {
        return try {
            val response = apiService.printShiftSummary()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to print summary"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
