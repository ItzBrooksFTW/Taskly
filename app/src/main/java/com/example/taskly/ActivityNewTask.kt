package com.example.taskly



import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taskly.data.Task

import com.example.taskly.utils.TaskStorage
import com.example.taskly.utils.switchScreens
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class ActivityNewTask : AppCompatActivity() {

    private lateinit var dateInput : TextView
    private val calendar = Calendar.getInstance()
    private var selectedDateTime: LocalDateTime? = null
    private var currentDateTime: LocalDateTime? = null
    private lateinit var taskStorage: TaskStorage
    private var taskList: MutableList<Task> = mutableListOf()
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
        val buttonBack: Button = findViewById(R.id.buttonBack)
        buttonBack.setOnClickListener {

            switchScreens(this, MainActivity::class.java, true, null)
        }

        taskStorage = TaskStorage(this)
        taskList = taskStorage.loadTasks().toMutableList()

        Log.d("ActivityNewTask", "Loaded tasks: $taskList")

        val spinner: Spinner = findViewById(R.id.spinner)

        val buttonSubmit : Button=findViewById(R.id.buttonSubmit)

        dateInput = findViewById(R.id.dateInput)

        dateInput.setOnClickListener(){

            showMaterialDatePicker()

        }



        currentDateTime=LocalDateTime.now()
        updateSelectedDateTextView(currentDateTime!!)

       ArrayAdapter.createFromResource(
            this,
            R.array.dropdown_values,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        buttonSubmit.setOnClickListener(){

            val title = findViewById<TextView>(R.id.titleInput).text.toString()
            val description = findViewById<TextView>(R.id.descInput).text.toString()
            val priority = spinner.selectedItem.toString()
            if (selectedDateTime != null) {
                val task = Task.fromLocalDateTime(title, description, selectedDateTime!!, priority)
                taskList.add(task)
                taskStorage.saveTasks(taskList)
                val bundle = Bundle()
                bundle.putSerializable("task", task)
                switchScreens(this, ActivityAllTasks::class.java, true, bundle)
            } else {
                Log.e("ActivityNewTask", "selectedDateTime is null")
            }
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
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .setTitleText("Izaberi vrijeme")
            .build()

        timePicker.addOnPositiveButtonClickListener {
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute

            val selectedDate = Instant.ofEpochMilli(calendar.timeInMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            selectedDateTime = LocalDateTime.of(selectedDate, java.time.LocalTime.of(selectedHour, selectedMinute))
            updateSelectedDateTextView(selectedDateTime!!)
        }

        timePicker.show(supportFragmentManager, "TIME_PICKER")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateSelectedDateTextView(chosenDateTime: LocalDateTime) {
        chosenDateTime.let {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault())
            dateInput.text = it.format(formatter)
        }
    }


}