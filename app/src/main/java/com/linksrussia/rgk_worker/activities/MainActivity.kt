package com.linksrussia.rgk_worker.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.linksrussia.rgk_worker.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.selectDeviceBtn).setOnClickListener { btn: View? ->
            startActivity(Intent(this, SelectDeviceActivity::class.java))
        }
        findViewById<View>(R.id.measurementBtn).setOnClickListener { btn: View? ->
            startActivity(Intent(this, SeriesActivity::class.java))
        }
        findViewById<View>(R.id.infoBtn).setOnClickListener { btn: View? ->
            startActivity(Intent(this, InfoActivity::class.java))
        }
    }
}