// NotificationReceiver.kt
package com.brooks.taskly

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.brooks.taskly.utils.formatDateTime
import java.time.LocalDateTime
import com.brooks.taskly.utils.TaskStorage
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {



        val id = intent.getStringExtra("id")
        val taskList = TaskStorage(context).loadTasks()

        val task = taskList.find { it.id == id } ?: return //ako zadatak nije pronadjen ne treba se prikazati notifikacija
        Log.d("provjera2", "postoji notifikacija")

        if(task.isComplete) return  //ako je zadatak vec zavrsen ne treba se prikazati notifikacija

        val title = task.title
        val date = formatDateTime(LocalDateTime.parse(task.date))

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "task_channel"

        val channel = NotificationChannel(channelId, "Obavijesti za zadatke", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel) //kreira se kanal za notifikacije

        //da se otvori aplikacija na zaslonu allTasks kad se klikne na notifikaciju
        val openAppIntent = Intent(context, ActivityAllTasks::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        //postavljanje ikone za notifikaciju
        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.exclamation_icon)

        //kreiranje notifikacije
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(largeIcon)
            .setContentTitle("Rok za zadatak istekao")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Zadatak: $title\n$date"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(0, notification) //prikazuje notifikaciju
    }
}