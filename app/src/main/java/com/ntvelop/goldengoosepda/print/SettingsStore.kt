package com.ntvelop.goldengoosepda.print

import android.content.Context


object SettingsStore {
    private const val PREFS = "printer_prefs"
    private const val KEY_MAC = "printer_mac"
    private const val KEY_WAITER = "waiter_name"

    fun saveMac(ctx: Context, mac: String) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_MAC, mac).apply()
    }

    fun getMac(ctx: Context): String? {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_MAC, null)
    }

    // ---- NEW: waiter name (πάντα ΚΕΦΑΛΑΙΑ + max 10) ----
    fun saveWaiter(ctx: Context, rawName: String) {
        val cleaned = rawName.uppercase().take(10)
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_WAITER, cleaned).apply()
    }

    fun getWaiter(ctx: Context): String? {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_WAITER, null)
    }
}
