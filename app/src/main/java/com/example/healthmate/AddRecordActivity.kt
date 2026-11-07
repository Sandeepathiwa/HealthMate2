package com.example.healthmate

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.app.DatePickerDialog
import android.widget.EditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.healthmate.viewmodel.AddRecordViewModel
import com.example.healthmate.viewmodel.SaveState

class AddRecordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_record)
        val dateInput = findViewById<EditText>(R.id.etDate)

        val stepsInput = findViewById<EditText>(R.id.etSteps)
        val caloriesInput = findViewById<EditText>(R.id.etCalories)
        val waterInput = findViewById<EditText>(R.id.etWater)
        val saveButton = findViewById<Button>(R.id.btnSaveRecord)

        val viewModel = ViewModelProvider(this)[AddRecordViewModel::class.java]

        viewModel.saveState.observe(this) { state ->
            saveButton.isEnabled = state == SaveState.IDLE ||
                    state == SaveState.ERROR

            saveButton.setText(
                if (state == SaveState.SAVING) {
                    R.string.saving_record
                } else {
                    R.string.save_record
                }
            )

            when (state) {
                SaveState.SUCCESS -> {
                    Toast.makeText(
                        this,
                        R.string.record_saved,
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                }

                SaveState.ERROR -> {
                    Toast.makeText(
                        this,
                        R.string.record_save_failed,
                        Toast.LENGTH_LONG
                    ).show()
                }

                else -> Unit
            }
        }


        fun validateNumber(input: EditText): Int? {
            val text = input.text.toString().trim()
            input.error = null

            if (text.isEmpty()) {
                input.error = getString(R.string.error_required)
                return null
            }

            val number = text.toIntOrNull()

            if (number == null || number < 0) {
                input.error = getString(R.string.error_number)
                return null
            }

            return number
        }


        saveButton.setOnClickListener {
            val steps = validateNumber(stepsInput)
            val calories = validateNumber(caloriesInput)
            val water = validateNumber(waterInput)

            when {
                steps == null -> stepsInput.requestFocus()
                calories == null -> caloriesInput.requestFocus()
                water == null -> waterInput.requestFocus()

                else -> {
                    viewModel.saveRecord(
                        date = dateInput.text.toString().trim(),
                        steps = steps,
                        calories = calories,
                        water = water
                    )
                }
            }
        }

        val selectedDate = Calendar.getInstance()
        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        )


        dateInput.setText(dateFormat.format(selectedDate.time))


        dateInput.setOnClickListener {
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    selectedDate.set(year, month, day)

                    dateInput.setText(
                        dateFormat.format(selectedDate.time)
                    )
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            )

            datePicker.show()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}