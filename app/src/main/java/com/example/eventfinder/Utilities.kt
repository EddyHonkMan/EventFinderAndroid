package com.example.eventfinder

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*


object Utilities {
    val inputDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    val outputDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    val eventDetailDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val inputTimeFormatter = SimpleDateFormat("HH:mm:ss", Locale.US)
    val outputTimeFormatter = SimpleDateFormat("h:mm a", Locale.US)

    fun showSnackbar(view: View, text: String) {
        val snackbar = Snackbar.make(
            (view.context as AppCompatActivity).findViewById(R.id.content),
            text,
            Snackbar.LENGTH_LONG
        )
        snackbar.setTextColor(Color.BLACK)
        snackbar.show()
    }

    //the following function is inspired by https://stackoverflow.com/questions/9891360/getting-activity-from-context-in-android/46205896#46205896
    fun getActivityLifeCycleScope(context: Context?): LifecycleCoroutineScope? {
        if (context == null) {
            return null
        } else if (context is ContextWrapper) {
            return if (context is LifecycleOwner) {
                (context as LifecycleOwner).lifecycleScope
            } else {
                getActivityLifeCycleScope(context.baseContext)
            }
        }
        return null
    }
}