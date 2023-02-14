package com.linksrussia.rgk_worker.util

import android.bluetooth.BluetoothSocket
import android.content.Intent
import com.linksrussia.rgk_worker.App
import com.linksrussia.rgk_worker.receivers.DataReceiver
import java.util.function.Consumer
import kotlin.concurrent.thread

class DeviceDataUtil {
    companion object {
        private var bluetoothSocket: BluetoothSocket? = null
        private var receiveThread: Thread? = null

        fun createSessionForSelectedDevice(callback: () -> Unit) {
            receiveThread?.interrupt()

            if (null != bluetoothSocket) {
                DeviceSessionWorker.closeSocket(bluetoothSocket)
            }

            bluetoothSocket = DeviceSessionWorker.getConnectedSocket(App.selectedDevice, 1)
            callback.invoke()

            if (null != bluetoothSocket) {
                receiveThread = thread {
                    DeviceSessionWorker.receiveData(
                        bluetoothSocket,
                        Consumer { line ->
                            println(line)
                            if (line.startsWith("Len=")) {
                                val distStr = line.substring("Len=".length)
                                App.context?.sendBroadcast(
                                    Intent(DataReceiver.INTENT_ACTION)
                                        .putExtra(
                                            DataReceiver.DATA_EXTRA,
                                            distStr.toDouble()
                                        )
                                )
                            }
                        })
                }
            }
        }
    }
}