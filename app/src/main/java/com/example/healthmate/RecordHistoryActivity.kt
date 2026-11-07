package com.example.healthmate

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.healthmate.viewmodel.RecordHistoryViewModel
import android.app.DatePickerDialog
import android.widget.Button
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import android.content.Intent


class RecordHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_record_history)

        val recordsList = findViewById<ListView>(R.id.lvRecords)
        val emptyMessage = findViewById<TextView>(
            R.id.tvEmptyRecords
        )

        val adapter = ArrayAdapter<String>(
            this,
            R.layout.item_health_record,
            mutableListOf<String>()
        )

        recordsList.adapter = adapter
        recordsList.emptyView = emptyMessage

        val viewModel = ViewModelProvider(this)[
            RecordHistoryViewModel::class.java
        ]

        recordsList.setOnItemClickListener { _, _, position, _ ->
            val record = viewModel.records.value?.getOrNull(position)

            if (record != null) {
                val editIntent = Intent(
                    this,
                    EditRecordActivity::class.java
                )

                editIntent.putExtra("record_id", record.id)
                startActivity(editIntent)
            }
        }

        // Press and hold a record to request deletion.
        recordsList.setOnItemLongClickListener { _, _, position, _ ->

            val record = viewModel.records.value?.getOrNull(position)

            if (record != null) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.delete_record_title)
                    .setMessage(
                        getString(
                            R.string.delete_record_message,
                            record.date
                        )
                    )
                    .setPositiveButton(R.string.delete_action) { _, _ ->
                        viewModel.deleteRecord(record)
                    }
                    .setNegativeButton(R.string.cancel_action, null)
                    .show()
            }

            true
        }

// Show whether deletion succeeded.
        viewModel.deleteResult.observe(this) { success ->
            if (success != null) {
                Toast.makeText(
                    this,
                    if (success) {
                        R.string.record_deleted
                    } else {
                        R.string.delete_failed
                    },
                    Toast.LENGTH_SHORT
                ).show()

                viewModel.clearDeleteResult()
            }
        }

        val filterButton = findViewById<Button>(R.id.btnFilterDate)
        val showAllButton = findViewById<Button>(R.id.btnShowAll)

        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        )

        viewModel.selectedDate.observe(this) { date ->
            filterButton.text = if (date == null) {
                getString(R.string.filter_by_date)
            } else {
                getString(R.string.filter_selected_date, date)
            }
        }

        filterButton.setOnClickListener {
            val calendar = Calendar.getInstance()

            // Reopen the calendar at the previously selected date.
            viewModel.selectedDate.value?.let { date ->
                dateFormat.parse(date)?.let { parsedDate ->
                    calendar.time = parsedDate
                }
            }

            val picker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)

                    val selectedDate = dateFormat.format(calendar.time)
                    viewModel.filterByDate(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            picker.show()
        }

        showAllButton.setOnClickListener {
            viewModel.showAllRecords()
        }

        viewModel.records.observe(this) { records ->
            val displayRecords = records.map { record ->
                getString(
                    R.string.record_details,
                    record.date,
                    record.steps,
                    record.calories,
                    record.water
                )
            }

            adapter.clear()
            adapter.addAll(displayRecords)
            adapter.notifyDataSetChanged()
        }

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