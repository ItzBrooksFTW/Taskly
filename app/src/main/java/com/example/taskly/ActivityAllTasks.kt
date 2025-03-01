package com.example.taskly

import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taskly.data.Task
import com.example.taskly.utils.TaskAdapter
import com.example.taskly.utils.TaskStorage
import com.example.taskly.utils.switchScreens


class ActivityAllTasks : AppCompatActivity() {
    private lateinit var taskStorage: TaskStorage
    private lateinit var taskList: MutableList<Task>
    private lateinit var taskAdapter: TaskAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_all_tasks)
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

        taskAdapter = TaskAdapter(this, taskList)
        val listView: ListView = findViewById(R.id.listViewTasks)
        listView.adapter = taskAdapter
    }
}