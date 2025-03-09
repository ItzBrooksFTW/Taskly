package com.brooks.taskly.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.brooks.taskly.ActivityAllTasks
import com.brooks.taskly.ActivityNewTask
import com.brooks.taskly.data.Task
import com.brooks.taskly.R
import java.time.LocalDateTime


class TaskAdapter(private val context: Context, private val taskList: MutableList<Task>) : ArrayAdapter<Task>(context, 0, taskList) {

    private val taskStorage = TaskStorage(context)

    private var showTaskList: MutableList<Task> = taskList.map { it.copy() }.toMutableList()


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_task, parent, false)
        val task = showTaskList[position] //
        //val task = taskList[position]

        val textViewTaskTitle = view.findViewById<TextView>(R.id.textViewTaskTitle)
        val textViewTaskDate = view.findViewById<TextView>(R.id.textViewTaskDate)
        val checkBoxComplete = view.findViewById<CheckBox>(R.id.checkBoxComplete)
        val buttonDelete = view.findViewById<Button>(R.id.buttonDelete)
        val buttonEdit = view.findViewById<Button>(R.id.buttonEdit)
        val overDue = view.findViewById<TextView>(R.id.textViewTaskOverDue)


        val date=formatDateTime(LocalDateTime.parse(task.date))

        val textViewTaskPriority = view.findViewById<TextView>(R.id.textViewTaskPriority)

        textViewTaskTitle.text = task.title
        textViewTaskDate.text = date
        textViewTaskPriority.text= task.priority

        overDue.text= getDifferenceFromNow(task.date)



        checkBoxComplete.setOnCheckedChangeListener { _, isChecked ->

            val originalTask = taskList.find { it.id == task.id }
            if (originalTask != null) {
                originalTask.isComplete = isChecked
                taskStorage.saveTasks(taskList)
            }
            if (isChecked) {
                textViewTaskTitle.setTextColor(getThemeColor(context, android.R.attr.textColorSecondary))
            } else {
                textViewTaskTitle.setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
            }

        }

        checkBoxComplete.isChecked = task.isComplete


        buttonDelete.setOnClickListener {


        showDeleteConfirmationDialog(task)
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
        showTaskList.addAll(newTaskList.map { it.copy() })
        Log.d("taskoviUpdate", "Loaded tasks: $taskList\n$showTaskList")
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
        val textViewTaskOverDuePopup = popupView.findViewById<TextView>(R.id.textViewTaskOverDuePopup)

        val dateChanged: String = if(task.dateChanged.isNotBlank()) {
            formatDateTime(LocalDateTime.parse(task.dateChanged))
        } else{
            context.getString(R.string.no_change)
        }


        textViewTaskTitle.text = task.title
        textViewTaskDescription.text = task.description.ifBlank { context.getString(R.string.no_description) }
        textViewTaskDate.text = context.getString(R.string.dueDate, date)
        textViewTaskOverDuePopup.text= getDifferenceFromNow(task.date)
        textViewTaskPriority.text = context.getString(R.string.priority2, task.priority)
        textViewTaskDateChanged.text =(context.getString(R.string.date_changed, dateChanged)).takeIf { task.dateChanged.isNotBlank() } ?: dateChanged

        val popupWindow = PopupWindow(popupView,convertDpToPx(context, 330), ViewGroup.LayoutParams.WRAP_CONTENT , true)
        popupWindow.isOutsideTouchable = false
        //TODO animacija
        popupWindow.showAtLocation(anchorView, android.view.Gravity.CENTER, 0, 0)

        //popupView.setBackgroundResource(R.drawable.rounded_border)
        if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_NO) {
            changeBackgroundColor(popupView, "#FFFFFF")
        }


    }
    private fun findTaskPosition(task: Task): Int?{

        return taskList.indexOfFirst{it.id==task.id}.takeIf { it != -1 }
    }
    private fun convertDpToPx(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()
    }

    private fun showDeleteConfirmationDialog(task: Task) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.delete_task))
        builder.setMessage(context.getString(R.string.delete_task_message))
        builder.setPositiveButton(context.getString(R.string.delete_task_confirm)) { dialog, _ ->
            taskList.remove(task)
            showTaskList.remove(task)
            taskStorage.saveTasks(taskList)
            notifyDataSetChanged()
            checkList(context as ActivityAllTasks)
            dialog.dismiss()
            Toast.makeText(context, context.getString(R.string.delete_success), Toast.LENGTH_SHORT, ).show()

        }
        builder.setNegativeButton(context.getString(R.string.delete_task_cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

}