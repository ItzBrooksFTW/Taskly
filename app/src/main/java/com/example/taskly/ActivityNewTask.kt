package com.example.taskly





import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taskly.data.Task
import com.example.taskly.utils.TaskStorage
import com.example.taskly.utils.switchScreens
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class ActivityNewTask : AppCompatActivity() {

    private lateinit var dateInput : TextView
    private var selectedDateTime: LocalDateTime? = null
    private var currentDateTime: LocalDateTime? = null
    private lateinit var taskStorage: TaskStorage
    private var taskList: MutableList<Task> = mutableListOf()
    private var taskToEditID: String? = null  //sluzi da se provjeri je li se ureduje zadatak ili samo stvara novi
    private var taskToEdit: Task? = null
    private var taskToSchedule: Task? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_task)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }





        taskStorage = TaskStorage(this)
        taskList = taskStorage.loadTasks().toMutableList()

        Log.d("ActivityNewTask", "Loaded tasks: $taskList")


        val spinner: Spinner = findViewById(R.id.spinnerPriority)

        val buttonSubmit : Button=findViewById(R.id.buttonSubmit)

        dateInput = findViewById(R.id.dateInput)

        dateInput.setOnClickListener(){

            showMaterialDatePicker()

        }



        currentDateTime=LocalDateTime.now().plus(5, java.time.temporal.ChronoUnit.MINUTES)
        updateSelectedDateTextView(currentDateTime!!)
        selectedDateTime=currentDateTime

       ArrayAdapter.createFromResource(
            this,
            R.array.dropdown_values_priority,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        taskToEditID = intent.getStringExtra("taskID")
        taskToEdit = taskList.find { it.id == taskToEditID }
        taskToEdit?.let { task ->
            findViewById<TextView>(R.id.activityTitle).text= "Uredi zadatak"
            findViewById<TextView>(R.id.titleInput).text = task.title
            findViewById<TextView>(R.id.descInput).text = task.description
            selectedDateTime = LocalDateTime.parse(task.date)
            updateSelectedDateTextView(selectedDateTime!!)
            val priorityPosition = resources.getStringArray(R.array.dropdown_values_priority).indexOf(task.priority)
            spinner.setSelection(priorityPosition)
            buttonSubmit.text = "Ažuriraj zadatak"
        }

        buttonSubmit.setOnClickListener(){

            if (findViewById<TextView>(R.id.titleInput).text.toString().isNotBlank() && selectedDateTime != null) {
                val title = findViewById<TextView>(R.id.titleInput).text.toString().trim()
                val description = findViewById<TextView>(R.id.descInput).text.toString().trim()
                val priority = spinner.selectedItem.toString()
                if (taskToEdit != null) {

                    taskToEdit!!.title = title
                    taskToEdit!!.description = description
                    taskToEdit!!.date = selectedDateTime!!.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    taskToEdit!!.priority = priority
                    taskToEdit!!.dateChanged = LocalDateTime.now().toString()

                    Log.d("provjera", "${taskToEdit!!.id}\n$taskToEditID")
                    val position = taskList.indexOfFirst { it.id == taskToEditID }
                    if(position != -1) {
                        taskList[position] = taskToEdit!!
                        Log.d("ActivityNewTaskEdit2", "Postoji")
                    }
                    taskToSchedule=taskToEdit
                    Log.d("ActivityNewTaskEdit", "Loaded tasks: $taskList")
                } else {

                    val task = Task.createTask(title, description, selectedDateTime!!, priority, false, "")
                    taskList.add(task)

                    taskToSchedule=task
                }
                taskToSchedule?.let { scheduleNotification(it) }
                taskStorage.saveTasks(taskList)
                switchScreens(this, ActivityAllTasks::class.java, true, null)
            } else {
                Toast.makeText(this, "Unesite naslov i datum", Toast.LENGTH_SHORT).show()
            }
        }
        //gumb nazad
        val buttonBack: Button = findViewById(R.id.buttonBack)
        buttonBack.setOnClickListener {
            if(taskToEdit != null) {  //ako se ureduje zadatak onda se vraca na all tasks
                switchScreens(this, ActivityAllTasks::class.java, true, null)
            } else {
                switchScreens(this, MainActivity::class.java, true, null)
            }
        }

    }



    private fun showMaterialDatePicker() {
        val dateSelection: Long

        val today = MaterialDatePicker.todayInUtcMilliseconds()
        if(taskToEdit==null){
            dateSelection=today

        }
        else{
            dateSelection=LocalDateTime.parse(taskToEdit!!.date).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
        val constraintsBuilder = CalendarConstraints.Builder()
            .setStart(today)



            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Izaberi datum")
                .setSelection(dateSelection)
                .setCalendarConstraints(constraintsBuilder.build())
                .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val selectedDate = Instant.ofEpochMilli(selection)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            showMaterialTimePicker(selectedDate)

        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }



    private fun showMaterialTimePicker(selectedDate: LocalDate) {
        val timeSelection: LocalDateTime
        if(taskToEdit==null){ //ako se stvara novi zadatak
            timeSelection = LocalDateTime.now().plusMinutes(5)
        }
        else{ //ako se ureduje zadatak
            timeSelection = LocalDateTime.parse(taskToEdit!!.date)
        }


        val hour = timeSelection.hour
        val minute = timeSelection.minute

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

            val selectedTime= LocalTime.of(selectedHour, selectedMinute)



           if (LocalDateTime.of(selectedDate, selectedTime).isAfter(LocalDateTime.now())) {
                selectedDateTime = LocalDateTime.of(selectedDate, selectedTime)
               Log.d("ActivityNewTask", "$selectedDate $selectedTime")
                updateSelectedDateTextView(selectedDateTime!!)

            } else {
                Log.d("ActivityNewTask", "$selectedDate $selectedTime")
                Toast.makeText(this, "Odaberite buduće vrijeme", Toast.LENGTH_SHORT).show()
            }
        }

        timePicker.show(supportFragmentManager, "TIME_PICKER")
    }


    private fun updateSelectedDateTextView(chosenDateTime: LocalDateTime) {
        chosenDateTime.let {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault())
            dateInput.text = it.format(formatter)
        }
    }



    private fun scheduleNotification(task: Task) {


        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("id", task.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAtMillis =
            task.getLocalDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()


        try {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } catch (e: SecurityException) {
            Log.e("ActivityNewTask", "Cannot schedule exact alarms: ${e.message}")
        }
    }

}
