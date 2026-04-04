package com.ntvelop.goldengoosepda

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

private val SPP_UUID: UUID =
    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

/**
 * Προσπαθεί να συνδεθεί και να κρατήσει τη σύνδεση “ζωντανή” για N δευτ.
 * Δοκιμάζει 4 τρόπους (insecure/secure + reflection ch1).
 */
