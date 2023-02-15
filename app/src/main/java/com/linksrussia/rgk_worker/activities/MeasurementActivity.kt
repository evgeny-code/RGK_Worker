package com.linksrussia.rgk_worker.activities

import android.annotation.SuppressLint
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.TableLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.linksrussia.rgk_worker.App
import com.linksrussia.rgk_worker.R
import com.linksrussia.rgk_worker.db.dao.MeasureDao
import com.linksrussia.rgk_worker.db.entities.Measure
import com.linksrussia.rgk_worker.receivers.DataReceiver
import com.linksrussia.rgk_worker.util.DialogUtil
import com.linksrussia.rgk_worker.util.ShareUtil
import java.text.SimpleDateFormat
import java.util.*
import java.util.function.Consumer
import kotlin.concurrent.thread

class MeasurementActivity : AppCompatActivity() {
    companion object {
        val SERIES_ID = "MEASURE_ID"
        val SERIES_NAME = "MEASURE_NAME"
    }

    private var dataReceiver: DataReceiver? = null
    private val dialogUtil = DialogUtil()
    private val measureDao: MeasureDao? = App.db!!.measureDao()
    private val renderedRows = mutableListOf<View>()

    fun renderData(measures: List<Measure>?) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")

        val measurementDataLayout = findViewById<TableLayout>(R.id.measurementDataLayout)
        renderedRows.forEach(Consumer { view: View? ->
            measurementDataLayout.removeView(
                view
            )
        })
        renderedRows.clear()
        for (measure in measures!!) {
            val inflate =
                layoutInflater.inflate(R.layout.item_measure, measurementDataLayout, false)
            (inflate.findViewById<View>(R.id.itemMeasureDateTime) as TextView).text =
                dateFormat.format(Date(measure?.timeMills!!))
            (inflate.findViewById<View>(R.id.itemMeasureDistance) as TextView).text =
                "" + measure.distance
            inflate.findViewById<View>(R.id.itemMeasurementDelete)
                .setOnClickListener { v: View? ->
                    measureDao!!.delete(measure)
                    renderData(measureDao?.getBySeries(measure.seriesId)!!)
                }
            measurementDataLayout.addView(inflate)
            renderedRows.add(inflate)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_measurement)

        val measurementNameTextView = findViewById<TextView>(R.id.measurementName)
        measurementNameTextView.text = intent.getStringExtra(SERIES_NAME)!!.uppercase() + ":"

        val seriesId = intent.getLongExtra(SERIES_ID, -1L)
        dataReceiver =
            DataReceiver(seriesId, Consumer { renderData(measureDao?.getBySeries(seriesId)!!) })
        registerReceiver(dataReceiver, IntentFilter(DataReceiver.INTENT_ACTION))

        findViewById<View>(R.id.fab).setOnClickListener { v: View? ->
            ShareUtil.sendData(
                this@MeasurementActivity,
                measureDao?.getBySeries(seriesId)
            )
        }

        thread {
            renderData(measureDao?.getBySeries(seriesId)!!)
        }


        if (!App.isDeviceConnected()) {
            dialogUtil.onCreateNoDeviceForkDialog(this).show()
        } else {
            Snackbar.make(
                findViewById(R.id.measurementDataLayout),
                String.format(
                    getString(R.string.measurementDeviceInfoPattern),
                    App.selectedDevice?.name,
                    App.selectedDevice?.address
                ),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (null != dataReceiver)
            unregisterReceiver(dataReceiver)
    }
}