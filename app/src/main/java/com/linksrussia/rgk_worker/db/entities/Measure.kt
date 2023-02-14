package com.linksrussia.rgk_worker.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = Series::class,
        parentColumns = ["id"],
        childColumns = ["series_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
class Measure {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var distance: Double? = null
    var timeMills: Long = 0

    @ColumnInfo(name = "series_id")
    var seriesId: Long = 0
}