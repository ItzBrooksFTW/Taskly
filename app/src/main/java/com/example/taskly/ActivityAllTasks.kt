package com.example.taskly

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
import com.example.taskly.data.Task
import com.example.taskly.utils.TaskAdapter
import com.example.taskly.utils.TaskStorage
import com.example.taskly.utils.switchScreens
import java.time.LocalDateTime
import androidx.core.graphics.createBitmap


class ActivityAllTasks : AppCompatActivity() {
    private lateinit var taskStorage: TaskStorage
    private lateinit var taskList: MutableList<Task>
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
        taskStorage = TaskStorage(this)
        taskList = taskStorage.loadTasks().toMutableList()

        var sortOrder= true
        val buttonBack: Button = findViewById(R.id.buttonBack)
        val spinner: Spinner = findViewById(R.id.spinnerSorting)
        val buttonSort: ImageButton = findViewById(R.id.buttonSort)

        buttonBack.setOnClickListener {

            switchScreens(this, MainActivity::class.java, true, null)
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.dropdown_values_sort,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
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

            val flippedBitmap=flipDrawable(this, R.drawable.arrow, sortOrder)
            buttonSort.setImageBitmap(flippedBitmap)
        }




        taskAdapter = TaskAdapter(this, taskList)
        val listView: ListView = findViewById(R.id.listViewTasks)
        listView.adapter = taskAdapter




    }
    private fun sortTasks(sortOrder:Boolean) {


        val spinnerSortBy: Spinner = findViewById(R.id.spinnerSorting)
        val sortChoice = spinnerSortBy.selectedItem.toString()

        Log.d("taskovi prije", "Loaded tasks: $taskList")

        val priorityMap = mapOf(
            "Niski" to 0,
            "Srednji" to 1,
            "Visoki" to 2
        )



        sortedTaskList = when (sortChoice) {



            "Po datumu" -> {

                if (sortOrder) {
                    taskList.sortedBy { LocalDateTime.parse(it.date) }
                } else {
                    taskList.sortedByDescending { LocalDateTime.parse(it.date) }
                }
            }
            "Po prioritetu" -> {
                if (sortOrder) {
                    taskList.sortedBy { priorityMap[it.priority]?:0 }
                } else {
                    taskList.sortedByDescending { priorityMap[it.priority]?:0 }
                }
            }

            else -> {
                Log.d("taskovi", "Loaded tasks: $taskList")
                taskStorage.loadTasks()

            }
        }.toMutableList()

        taskAdapter.updateTasks(sortedTaskList)
    }

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