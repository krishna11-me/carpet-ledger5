package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TransactionEntity::class, CustomCarpetTypeEntity::class, CustomCarpetSizeEntity::class, CustomCategoryEntity::class, CarpetImageEntity::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val transactionDao: TransactionDao
    abstract val customCarpetTypeDao: CustomCarpetTypeDao
    abstract val customCarpetSizeDao: CustomCarpetSizeDao
    abstract val customCategoryDao: CustomCategoryDao
    abstract val carpetImageDao: CarpetImageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "carpet_ledger_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
