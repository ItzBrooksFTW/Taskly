// NotificationReceiver.kt
package com.example.taskly

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.example.taskly.utils.formatDateTime
import java.time.LocalDateTime
import com.example.taskly.utils.TaskStorage

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title")
       val date = formatDateTime(LocalDateTime.parse(intent.getStringExtra("date")), "dd.MM.yyyy HH:mm")

        //provjerava ako je zadatak izvrsen, ako je onda ne salje notifikaciju
        val id = intent.getStringExtra("id")
        val taskList = TaskStorage(context).loadTasks()

        val task = taskList.find { it.id == id } ?: return

        if(task.isComplete) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "task_channel"

        val channel = NotificationChannel(channelId, "Task Notifications", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        //da se otvori aplikacija kad se klikne na notifikaciju
        val openAppIntent = Intent(context, ActivityAllTasks::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.exclamation_icon)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(largeIcon)
            .setContentTitle("Rok za zadatak istekao")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Zadatak: $title\n$date"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(0, notification)
    }
}