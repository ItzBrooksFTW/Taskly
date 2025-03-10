package com.brooks.taskly

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.brooks.taskly.data.Task
import com.brooks.taskly.utils.TaskAdapter
import com.brooks.taskly.utils.TaskStorage
import com.brooks.taskly.utils.switchScreens
import java.time.LocalDateTime
import androidx.core.graphics.createBitmap
import com.brooks.taskly.utils.checkList
import com.brooks.taskly.utils.checkTheme


class ActivityAllTasks : AppCompatActivity() {
    private lateinit var taskStorage: TaskStorage
    private var taskList: MutableList<Task> = mutableListOf()
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var sortedTaskList: MutableList<Task>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_all_tasks)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkTheme(this)
        taskStorage = TaskStorage(this)
        taskList = taskStorage.loadTasks().toMutableList()



        checkList(this)  //provjerava je li lista prazna i prikazuje poruku ako je


        var sortOrder= true
        val buttonBack: Button = findViewById(R.id.buttonBack)
        val spinner: Spinner = findViewById(R.id.spinnerSorting)
        val buttonSort: ImageButton = findViewById(R.id.buttonSort)

        taskAdapter = TaskAdapter(this, taskList)
        val listView: ListView = findViewById(R.id.listViewTasks)
        listView.adapter = taskAdapter

        buttonBack.setOnClickListener {

            switchScreens(this, MainActivity::class.java, true, null)
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.dropdown_values_sort,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_item)
            spinner.adapter = adapter
        }
        spinner.setSelection(0, false)


        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                sortTasks(sortOrder)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                //nista
            }
        }
        buttonSort.setOnClickListener {
            sortOrder=!sortOrder
            sortTasks(sortOrder)
            Log.d("sortOrder", sortOrder.toString())

            val flippedBitmap=flipDrawable(this, R.drawable.arrow_colored, sortOrder)
            buttonSort.setImageBitmap(flippedBitmap)
        }









    }
    override fun onBackPressed() {
        super.onBackPressed()
        switchScreens(this, MainActivity::class.java)
    }

    private fun sortTasks(sortOrder:Boolean) {


        val spinnerSortBy: Spinner = findViewById(R.id.spinnerSorting)
        val sortChoice = spinnerSortBy.selectedItemPosition

        Log.d("taskovi prije", "Loaded tasks: $taskList")

        val priorityArray=resources.getStringArray(R.array.dropdown_values_priority)

        val priorityMap = mapOf(
            priorityArray[2] to 0, //niski prioritet
            priorityArray[1] to 1,  //srednji prioritet
            priorityArray[0] to 2    //visoki prioritet

        )

       val taskListCopy = taskList.map { it.copy() }.toMutableList()

        sortedTaskList = when (sortChoice) {



            1 -> { //sortiranje po datumu

                if (sortOrder) {
                    taskListCopy.sortedBy { LocalDateTime.parse(it.date) }.also{
                        Log.d("taskovidatum", "Loaded tasks: $taskList")
                    }
                } else {
                    taskListCopy.sortedByDescending { LocalDateTime.parse(it.date) }.also{
                        Log.d("taskovidatum2", "Loaded tasks: $taskList")
                    }
                }
            }
            2 -> { //sortiranje po prioritetu
                if (sortOrder) {
                    taskListCopy.sortedBy { priorityMap[it.priority]?:0 }
                } else {
                    taskListCopy.sortedByDescending { priorityMap[it.priority]?:0 }
                }
            }

            else -> { //zadano sortiranje
                Log.d("taskoviDefault", "Loaded tasks: $taskList")
                taskStorage.loadTasks()

            }
        }.toMutableList()

        taskAdapter.updateTasks(sortedTaskList)
    }


    //funkcija za okretanje strelice
    private fun flipDrawable(context: Context, drawableId: Int, orientation: Boolean): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId) ?: return createBitmap(1, 1)
        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        val matrix = Matrix()
        if (orientation) { //ako je true onda je strelica prema gore
            matrix.preScale(-1.0f, 1.0f)
        } else { //ako je false onda je strelica prema dolje
            matrix.preScale(1.0f, -1.0f)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}