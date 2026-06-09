package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carpet_images")
data class CarpetImageEntity(
    @PrimaryKey val styleName: String,
    val imageUri: String
)
