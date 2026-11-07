package com.example.healthmate.data

import kotlinx.coroutines.flow.Flow

class HealthRecordRepository(
    private val dao: HealthRecordDao
) {

    val allRecords: Flow<List<HealthRecord>> =
        dao.getAllRecords()

    fun getRecordsByDate(date: String): Flow<List<HealthRecord>> {
        return dao.getRecordsByDate(date)
    }

    suspend fun getRecordById(id: Int): HealthRecord? {
        return dao.getRecordById(id)
    }

    suspend fun insertRecord(record: HealthRecord): Long {
        return dao.insertRecord(record)
    }

    suspend fun updateRecord(record: HealthRecord): Int {
        return dao.updateRecord(record)
    }

    suspend fun deleteRecord(record: HealthRecord): Int {
        return dao.deleteRecord(record)
    }
}