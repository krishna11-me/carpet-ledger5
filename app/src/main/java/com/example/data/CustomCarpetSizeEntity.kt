package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_carpet_sizes")
data class CustomCarpetSizeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sizeValue: String,    // e.g. "1.50" or "8*10"
    val isDurri: Boolean      // true if it is a Durri dimension, false if standard carpet width (meters)
)
