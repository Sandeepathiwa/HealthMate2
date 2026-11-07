package com.example.healthmate.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.healthmate.data.HealthMateDatabase
import com.example.healthmate.data.HealthRecord
import com.example.healthmate.data.HealthRecordRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class EditRecordViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = HealthRecordRepository(
        HealthMateDatabase.getDatabase(application)
            .healthRecordDao()
    )

    private val _record = MutableLiveData<HealthRecord>()
    val record: LiveData<HealthRecord> = _record

    private val _loadError = MutableLiveData(false)
    val loadError: LiveData<Boolean> = _loadError

    private val _saveState = MutableLiveData(SaveState.IDLE)
    val saveState: LiveData<SaveState> = _saveState

    private var requestedId: Int? = null

    fun loadRecord(id: Int) {
        // Avoid loading again when the screen rotates.
        if (requestedId == id) return
        requestedId = id

        viewModelScope.launch {
            try {
                val existingRecord = repository.getRecordById(id)

                if (existingRecord == null) {
                    _loadError.value = true
                } else {
                    _record.value = existingRecord
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                _loadError.value = true
            }
        }
    }

    fun updateRecord(
        date: String,
        steps: Int,
        calories: Int,
        water: Int
    ) {
        val existingRecord = _record.value ?: return

        if (_saveState.value == SaveState.SAVING ||
            _saveState.value == SaveState.SUCCESS
        ) {
            return
        }

        if (date.isBlank() ||
            steps < 0 ||
            calories < 0 ||
            water < 0
        ) {
            _saveState.value = SaveState.ERROR
            return
        }

        _saveState.value = SaveState.SAVING

        viewModelScope.launch {
            try {
                val updatedRecord = existingRecord.copy(
                    date = date,
                    steps = steps,
                    calories = calories,
                    water = water
                )

                val updatedRows = repository.updateRecord(
                    updatedRecord
                )

                _saveState.value = if (updatedRows == 1) {
                    SaveState.SUCCESS
                } else {
                    SaveState.ERROR
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                _saveState.value = SaveState.ERROR
            }
        }
    }
}