package com.ntvelop.goldengoosepda.network

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("goldengoose_settings", Context.MODE_PRIVATE)

    fun getWaiterName(): String? {
        return prefs.getString("waiter_name", null)
    }

    fun setWaiterName(name: String) {
        prefs.edit().putString("waiter_name", name).apply()
    }

    fun getServerIp(): String {
        return prefs.getString("server_ip", "192.168.1.50") ?: "192.168.1.50"
    }

    fun setServerIp(ip: String) {
        prefs.edit().putString("server_ip", ip).apply()
    }

    fun getPrinterIp(): String {
        return prefs.getString("printer_ip", "192.168.1.100") ?: "192.168.1.100"
    }

    fun setPrinterIp(ip: String) {
        prefs.edit().putString("printer_ip", ip).apply()
    }
}
