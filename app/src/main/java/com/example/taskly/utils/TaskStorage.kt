package com.example.taskly.utils

import android.content.Context

import com.example.taskly.data.Task

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class TaskStorage(context: Context) {
    private val json = Json { prettyPrint = true }
    private val file = File(context.filesDir, "tasks.json")

    fun saveTasks(taskList: List<Task>) {
        val jsonString = json.encodeToString(taskList)
        file.writeText(jsonString)
    }

    fun loadTasks(): List<Task> {
        if (!file.exists()) return emptyList()
        val jsonString = file.readText()
        return json.decodeFromString(jsonString)
    }


}