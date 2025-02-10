package com.example.taskly

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taskly.utils.*


class MainActivity : AppCompatActivity() {

    private lateinit var buttonNewTask : Button
    private lateinit var buttonAllTasks : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        buttonNewTask = findViewById(R.id.buttonNewTask)
        buttonAllTasks =findViewById(R.id.buttonAllTasks)


        buttonNewTask.setOnClickListener(){
            switchScreens(this, ActivityNewTask::class.java, true, null)

        }

        buttonAllTasks.setOnClickListener(){
            switchScreens(this, ActivityAllTasks::class.java, true, null)

        }

    }
}