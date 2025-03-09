package com.brooks.taskly.utils

import android.Manifest
import android.animation.Animator
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.brooks.taskly.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.core.graphics.toColorInt
import android.animation.AnimatorListenerAdapter
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.TypedValue
import android.widget.Toast
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import com.brooks.taskly.ActivityAllTasks
import com.brooks.taskly.data.Task
import java.time.Duration

fun checkForPerms(activity: Activity, registry: ActivityResultRegistry) {
    Log.d("checkForPerms", "Checking for permissions")
    val requestExactAlarmPermissionLauncher = registry.register(
        "requestExactAlarmPermission",
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!(activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()) {
                Toast.makeText(activity, "Dozvola za alarm odbijena", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val requestNotificationPermissionLauncher = registry.register(
        "requestNotificationPermission",
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(activity, "Dozvola za notifikaciju odbijena", Toast.LENGTH_SHORT).show()
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            requestExactAlarmPermissionLauncher.launch(intent)
        }
    }
}



fun switchScreens(
    activity1: Activity,
    activity2: Class<*>,
    finishCurrent: Boolean = false,
    bundle: Bundle?=null
){
    val intent = Intent(activity1, activity2)
    if (bundle != null) {
        intent.putExtras(bundle)
    }
    startActivity(activity1, intent, null)
    if (finishCurrent) {
        activity1.finish()
    }

}

fun changeBackgroundColor(

    activity : View,
    color: String
)
{


    val background = ContextCompat.getDrawable(activity.context, R.drawable.rounded_border)?.mutate() as? GradientDrawable
    background?.setColor(color.toColorInt())
    activity.background = background
}

fun formatDateTime(
    dateTime: LocalDateTime
): String {
    val pattern="dd.MM.yyyy HH:mm"
    val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
    return dateTime.format(formatter)
}

fun fadeInOut(view: View, show: Boolean) {
    if (show) {
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate()
            .alpha(1f)
            .setDuration(300)
            .setListener(null)
    } else {
        view.animate()
            .alpha(0f)
            .setDuration(300)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                }
            })
    }
}
fun getThemeColor(context: Context, attribute: Int): Int {
    val typedValue = TypedValue()
    val theme = context.theme
    theme.resolveAttribute(attribute, typedValue, true)
    return typedValue.data
}

fun checkList(context:Context) {
    val taskList: MutableList<Task> = TaskStorage(context).loadTasks().toMutableList()
    val emptyListWarning: TextView =(context as ActivityAllTasks).findViewById(R.id.emptyListWarning)
    if(taskList.isEmpty()){
        emptyListWarning.visibility=View.VISIBLE
    }
    else{
        emptyListWarning.visibility=View.GONE
    }

}

fun checkTheme(context:Context): Int{
    val sharedPreferences = context.getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
    val selectedTheme = sharedPreferences.getInt("selectedTheme", 0)
    when (selectedTheme) {
        0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
    return selectedTheme
}

fun getDifferenceFromNow(taskDate: String): String {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val taskDateTime = LocalDateTime.parse(taskDate, formatter)
    val currentDateTime = LocalDateTime.now()

    return if (taskDateTime.isBefore(currentDateTime)) {
        val duration = Duration.between(taskDateTime, currentDateTime)
        val days = duration.toDays()
        val hours = duration.toHours() % 24
        val minutes = duration.toMinutes() % 60
        when{
            days>0 -> "+${days}d ${hours}h ${minutes}m"
            hours>0 -> "+${hours}h ${minutes}m"
            minutes>0 -> "+${minutes}m"
            else->""
        }
    } else {
        ""
    }
}







