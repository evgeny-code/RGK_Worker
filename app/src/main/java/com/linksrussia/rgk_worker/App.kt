package com.linksrussia.rgk_worker

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.room.Room
import com.linksrussia.rgk_worker.db.AppDatabase
import com.linksrussia.rgk_worker.util.DialogUtil
import java.util.*
import java.util.stream.Collectors

class App : Application() {
    companion object {
        const val DB_NAME = "measurement-series-db-v1"
        val DIALOG_UTIL = DialogUtil()

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

    private val handleAppCrash =
        Thread.UncaughtExceptionHandler { _, ex ->
            val stackTrace = Arrays.stream(ex.stackTrace)
                .map { st -> st.toString() }.collect(Collectors.joining("\n"))

            Intent().also { intent ->
                intent.action = "com.linksrussia.rgk_worker.STACK_TRACE_NOTIFICATION"
                intent.putExtra("data", stackTrace)
                sendBroadcast(intent)
            }

            Log.e("e", stackTrace)
        }


    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler(handleAppCrash);

        App.context = applicationContext
        App.db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, DB_NAME)
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }
}