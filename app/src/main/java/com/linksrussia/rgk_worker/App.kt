package com.linksrussia.rgk_worker

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.room.Room
import com.linksrussia.rgk_worker.db.AppDatabase

class App : Application() {
    companion object {
        const val DB_NAME = "measurement-series-db-v1"
        var db: AppDatabase? = null
        var selectedDevice: BluetoothDevice? = null
        var deviceConnected = false
        var context: Context? = null

        fun isDeviceConnected(): Boolean {
            return null != selectedDevice && App.deviceConnected
        }

        fun getDB(): AppDatabase {
            return db!!
        }
    }

    override fun onCreate() {
        super.onCreate()

        App.context = applicationContext
        App.db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, DB_NAME)
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }
}