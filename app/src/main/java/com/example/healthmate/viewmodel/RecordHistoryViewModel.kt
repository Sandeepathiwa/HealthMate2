package com.example.healthmate.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.example.healthmate.data.HealthMateDatabase
import com.example.healthmate.data.HealthRecordRepository
import androidx.lifecycle.viewModelScope
import com.example.healthmate.data.HealthRecord
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class RecordHistoryViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = HealthRecordRepository(
        HealthMateDatabase.getDatabase(application)
            .healthRecordDao()
    )

    // null means show every record.
    private val _selectedDate = MutableLiveData<String?>(null)

    val selectedDate: LiveData<String?> = _selectedDate

    val records = _selectedDate.switchMap { date ->
        if (date == null) {
            repository.allRecords.asLiveData()
        } else {
            repository.getRecordsByDate(date).asLiveData()

        }


    }


    private val _deleteResult = MutableLiveData<Boolean?>()
    val deleteResult: LiveData<Boolean?> = _deleteResult

    private var deleting = false

    fun deleteRecord(record: HealthRecord) {
        if (deleting) return

        deleting = true

        viewModelScope.launch {
            try {
                val deletedRows = repository.deleteRecord(record)
                _deleteResult.value = deletedRows == 1
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                _deleteResult.value = false
            } finally {
                deleting = false
            }
        }
    }

    fun clearDeleteResult() {
        _deleteResult.value = null
    }fun filterByDate(date: String) {

        _selectedDate.value = date
    }

    fun showAllRecords() {
        _selectedDate.value = null
    }
}