package com.ntvelop.goldengoosepda.feature_shifts.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ntvelop.goldengoosepda.feature_shifts.data.ShiftsRepository
import com.ntvelop.goldengoosepda.network.ShiftTotalsResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShiftViewModel @Inject constructor(
    private val repository: ShiftsRepository,
    private val settingsManager: com.ntvelop.goldengoosepda.network.SettingsManager
) : ViewModel() {

    val totals: StateFlow<ShiftTotalsResponse?> = repository.totals

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadTotals()
    }

    fun loadTotals() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getShiftTotals()
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun endShift(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.endShift()
                .onSuccess {
                    loadTotals()
                    onSuccess()
                }
                .onFailure { _error.value = it.message }
        }
    }

    fun printSummary() {
        viewModelScope.launch {
            repository.printShiftSummary()
                .onSuccess { /* Print job queued */ }
                .onFailure { _error.value = it.message }

            val printerIp = settingsManager.getPrinterIp()
            val currentTotals = totals.value
            val waiterName = settingsManager.getWaiterName() ?: "Σερβιτόρος"
            if (!printerIp.isNullOrBlank() && currentTotals != null) {
                val text = buildShiftSummaryText(waiterName, currentTotals.cash, currentTotals.card, currentTotals.total)
                com.ntvelop.goldengoosepda.print.PrinterService.printViaIp(printerIp, 9100, text)
            }
        }
    }

    private fun buildShiftSummaryText(waiterName: String, cash: Double, card: Double, total: Double): String {
        val sb = StringBuilder()
        sb.append("================================\n")
        sb.append("      GOLDEN GOOSE POS\n")
        sb.append("      ΣΥΝΟΛΟ ΒΑΡΔΙΑΣ\n")
        sb.append("================================\n")
        sb.append("Σερβιτόρος: $waiterName\n")
        val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        sb.append("Ημερομηνία: $dateStr\n")
        sb.append("--------------------------------\n")
        sb.append("Μετρητά:       €${String.format("%.2f", cash)}\n")
        sb.append("Κάρτα (POS):   €${String.format("%.2f", card)}\n")
        sb.append("--------------------------------\n")
        sb.append("ΣΥΝΟΛΟ:        €${String.format("%.2f", total)}\n")
        sb.append("================================\n")
        return sb.toString()
    }

    fun clearError() {
        _error.value = null
    }
}
