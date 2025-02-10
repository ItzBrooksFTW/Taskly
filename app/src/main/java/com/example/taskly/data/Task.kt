package com.example.taskly.data

import java.time.LocalDateTime


data class Task(

    val title: String,
    val description: String,
    val date: LocalDateTime,
    val priority: String
    )
