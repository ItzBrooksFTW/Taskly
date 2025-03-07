package com.example.taskly.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.taskly.ActivityNewTask
import com.example.taskly.data.Task
import com.example.taskly.R
import java.time.LocalDateTime

class TaskAdapter(private val context: Context, private val taskList: MutableList<Task>) : ArrayAdapter<Task>(context, 0, taskList) {

    private val taskStorage = TaskStorage(context)
    private val showTaskList: MutableList<Task> = taskList //


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_task, parent, false)
        val task = showTaskList[position] //
        //val task = taskList[position]

        val textViewTaskTitle = view.findViewById<TextView>(R.id.textViewTaskTitle)
        val textViewTaskDate = view.findViewById<TextView>(R.id.textViewTaskDate)
        val checkBoxComplete = view.findViewById<CheckBox>(R.id.checkBoxComplete)
        val buttonDelete = view.findViewById<Button>(R.id.buttonDelete)
        val buttonEdit = view.findViewById<Button>(R.id.buttonEdit)
        val date=formatDateTime(LocalDateTime.parse(task.date))

        val textViewTaskPriority = view.findViewById<TextView>(R.id.textViewTaskPriority)

        textViewTaskTitle.text = task.title
        textViewTaskDate.text = date
        textViewTaskPriority.text= task.priority

        checkBoxComplete.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                textViewTaskTitle.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                task.isComplete=true

                taskStorage.saveTasks(taskList)
                Log.d("ActivityNewTaskEdit", "Loaded tasks: $taskList")
            } else {
                textViewTaskTitle.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                task.isComplete=false

                taskStorage.saveTasks(taskList)
                Log.d("ActivityNewTaskEdit", "Loaded tasks: $taskList")
            }
        }

        checkBoxComplete.isChecked = task.isComplete


        buttonDelete.setOnClickListener {
            taskList.remove(task)
            showTaskList.remove(task) //
            taskStorage.saveTasks(taskList)
            notifyDataSetChanged()
        }

        buttonEdit.setOnClickListener {

            val intent = Intent(context, ActivityNewTask::class.java)
            intent.putExtra("taskID", task.id)
            context.startActivity(intent)
        }

        view.setOnClickListener {

                showTaskDetailsPopup(task, view, date)

        }

        return view
    }
    fun updateTasks(newTaskList: MutableList<Task>) {

        showTaskList.clear()
        showTaskList.addAll(newTaskList)

        //taskList.clear()
        //taskList.addAll(newTaskList)
        notifyDataSetChanged()
    }

    private fun showTaskDetailsPopup(task: Task, anchorView: View, date : String) {
        val popupView = LayoutInflater.from(context).inflate(R.layout.popup_task_details, null)

        val textViewTaskTitle = popupView.findViewById<TextView>(R.id.textViewTaskTitle)
        val textViewTaskDescription = popupView.findViewById<TextView>(R.id.textViewTaskDescription)
        val textViewTaskDate = popupView.findViewById<TextView>(R.id.textViewTaskDate)
        val textViewTaskPriority = popupView.findViewById<TextView>(R.id.textViewTaskPriority)
        val textViewTaskDateChanged=popupView.findViewById<TextView>(R.id.textViewTaskDateChanged)
        val dateChanged: String
        if(task.dateChanged.isNotBlank()) {
            dateChanged = formatDateTime(LocalDateTime.parse(task.dateChanged))
        }
        else{
            dateChanged = "Nema izmjene"
        }


        textViewTaskTitle.text = task.title
        textViewTaskDescription.text = task.description
        textViewTaskDate.text = date
        textViewTaskPriority.text = task.priority
        textViewTaskDateChanged.text = dateChanged

        val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.showAtLocation(anchorView, android.view.Gravity.CENTER, 0, 0)
        popupView.setBackgroundResource(R.drawable.rounded_border)
        changeBackgroundColor(popupView, "#FFFFFF")
//TODO 2: fix the popup window background color

        popupView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_OUTSIDE) {
                popupWindow.dismiss()
                true
            } else {
                v.performClick()
                false
            }
        }
    }
    private fun findTaskPosition(task: Task): Int?{

        return taskList.indexOfFirst{it.id==task.id}.takeIf { it != -1 }
    }

}