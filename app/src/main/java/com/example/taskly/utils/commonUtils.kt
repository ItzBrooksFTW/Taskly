package com.example.taskly.utils

import android.app.Activity

import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity


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


