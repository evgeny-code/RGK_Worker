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
import android.widget.CheckBox
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.linksrussia.rgk_worker.App
import com.linksrussia.rgk_worker.R
import com.linksrussia.rgk_worker.dto.BluetoothDeviceWrapper
import com.linksrussia.rgk_worker.util.DeviceDataUtil
import com.linksrussia.rgk_worker.util.DialogUtil

class SelectDeviceActivity : AppCompatActivity() {
    val ACCESS_COARSE_LOCATION_CODE = 33
    val ACCESS_FINE_LOCATION_CODE = 55

    private val dialogUtil = DialogUtil()

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_device)

        findViewById<View>(R.id.checkButton).setOnClickListener { v: View? ->
            var isOk = true

            if (!this@SelectDeviceActivity.checkOrRequest(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    ACCESS_COARSE_LOCATION_CODE
                )
            ) {
                isOk = false
            }
            if (!this@SelectDeviceActivity.checkOrRequest(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    ACCESS_FINE_LOCATION_CODE
                )
            ) {
                isOk = false
            }
            if (isOk) {
                dialogUtil.infoDialog(
                    this,
                    "Все необходимые разрешения у приложения есть"
                ).show()
            } else {
                dialogUtil.infoDialog(
                    this,
                    "Для работы приложения нужен доступ к местоположению"
                ).show()
            }
        }

        findViewById<View>(R.id.btSettingButton).setOnClickListener { v: View? ->
            startActivity(
                Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            )
        }

        findViewById<View>(R.id.appSettingButton).setOnClickListener { v: View? ->
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }

        renderBonded()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    fun renderBonded() {
        val bluetoothManager: BluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        val deviceMap = mutableMapOf<String, BluetoothDeviceWrapper>()
        for (bondedDevice in bluetoothManager.adapter.bondedDevices) {
            deviceMap[bondedDevice.address] =
                BluetoothDeviceWrapper(bondedDevice, bondedDevice.name)
        }

        val layout = findViewById<GridLayout>(R.id.bondedDevicesLayout)
        layout.removeAllViews()

        if (deviceMap.isEmpty())
            dialogUtil.infoDialog(
                this@SelectDeviceActivity,
                "У вас нет привязанных Bluetooth приборов"
            ).show()


        val selectedDevice: BluetoothDevice? = App.selectedDevice
        deviceMap.forEach { (name: String?, bluetoothDeviceWrapper: BluetoothDeviceWrapper) ->

            val inflate: View = layoutInflater.inflate(R.layout.item_device, layout, false)
            (inflate.findViewById<View>(R.id.deviceName) as TextView).text =
                bluetoothDeviceWrapper.getName()
            (inflate.findViewById<View>(R.id.deviceAddress) as TextView).text =
                bluetoothDeviceWrapper.device.address
            inflate.setOnClickListener { v: View? ->
                App.selectedDevice = bluetoothDeviceWrapper.device
                Toast.makeText(
                    this@SelectDeviceActivity,
                    "Пытаемся подключиться к прибору " + bluetoothDeviceWrapper.getName(),
                    Toast.LENGTH_SHORT
                ).show()

                DeviceDataUtil.createSessionForSelectedDevice(this::renderBonded)
            }

            if (App.isDeviceConnected()
                && bluetoothDeviceWrapper.device.address.equals(selectedDevice?.address)
            ) (inflate.findViewById<View>(R.id.checkBox) as CheckBox).isChecked = true

            layout.addView(inflate)
        }
    }

    private fun checkOrRequest(permission: String, requestCode: Int): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this@SelectDeviceActivity,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@SelectDeviceActivity,
                arrayOf(permission),
                requestCode
            )
            return false
        }
        return true
    }
}