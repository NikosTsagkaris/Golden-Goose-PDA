package com.ntvelop.goldengoosepda.print

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.charset.Charset
import java.util.UUID
import android.annotation.SuppressLint

/**
 * Classic Bluetooth SPP (RFCOMM) για ESC/POS.
 * Δοκιμάζει 4 τρόπους σύνδεσης (insecure/secure + reflection channel 1).
 * Κάνει init ESC @, ορίζει code page με ESC t, γράφει, feed & cut.
 */
object PrinterService {

    // SPP UUID (RFCOMM)
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    @SuppressLint("MissingPermission")
    suspend fun printText(
        mac: String,
        text: String,
        codePage: CodePage = CodePage.WIN1253      // default Ελληνικά Windows
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val adapter = BluetoothAdapter.getDefaultAdapter()
            ?: return@withContext Result.failure(IllegalStateException("No Bluetooth adapter"))
        if (!adapter.isEnabled) {
            return@withContext Result.failure(IllegalStateException("Bluetooth is OFF"))
        }

        val device: BluetoothDevice = try {
            adapter.getRemoteDevice(mac)
        } catch (e: IllegalArgumentException) {
            return@withContext Result.failure(IllegalArgumentException("Bad MAC: $mac", e))
        }

        if (adapter.isDiscovering) adapter.cancelDiscovery()

        val attempts: List<() -> BluetoothSocket> = listOf(
            { device.createInsecureRfcommSocketToServiceRecord(SPP_UUID) }, // 1
            { device.createRfcommSocketToServiceRecord(SPP_UUID) },         // 2
            { // 3 reflection (insecure) ch 1
                val m = device.javaClass.getMethod(
                    "createInsecureRfcommSocket",
                    Int::class.javaPrimitiveType
                ); m.invoke(device, 1) as BluetoothSocket
            },
            { // 4 reflection (secure) ch 1
                val m = device.javaClass.getMethod(
                    "createRfcommSocket",
                    Int::class.javaPrimitiveType
                ); m.invoke(device, 1) as BluetoothSocket
            }
        )

        var lastErr: Exception? = null
        for (build in attempts) {
            var s: BluetoothSocket? = null
            try {
                s = build()
                s.connect()

                val out = s.outputStream
                // --- ESC/POS init
                out.write(byteArrayOf(0x1B, 0x40))               // ESC @ (initialize)
                out.write(byteArrayOf(0x1B, 0x74, codePage.escNo))// ESC t n (code page)

                // Με την αντίστοιχη Java Charset ώστε τα bytes να ταιριάζουν με το code page
                val charset: Charset = when (codePage) {
                    CodePage.WIN1253 -> Charset.forName("windows-1253")
                    CodePage.CP737   -> Charset.forName("CP737")
                    CodePage.CP869   -> Charset.forName("CP869")
                }

                out.write(text.toByteArray(charset))
                out.write(byteArrayOf(0x0A, 0x0A))               // 2x LF
                out.write(byteArrayOf(0x1B, 0x64, 0x02))          // feed 2 lines
                // Αν υποστηρίζεται μερικό κόψιμο:
                out.write(byteArrayOf(0x1D, 0x56, 0x41, 0x03))    // GS V 65 3

                out.flush()
                // μικρή αναμονή ώστε να μην κλείσει πρόωρα το socket
                Thread.sleep(150)
                s.close()
                return@withContext Result.success(Unit)
            } catch (e: Exception) {
                lastErr = e
                try { s?.close() } catch (_: Exception) {}
            }
        }
        Result.failure(lastErr ?: Exception("Unknown BT error"))
    }
}

/** Code pages όπως εμφανίζονται στο self-test της συσκευής */
enum class CodePage(val escNo: Byte) {
    // Από το χαρτί σου:
    // 64: PC737(Greek), 66: PC869(Greek), 90: WPC1253
    CP737(64),
    CP869(66),
    WIN1253(90)
}
