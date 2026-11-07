package com.example.healthmate

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Intent
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import com.example.healthmate.viewmodel.DashboardViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var dashboardViewModel: DashboardViewModel

    override fun onResume() {
        super.onResume()

        dashboardViewModel.refreshDate()

        val dateFormat = SimpleDateFormat(
            "EEEE, d MMMM yyyy",
            Locale.getDefault()
        )

        findViewById<TextView>(R.id.tvDate).text =
            dateFormat.format(Date())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnRecordHistory).setOnClickListener {
            startActivity(Intent(this, RecordHistoryActivity::class.java))
        }

        dashboardViewModel = ViewModelProvider(this)[
            DashboardViewModel::class.java
        ]

        val stepsText = findViewById<TextView>(R.id.tvSteps)
        val caloriesText = findViewById<TextView>(R.id.tvCalories)
        val waterText = findViewById<TextView>(R.id.tvWater)

        dashboardViewModel.todayRecords.observe(this) { records ->
            val totalSteps = records.sumOf { it.steps.toLong() }
            val totalCalories = records.sumOf { it.calories.toLong() }
            val totalWater = records.sumOf { it.water.toLong() }

            stepsText.text = getString(R.string.steps_total, totalSteps)
            caloriesText.text = getString(
                R.string.calories_total,
                totalCalories
            )
            waterText.text = getString(R.string.water_total, totalWater)
        }

        findViewById<Button>(R.id.btnAddRecord).setOnClickListener {
            startActivity(Intent(this, AddRecordActivity::class.java))
        }

        val dateText = findViewById<TextView>(R.id.tvDate)

        val dateFormat = SimpleDateFormat(
            "EEEE, d MMMM yyyy",
            Locale.getDefault()
        )

        dateText.text = dateFormat.format(Date())

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