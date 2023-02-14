package com.linksrussia.rgk_worker.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(unique = true, value = ["name"])])
class Series(var name: String) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
