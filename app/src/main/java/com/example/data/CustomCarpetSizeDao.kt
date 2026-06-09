package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomCarpetSizeDao {
    @Query("SELECT * FROM custom_carpet_sizes ORDER BY id ASC")
    fun getAllCustomSizes(): Flow<List<CustomCarpetSizeEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCustomSize(customSize: CustomCarpetSizeEntity)

    @Delete
    suspend fun deleteCustomSize(customSize: CustomCarpetSizeEntity)

    @Query("DELETE FROM custom_carpet_sizes")
    suspend fun deleteAllCustomSizes()
}
