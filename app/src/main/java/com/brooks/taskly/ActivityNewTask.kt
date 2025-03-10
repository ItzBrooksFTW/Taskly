package com.brooks.taskly





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
import com.brooks.taskly.data.Task
import com.brooks.taskly.utils.TaskStorage
import com.brooks.taskly.utils.switchScreens
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
import android.text.TextWatcher
import android.text.Editable
import android.view.View
import com.brooks.taskly.utils.checkForPerms
import com.brooks.taskly.utils.checkTheme
import com.brooks.taskly.utils.fadeInOut

class ActivityNewTask : AppCompatActivity() {

    private var selectedDateTime: LocalDateTime? = null
    private var currentDateTime: LocalDateTime? = null
    private lateinit var taskStorage: TaskStorage
    private var taskList: MutableList<Task> = mutableListOf()
    private var taskToEditID: String? = null  //sluzi da se provjeri je li se ureduje zadatak ili samo stvara novi
    private var taskToEdit: Task? = null
    private var taskToSchedule: String? = null

    private lateinit var titleInput: TextView
    private lateinit var charCountWarningTitle: TextView
    private lateinit var charCountWarningDesc: TextView
    private lateinit var descInput: TextView
    private lateinit var dateInput: TextView
    private lateinit var spinner: Spinner
    private lateinit var buttonSubmit: Button

    private var isCharCountWarningTitleVisible = false
    private var isCharCountWarningDescVisible = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_task)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkTheme(this)  //provjerava se koja je tema postavljena



        taskStorage = TaskStorage(this)
        taskList = taskStorage.loadTasks().toMutableList() //ucitava se lista zadataka

        taskToEditID = intent.getStringExtra("taskID") //ako je poslan intent s ID-om zadataka onda se ureduje taj zadatak
        taskToEdit = taskList.find { it.id == taskToEditID } //trazi se zadatak s tim ID-om
        Log.d("ActivityNewTask", "Loaded tasks: $taskList")
        Log.d("tasktoedit", "$taskToEditID")

        titleInput=findViewById(R.id.titleInput)
        charCountWarningTitle=findViewById(R.id.charCountWarningTitle)
        charCountWarningDesc=findViewById(R.id.charCountWarningDesc)
        descInput=findViewById(R.id.descInput)
        dateInput = findViewById(R.id.dateInput)
        spinner= findViewById(R.id.spinnerPriority)

        buttonSubmit=findViewById(R.id.buttonSubmit)




        dateInput.setOnClickListener(){ //klikom na polje za datum i vrijeme se otvara dialog za odabir

            showMaterialDatePicker()

        }

        //provjerava se je li uneseno vise od 25 znakova i je li upozorenje vidljivo
        titleInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.length > 25 && !isCharCountWarningTitleVisible) {
                    //charCountWarningTitle.visibility = View.VISIBLE
                    fadeInOut(charCountWarningTitle, true)
                    isCharCountWarningTitleVisible = true
                } else if(s!=null && s.length<=25 && isCharCountWarningTitleVisible){
                    //charCountWarningTitle.visibility = View.GONE
                    fadeInOut(charCountWarningTitle, false)

                    isCharCountWarningTitleVisible = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        //provjerava se je li uneseno vise od 400 znakova i je li upozorenje vidljivo
        descInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.length > 400 && !isCharCountWarningDescVisible) {
                    //charCountWarningTitle.visibility = View.VISIBLE
                    fadeInOut(charCountWarningDesc, true).also{
                        charCountWarningDesc.visibility=View.VISIBLE
                    }
                    isCharCountWarningDescVisible = true
                } else if(s!=null && s.length<=400 && isCharCountWarningDescVisible){

                    //charCountWarningTitle.visibility = View.GONE
                    fadeInOut(charCountWarningDesc, false).also{
                        charCountWarningDesc.visibility=View.GONE
                    }
                    isCharCountWarningDescVisible = false

                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        currentDateTime=LocalDateTime.now().plus(5, java.time.temporal.ChronoUnit.MINUTES)  //postavlja se trenutno vrijeme +5 minuta
        updateSelectedDateTextView(currentDateTime!!) //postavlja se tekst na polje za datum i vrijeme
        selectedDateTime=currentDateTime //postavlja se odabrano vrijeme na trenutno vrijeme


        //postavlja se spinner za odabir prioriteta
       ArrayAdapter.createFromResource(
            this,
            R.array.dropdown_values_priority,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_item)
            spinner.adapter = adapter
        }

        //ako se ureduje zadatak onda se postavljaju vrijednosti polja na vrijednosti zadatka
        taskToEdit?.let { task ->
            findViewById<TextView>(R.id.activityTitle).text= getString(R.string.edit_task_title)
            titleInput.text = task.title
            descInput.text = task.description
            selectedDateTime = LocalDateTime.parse(task.date)
            updateSelectedDateTextView(selectedDateTime!!)
            val priorityPosition = resources.getStringArray(R.array.dropdown_values_priority).indexOf(task.priority)
            spinner.setSelection(priorityPosition)
            buttonSubmit.text = getString(R.string.update_task)
        }

        //gumb za spremanje zadatka
        buttonSubmit.setOnClickListener(){
            checkForPerms(this, activityResultRegistry) //provjerava se je li dozvola za alarm i notifikacije odobrena
                makeTask(titleInput.text.toString(), taskList.size<30)
                //vrijeme je uvijek odabrano pa nema potrebe za provjerom

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
    //ako se pritisne back button onda se vraca na prethodni zaslon
    override fun onBackPressed() {
        super.onBackPressed()
        if(taskToEdit ==null) {
            switchScreens(this, MainActivity::class.java)
        }
        else{
            switchScreens(this, ActivityAllTasks::class.java)
        }
    }

    //funkcija za prikazivanje dialoga za odabir datuma
    private fun showMaterialDatePicker() {
        val dateSelection: Long

        val today = MaterialDatePicker.todayInUtcMilliseconds()
        dateSelection = if(taskToEdit==null){  //ako se stvara novi zadatak onda se postavlja na danasnji datum
            today

        } else{  //ako se ureduje zadatak onda se postavlja odabrani datum
            LocalDateTime.parse(taskToEdit!!.date).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }


        val constraintsBuilder = CalendarConstraints.Builder()
            .setStart(today)



            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.date_picker_title))
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


    //funkcija za prikazivanje dialoga za odabir vremena
    private fun showMaterialTimePicker(selectedDate: LocalDate) {

        //ako se stvara novi zadatak onda se postavlja na trenutno vrijeme +5 minuta
        val timeSelection: LocalDateTime = if(taskToEdit==null){
            LocalDateTime.now().plusMinutes(5)
        } else{ //ako se ureduje zadatak onda se postavlja na odabrano vrijeme
            LocalDateTime.parse(taskToEdit!!.date)
        }


        val hour = timeSelection.hour
        val minute = timeSelection.minute


        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hour)
            .setMinute(minute)
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .setTitleText(getString(R.string.time_picker_title))
            .build()

        timePicker.addOnPositiveButtonClickListener {
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute

            val selectedTime= LocalTime.of(selectedHour, selectedMinute)



           if (LocalDateTime.of(selectedDate, selectedTime).isAfter(LocalDateTime.now())) { //provjerava je li odabrano vrijeme u buducnosti
                selectedDateTime = LocalDateTime.of(selectedDate, selectedTime)
               Log.d("ActivityNewTask", "$selectedDate $selectedTime")
                updateSelectedDateTextView(selectedDateTime!!)

            } else { //ako nije onda se prikazuje toast
                Log.d("ActivityNewTask", "$selectedDate $selectedTime")
                Toast.makeText(this, getString(R.string.time_toast), Toast.LENGTH_SHORT).show()  //"Odaberite vrijeme u buducnosti"
            }
        }

        timePicker.show(supportFragmentManager, "TIME_PICKER")
    }

    //funkcija za azuriranje teksta na polju za datum i vrijeme
    private fun updateSelectedDateTextView(chosenDateTime: LocalDateTime) {
        chosenDateTime.let {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault())
            dateInput.text = it.format(formatter)
        }
    }


    //funkcija za postavljanje notifikacije
    private fun scheduleNotification(taskID:String) {
        Log.d("vrijeme0", taskID)
        val loadedTasks= taskStorage.loadTasks().toMutableList()
        Log.d("taskovi", "Loaded tasks: $loadedTasks")
        val task = loadedTasks.find { it.id == taskID } ?: return
        Log.d("vrijeme", task.getLocalDateTime().toString())
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("id", task.id)
        }

        //postavljanje intenta za notifikaciju
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        //postavljanje alarma
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAtMillis =
            task.getLocalDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        Log.d("vrijeme", LocalDateTime.ofInstant(Instant.ofEpochMilli(triggerAtMillis), ZoneId.systemDefault()).toString())

        try {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            Log.d("vrijeme2", LocalDateTime.ofInstant(Instant.ofEpochMilli(triggerAtMillis), ZoneId.systemDefault()).toString())

        } catch (e: SecurityException) {
            Log.e("ActivityNewTask", "Cannot schedule exact alarms: ${e.message}")
        }
    }
    //funkcija za stvaranje ili azuriranje zadatka
    private fun makeTask(titleCondition: String, taskSizeCondition: Boolean){

        if (titleCondition.isNotBlank() && taskSizeCondition) { //provjerava je li unesen naslov i je li broj zadataka manji od 30
            val title = titleInput.text.toString().trim().capitalize(Locale.getDefault())
            val description = descInput.text.toString().trim().capitalize(Locale.getDefault())
            val priority = spinner.selectedItem.toString()

            if (taskToEditID != null) { //ako se ureduje zadatak onda se azurira taj zadatak
                val prevDate=taskList.find { it.id == taskToEditID }!!.date
                taskToEdit!!.title = title
                taskToEdit!!.description = description
                taskToEdit!!.date = selectedDateTime!!.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                taskToEdit!!.priority = priority
                taskToEdit!!.dateChanged = LocalDateTime.now().toString()  //postavlja se datum promjene

                Log.d("provjera", "${taskToEdit!!.id}\n$taskToEditID")
                val position = taskList.indexOfFirst { it.id == taskToEditID }
                if(position != -1) {
                    taskList[position] = taskToEdit!! //azurira se zadatak u listi
                    Log.d("ActivityNewTaskEdit2", "Postoji")
                }
                //takeif provjerava je li datum u buducnosti jer se alarm postavlja cak i s proslim vremenom pa daje notifikaciju odmah
                taskToSchedule=taskToEditID.takeIf{ LocalDateTime.parse(taskToEdit!!.date).isAfter(LocalDateTime.now()) && prevDate!=taskToEdit!!.date}
                Log.d("ActivityNewTaskEdit", "Loaded tasks: $taskList")
            } else {
                //ako se stvara novi zadatak onda se stvara novi zadatak
                val task = Task.createTask(title, description, selectedDateTime!!, priority/*, false, ""*/)
                taskList.add(task)

                taskToSchedule=task.id.takeIf{ LocalDateTime.parse(task.date).isAfter(LocalDateTime.now())}
                Log.d("taskID", "${task.id}\n$taskToSchedule")
            }

            Log.d("taskID3", "$taskToSchedule")
            taskStorage.saveTasks(taskList)  //sprema se lista zadataka

            //ako je odabrano vrijeme u buducnosti onda se postavlja notifikacija
            taskToSchedule?.let { scheduleNotification(it)
                Log.d("taskID2", "$taskToSchedule")
            }

            switchScreens(this, ActivityAllTasks::class.java, true, null)
        } else {
            if(titleCondition.isBlank()) //ako nije unesen naslov onda se prikazuje toast
                Toast.makeText(this, "Unesite naslov", Toast.LENGTH_SHORT).show()
            if(!taskSizeCondition)  //ako je broj zadataka veci od 30 onda se prikazuje toast
                Toast.makeText(this, "Najveci broj zadataka je 30!", Toast.LENGTH_SHORT).show()

        }
    }

}
