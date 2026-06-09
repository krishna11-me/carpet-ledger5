package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CarpetImageDao {
    @Query("SELECT * FROM carpet_images")
    fun getAllCarpetImages(): Flow<List<CarpetImageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarpetImage(carpetImage: CarpetImageEntity)

    @Query("DELETE FROM carpet_images WHERE styleName = :styleName")
    suspend fun deleteCarpetImage(styleName: String)

    @Query("DELETE FROM carpet_images")
    suspend fun deleteAllCarpetImages()
}
