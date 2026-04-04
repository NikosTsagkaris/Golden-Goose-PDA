package com.ntvelop.goldengoosepda.feature_setup.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ntvelop.goldengoosepda.network.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _serverIp = MutableStateFlow(settingsManager.getServerIp())
    val serverIp = _serverIp.asStateFlow()

    private val _printerIp = MutableStateFlow(settingsManager.getPrinterIp())
    val printerIp = _printerIp.asStateFlow()

    fun updateIp(newIp: String) {
        _serverIp.value = newIp
    }

    fun updatePrinterIp(newIp: String) {
        _printerIp.value = newIp
    }

    fun saveIp() {
        settingsManager.setServerIp(_serverIp.value)
        settingsManager.setPrinterIp(_printerIp.value)
    }
}
