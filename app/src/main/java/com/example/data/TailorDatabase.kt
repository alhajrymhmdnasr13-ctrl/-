package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ClientEntity::class], version = 1, exportSchema = false)
abstract class TailorDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao

    companion object {
        @Volatile
        private var INSTANCE: TailorDatabase? = null

        fun getDatabase(context: Context): TailorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TailorDatabase::class.java,
                    "tailor_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
