package com.example.healthmate.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [HealthRecord::class],
    version = 1,
    exportSchema = true
)
abstract class HealthMateDatabase : RoomDatabase() {

    abstract fun healthRecordDao(): HealthRecordDao

    companion object {

        @Volatile
        private var INSTANCE: HealthMateDatabase? = null

        fun getDatabase(context: Context): HealthMateDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HealthMateDatabase::class.java,
                    "health_mate_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}