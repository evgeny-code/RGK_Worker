package com.linksrussia.rgk_worker.activities

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.linksrussia.rgk_worker.App
import com.linksrussia.rgk_worker.R
import com.linksrussia.rgk_worker.dto.BluetoothDeviceWrapper
import com.linksrussia.rgk_worker.util.DeviceDataUtil
import kotlin.concurrent.thread


class SelectDeviceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_device)

        findViewById<View>(R.id.checkButton).setOnClickListener {
            if (checkPermissions()) {
                App.DIALOG_UTIL.infoDialog(
                    this,
                    "Все необходимые разрешения у приложения есть"
                ).show()
            }
        }

        findViewById<View>(R.id.btSettingButton).setOnClickListener {
            startActivity(
                Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            )
        }

        findViewById<View>(R.id.appSettingButton).setOnClickListener {
            val uri = Uri.fromParts("package", packageName, null)
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also { intent ->
                intent.data = uri
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (checkPermissions()) {
            thread {
                renderBonded()
            }
        }
    }

    private fun checkPermissions(): Boolean {
        if (
            PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(
                this@SelectDeviceActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            ||
            PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(
                this@SelectDeviceActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            ActivityCompat.requestPermissions(
                this@SelectDeviceActivity,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                2
            )

            return false
        }

        if (ContextCompat.checkSelfPermission(
                this@SelectDeviceActivity,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_DENIED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(
                    this@SelectDeviceActivity,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    2
                )
                return false
            }
        }

        return true
    }

    @SuppressLint("MissingPermission")
    fun renderBonded() {
        val bluetoothManager: BluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        val deviceMap = mutableMapOf<String, BluetoothDeviceWrapper>()
        for (bondedDevice in bluetoothManager.adapter.bondedDevices) {
            deviceMap[bondedDevice.address] =
                BluetoothDeviceWrapper(bondedDevice, bondedDevice.name)
        }

        runOnUiThread {
            val layout = findViewById<GridLayout>(R.id.bondedDevicesLayout)
            layout.removeAllViews()

            if (deviceMap.isEmpty())
                App.DIALOG_UTIL.infoDialog(
                    this@SelectDeviceActivity,
                    "У вас нет привязанных Bluetooth приборов"
                ).show()


            val selectedDevice: BluetoothDevice? = App.selectedDevice
            deviceMap.forEach { (_: String?, bluetoothDeviceWrapper: BluetoothDeviceWrapper) ->
                val inflate: View = layoutInflater.inflate(R.layout.item_device, layout, false)

                (inflate.findViewById<View>(R.id.deviceAddress) as TextView).text =
                    bluetoothDeviceWrapper.device.address
                inflate.setOnClickListener {
                    App.selectedDevice = bluetoothDeviceWrapper.device
                    Toast.makeText(
                        this@SelectDeviceActivity,
                        "Пытаемся подключиться к прибору " + bluetoothDeviceWrapper.getName(),
                        Toast.LENGTH_SHORT
                    ).show()

                    val isOk = DeviceDataUtil.createSessionForSelectedDevice()
                    if (isOk) {
                        Toast.makeText(
                            this@SelectDeviceActivity,
                            String.format("Прибор %s подключен", bluetoothDeviceWrapper.getName()),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@SelectDeviceActivity,
                            String.format(
                                "Не могу подключиться к прибору %s",
                                bluetoothDeviceWrapper.getName()
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    thread {
                        renderBonded()
                    }
                }

                val checkBox = inflate.findViewById<View>(R.id.checkBox) as CheckBox
                checkBox.text = bluetoothDeviceWrapper.getName()

                if (App.isDeviceConnected()
                    && bluetoothDeviceWrapper.device.address.equals(selectedDevice?.address)
                ) checkBox.isChecked = true

                layout.addView(inflate)
            }

            findViewById<View>(R.id.progressBar_cyclic).visibility = View.INVISIBLE
        }
    }
}