package com.example.healthmate.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "health_records")
data class HealthRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val date: String,

    val steps: Int,

    val calories: Int,

    val water: Int
)

@Dao
interface HealthRecordDao {

    @Insert
    suspend fun insertRecord(record: HealthRecord): Long

    @Query("SELECT * FROM health_records ORDER BY date DESC, id DESC")
    fun getAllRecords(): Flow<List<HealthRecord>>

    @Query("SELECT * FROM health_records WHERE date = :date ORDER BY id DESC")
    fun getRecordsByDate(date: String): Flow<List<HealthRecord>>

    @Query("SELECT * FROM health_records WHERE id = :id")
    suspend fun getRecordById(id: Int): HealthRecord?

    @Update
    suspend fun updateRecord(record: HealthRecord): Int

    @Delete
    suspend fun deleteRecord(record: HealthRecord): Int
}