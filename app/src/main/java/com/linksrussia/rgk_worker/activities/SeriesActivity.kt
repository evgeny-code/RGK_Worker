package com.linksrussia.rgk_worker.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.linksrussia.rgk_worker.App
import com.linksrussia.rgk_worker.R
import com.linksrussia.rgk_worker.db.dao.MeasureDao
import com.linksrussia.rgk_worker.db.dao.SeriesDao
import com.linksrussia.rgk_worker.util.ShareUtil

class SeriesActivity : AppCompatActivity() {
    private val seriesDao: SeriesDao = App.getDB().seriesDao()!!
    private val measureDao: MeasureDao = App.getDB().measureDao()!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_series)

        val dialog: Dialog = App.DIALOG_UTIL.onCreateAddSeriesDialog(this)
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { dialog.show() }

        renderData()
    }

    fun renderData() {
        val layoutInflater = layoutInflater
        val measurementLayout = findViewById<GridLayout>(R.id.measurementLayout)

        measurementLayout.removeAllViews()

        for (series in seriesDao.getAll()) {
            val inflate: View =
                layoutInflater.inflate(R.layout.item_series, measurementLayout, false)
            inflate.findViewById<View>(R.id.itemMeasurementExport)
                .setOnClickListener {
                    ShareUtil.sendData(
                        this@SeriesActivity, measureDao.getBySeries(series.id)
                    )
                }
            inflate.findViewById<View>(R.id.itemMeasurementDelete)
                .setOnClickListener {
                    AlertDialog.Builder(this).setTitle(R.string.needConfirm)
                        .setMessage(R.string.confirmDelete)
                        .setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(
                            R.string.yes
                        ) { _: DialogInterface?, _: Int ->
                            seriesDao.delete(series)
                            renderData()
                        }.setNegativeButton(R.string.no, null).show()
                }
            (inflate.findViewById<View>(R.id.itemMeasurementName) as TextView).text = series.name
            inflate.setOnClickListener {
                startActivity(
                    Intent(
                        this, MeasurementActivity::class.java
                    ).putExtra(MeasurementActivity.SERIES_ID, series.id)
                        .putExtra(MeasurementActivity.SERIES_NAME, series.name)
                )
            }
            measurementLayout.addView(inflate)
        }
    }


}