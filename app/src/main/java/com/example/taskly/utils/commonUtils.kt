package com.example.taskly.utils

import android.app.Activity
import android.content.Context

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.example.taskly.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


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
    background?.setColor(Color.parseColor(color))
    activity.background = background
}

fun formatDateTime(
    dateTime: LocalDateTime,
    pattern: String
): String {
    val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
    return dateTime.format(formatter)
}



