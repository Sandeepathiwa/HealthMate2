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

enum class SaveState {
    IDLE,
    SAVING,
    SUCCESS,
    ERROR
}

class AddRecordViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = HealthRecordRepository(
        HealthMateDatabase.getDatabase(application)
            .healthRecordDao()
    )

    private val _saveState = MutableLiveData(SaveState.IDLE)
    val saveState: LiveData<SaveState> = _saveState

    fun saveRecord(
        date: String,
        steps: Int,
        calories: Int,
        water: Int
    ) {
        // Prevent repeated saves while saving or after success.
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
                val record = HealthRecord(
                    date = date,
                    steps = steps,
                    calories = calories,
                    water = water
                )

                val newId = repository.insertRecord(record)

                _saveState.value = if (newId > 0) {
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