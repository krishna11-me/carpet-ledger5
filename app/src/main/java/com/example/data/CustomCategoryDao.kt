package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomCategoryDao {
    @Query("SELECT * FROM custom_categories ORDER BY id ASC")
    fun getAllCustomCategories(): Flow<List<CustomCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCustomCategory(category: CustomCategoryEntity)

    @Delete
    suspend fun deleteCustomCategory(category: CustomCategoryEntity)

    @Query("DELETE FROM custom_categories")
    suspend fun deleteAllCustomCategories()
}
