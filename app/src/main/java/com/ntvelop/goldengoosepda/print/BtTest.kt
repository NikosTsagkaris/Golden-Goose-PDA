@file:Suppress("MissingPermission")   // <-- ΠΡΟΣΘΕΣΕ ΤΟ
package com.ntvelop.goldengoosepda.print


import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

private val SPP_UUID: UUID =
    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

suspend fun testBtKeepAlive(
    mac: String,
    keepAliveSeconds: Int = 10
): Result<Unit> = withContext(Dispatchers.IO) {
    try {
        val adapter = BluetoothAdapter.getDefaultAdapter()
            ?: return@withContext Result.failure(IllegalStateException("No BT adapter"))
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
            { device.createInsecureRfcommSocketToServiceRecord(SPP_UUID) },
            { device.createRfcommSocketToServiceRecord(SPP_UUID) },
            {
                val m = device.javaClass.getMethod("createInsecureRfcommSocket", Int::class.javaPrimitiveType)
                m.invoke(device, 1) as BluetoothSocket
            },
            {
                val m = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                m.invoke(device, 1) as BluetoothSocket
            }
        )

        var lastErr: Exception? = null
        for (build in attempts) {
            var socket: BluetoothSocket? = null
            try {
                socket = build()
                socket.connect()

                val out = socket.outputStream
                val input = socket.inputStream

                out.write(byteArrayOf(0x1B, 0x40))
                out.write("HELLO BARISTA\n".toByteArray(Charsets.UTF_8))
                out.flush()

                val start = System.currentTimeMillis()
                val buf = ByteArray(1024)
                var lastBeat = 0L
                while (System.currentTimeMillis() - start < keepAliveSeconds * 1000L) {
                    if (System.currentTimeMillis() - lastBeat > 2000) {
                        out.write("PING\r\n".toByteArray()); out.flush()
                        lastBeat = System.currentTimeMillis()
                    }
                    if (input.available() > 0) {
                        val n = input.read(buf)
                        if (n <= 0) break
                    }
                    Thread.sleep(50)
                }

                out.write(byteArrayOf(0x1B, 0x64, 0x01)); out.flush()
                socket.close()
                return@withContext Result.success(Unit)
            } catch (e: Exception) {
                lastErr = e
                try { socket?.close() } catch (_: Exception) {}
            }
        }
        Result.failure(lastErr ?: Exception("Unknown BT error"))
    } catch (se: SecurityException) {
        // Αν δεν έχουν δοθεί οι BLUETOOTH_CONNECT/SCAN
        Result.failure(IllegalStateException("Bluetooth permission missing (CONNECT/SCAN). Grant it and retry.", se))
    }
}
