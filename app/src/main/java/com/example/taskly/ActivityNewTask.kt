package com.example.taskly


import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.setSelection
import androidx.compose.ui.semantics.text

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import java.util.Calendar
import kotlin.text.format
import java.util.Locale

class ActivityNewTask : AppCompatActivity() {

    private lateinit var dateInput : TextView
    private val calendar = Calendar.getInstance()
    private var selectedDateTime: LocalDateTime? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_task)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dateInput = findViewById(R.id.dateInput)

        dateInput.setOnClickListener(){

            showMaterialDatePicker()

        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun showMaterialDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Izaberi datum")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            calendar.timeInMillis = selection
            showMaterialTimePicker(calendar)
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showMaterialTimePicker(calendar: Calendar) {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hour)
            .setMinute(minute)
            .setTitleText("Izaberi vrijeme")
            .build()

        timePicker.addOnPositiveButtonClickListener {
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute

            val selectedDate = Instant.ofEpochMilli(calendar.timeInMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            selectedDateTime = LocalDateTime.of(selectedDate, java.time.LocalTime.of(selectedHour, selectedMinute))
            updateSelectedDateTextView()
        }

        timePicker.show(supportFragmentManager, "TIME_PICKER")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateSelectedDateTextView() {
        selectedDateTime?.let {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault())
            dateInput.text = it.format(formatter)
        }
    }


}