package com.example.taskly.data


import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import kotlinx.serialization.Serializable
import java.time.format.DateTimeFormatter

@Serializable
data class Task(
    val id: String = java.util.UUID.randomUUID().toString(),
    var title: String,
    var description: String,
    var date: String,
    var priority: String,
    var isComplete: Boolean = false,
    var dateChanged: String = ""
) : java.io.Serializable {

    fun getLocalDateTime(): LocalDateTime {
        return LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    }


    companion object {

        fun fromLocalDateTime(title: String, description: String, date: LocalDateTime, priority: String, isComplete: Boolean, dateChanged: String): Task {
            return Task(
                title = title,
                description = description,
                date = date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                priority = priority,
                isComplete = isComplete,
                dateChanged = dateChanged
            )
        }
    }
}
