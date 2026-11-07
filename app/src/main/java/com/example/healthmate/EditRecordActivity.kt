package com.example.healthmate

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.healthmate.viewmodel.EditRecordViewModel
import com.example.healthmate.viewmodel.SaveState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditRecordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_add_record)

        val title = findViewById<TextView>(R.id.tvFormTitle)
        val dateInput = findViewById<EditText>(R.id.etDate)
        val stepsInput = findViewById<EditText>(R.id.etSteps)
        val caloriesInput = findViewById<EditText>(R.id.etCalories)
        val waterInput = findViewById<EditText>(R.id.etWater)
        val saveButton = findViewById<Button>(R.id.btnSaveRecord)

        title.setText(R.string.edit_record_title)
        saveButton.setText(R.string.update_record)
        saveButton.isEnabled = false

        // The date is selected through the calendar.
        dateInput.keyListener = null

        val viewModel = ViewModelProvider(this)[
            EditRecordViewModel::class.java
        ]

        val recordId = intent.getIntExtra("record_id", -1)

        if (recordId <= 0) {
            Toast.makeText(
                this,
                R.string.record_load_failed,
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        fun refreshSaveButton() {
            val state = viewModel.saveState.value

            saveButton.isEnabled =
                viewModel.record.value != null &&
                        (state == SaveState.IDLE || state == SaveState.ERROR)

            saveButton.setText(
                if (state == SaveState.SAVING) {
                    R.string.saving_record
                } else {
                    R.string.update_record
                }
            )
        }

        viewModel.record.observe(this) { record ->
            if (savedInstanceState == null) {
                dateInput.setText(record.date)
                stepsInput.setText(record.steps.toString())
                caloriesInput.setText(record.calories.toString())
                waterInput.setText(record.water.toString())
            }

            refreshSaveButton()
        }

        viewModel.loadError.observe(this) { failed ->
            if (failed) {
                Toast.makeText(
                    this,
                    R.string.record_load_failed,
                    Toast.LENGTH_LONG
                ).show()

                finish()
            }
        }

        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).apply {
            isLenient = false
        }

        dateInput.setOnClickListener {
            if (viewModel.record.value == null) {
                return@setOnClickListener
            }

            val calendar = Calendar.getInstance()

            dateFormat.parse(
                dateInput.text.toString()
            )?.let { date ->
                calendar.time = date
            }

            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    dateInput.setText(
                        dateFormat.format(calendar.time)
                    )
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
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
                    viewModel.updateRecord(
                        date = dateInput.text.toString().trim(),
                        steps = steps,
                        calories = calories,
                        water = water
                    )
                }
            }
        }

        viewModel.saveState.observe(this) { state ->
            refreshSaveButton()

            when (state) {
                SaveState.SUCCESS -> {
                    Toast.makeText(
                        this,
                        R.string.record_updated,
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

        viewModel.loadRecord(recordId)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }
    }
}