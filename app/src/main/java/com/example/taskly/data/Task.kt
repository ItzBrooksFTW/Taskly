package com.example.taskly.data


import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import kotlinx.serialization.Serializable
import java.time.format.DateTimeFormatter

@Serializable
data class Task(
    val title: String,
    val description: String,
    val date: String,
    val priority: String
) : java.io.Serializable {
    @RequiresApi(Build.VERSION_CODES.O)
    fun getLocalDateTime(): LocalDateTime {
        return LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun fromLocalDateTime(title: String, description: String, date: LocalDateTime, priority: String): Task {
            return Task(title, description, date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), priority)
        }
    }
}
