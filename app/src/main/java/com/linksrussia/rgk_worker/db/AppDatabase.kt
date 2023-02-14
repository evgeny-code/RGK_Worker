package com.linksrussia.rgk_worker.db    // id 'kotlin-kapt'

import androidx.room.Database
import androidx.room.RoomDatabase
import com.linksrussia.rgk_worker.db.dao.MeasureDao
import com.linksrussia.rgk_worker.db.dao.SeriesDao
import com.linksrussia.rgk_worker.db.entities.Measure
import com.linksrussia.rgk_worker.db.entities.Series

@Database(
    version = 2,
    entities = [Measure::class, Series::class]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun measureDao(): MeasureDao?

    abstract fun seriesDao(): SeriesDao?
}