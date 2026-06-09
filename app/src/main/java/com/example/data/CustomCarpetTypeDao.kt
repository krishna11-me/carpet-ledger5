package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomCarpetTypeDao {
    @Query("SELECT * FROM custom_carpet_types ORDER BY id ASC")
    fun getAllCustomTypes(): Flow<List<CustomCarpetTypeEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCustomType(customType: CustomCarpetTypeEntity)

    @Delete
    suspend fun deleteCustomType(customType: CustomCarpetTypeEntity)

    @Query("DELETE FROM custom_carpet_types")
    suspend fun deleteAllCustomTypes()
}
