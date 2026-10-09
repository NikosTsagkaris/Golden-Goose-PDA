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

                val formattedBytes = formatEscposPayload(text, charset)
                out.write(formattedBytes)
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

    suspend fun printViaIp(
        ip: String,
        port: Int = 9100,
        text: String,
        codePage: CodePage = CodePage.WIN1253
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (ip.isBlank()) return@withContext Result.failure(IllegalArgumentException("Printer IP is empty"))
        try {
            val socket = java.net.Socket()
            socket.connect(java.net.InetSocketAddress(ip, port), 4000)
            val out = socket.outputStream

            // ESC/POS init
            out.write(byteArrayOf(0x1B, 0x40))               // ESC @ (initialize)
            out.write(byteArrayOf(0x1B, 0x74, codePage.escNo))// ESC t n (code page)

            val charset: Charset = when (codePage) {
                CodePage.WIN1253 -> Charset.forName("windows-1253")
                CodePage.CP737   -> Charset.forName("CP737")
                CodePage.CP869   -> Charset.forName("CP869")
            }

            val formattedBytes = formatEscposPayload(text, charset)
            out.write(formattedBytes)
            out.write(byteArrayOf(0x0A, 0x0A))
            out.write(byteArrayOf(0x1B, 0x64, 0x02))          // feed 2 lines
            out.write(byteArrayOf(0x1D, 0x56, 0x41, 0x03))    // GS V 65 3 cut
            out.flush()
            Thread.sleep(150)
            socket.close()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatEscposPayload(text: String, charset: Charset): ByteArray {
        val stream = java.io.ByteArrayOutputStream()
        // ESC @ (Init) + Bold ON
        stream.write(byteArrayOf(0x1B, 0x40, 0x1B, 0x45, 0x01))
        
        val lines = text.split("\n")
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() && !line.startsWith("[")) {
                stream.write("\n".toByteArray(charset))
                continue
            }
            
            val cleanLine = line.replace("[LB]", "").replace("[B]", "")
            
            val sizeCmd: ByteArray = when {
                // Header / Order # / Table # / Shift Totals -> 2x2 size
                line.startsWith("[LB]") || cleanLine.contains("ΠΑΡΑΓΓΕΛΙΑ", ignoreCase = true) || cleanLine.contains("ΤΡΑΠΕΖΙ", ignoreCase = true) || cleanLine.contains("ΣΥΝΟΛΟ", ignoreCase = true) || cleanLine.startsWith("===") -> {
                    byteArrayOf(0x1D, 0x21, 0x11.toByte()) // 2x2
                }
                // Items / Quantities / Prices -> 2x Height
                line.startsWith("[B]") || cleanLine.contains("x ", ignoreCase = true) || cleanLine.contains("€") || cleanLine.startsWith("---") -> {
                    byteArrayOf(0x1D, 0x21, 0x01.toByte()) // 2x Height
                }
                // Options / Details / Notes -> Normal size
                else -> {
                    byteArrayOf(0x1D, 0x21, 0x00.toByte()) // Normal
                }
            }
            
            stream.write(sizeCmd)
            stream.write(cleanLine.toByteArray(charset))
            stream.write("\n".toByteArray(charset))
        }
        
        return stream.toByteArray()
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
