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
import com.linksrussia.rgk_worker.util.DialogUtil
import com.linksrussia.rgk_worker.util.ShareUtil

class SeriesActivity : AppCompatActivity() {
    private val dialogUtil = DialogUtil()
    private val seriesDao: SeriesDao = App.getDB().seriesDao()!!
    private val measureDao: MeasureDao = App.getDB().measureDao()!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_series)

        val dialog: Dialog = dialogUtil.onCreateAddSeriesDialog(this)
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view -> dialog.show() }

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
                .setOnClickListener { view: View? ->
                    ShareUtil.sendData(
                        this@SeriesActivity, measureDao.getBySeries(series.id)
                    )
                }
            inflate.findViewById<View>(R.id.itemMeasurementDelete)
                .setOnClickListener { view: View? ->
                    AlertDialog.Builder(this).setTitle("Нужно подтверждение")
                        .setMessage("Вы уверены что хотите удалить серю измерений?")
                        .setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(
                            "ДА"
                        ) { dialogInterface: DialogInterface?, i: Int ->
                            seriesDao.delete(series)
                            renderData()
                        }.setNegativeButton("НЕТ", null).show()
                }
            (inflate.findViewById<View>(R.id.itemMeasurementName) as TextView).setText(series.name)
            inflate.setOnClickListener { view: View? ->
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