package com.example.healthmate.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.example.healthmate.data.HealthMateDatabase
import com.example.healthmate.data.HealthRecordRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class DailyTotals(
    val steps: Long,
    val calories: Long,
    val water: Long
)

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = HealthRecordRepository(
        HealthMateDatabase.getDatabase(application)
            .healthRecordDao()
    )

    private fun todayDate(): String {
        return SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).format(Date())
    }

    private val selectedDate = MutableStateFlow(todayDate())

    val todayRecords = selectedDate.flatMapLatest { date ->
        repository.getRecordsByDate(date)
    }.asLiveData()

    fun refreshDate() {
        selectedDate.value = todayDate()
    }
}