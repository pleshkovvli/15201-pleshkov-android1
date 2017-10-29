package ru.nsu.ccfit.pleshkov.notebook.presenter

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import ru.nsu.ccfit.pleshkov.notebook.R
import ru.nsu.ccfit.pleshkov.notebook.model.NoteStatus
import ru.nsu.ccfit.pleshkov.notebook.services.DeadlineReceiver
import ru.nsu.ccfit.pleshkov.notebook.view.NOTE_ID_KEY
import ru.nsu.ccfit.pleshkov.notebook.view.NOTIFICATION_ONCE

fun View.getColor(status: NoteStatus) = when(status) {
    NoteStatus.UNKNOWN -> getColorById(R.color.unknown_color)
    NoteStatus.URGENT -> getColorById(R.color.urgent_color)
    NoteStatus.PRIMARY -> getColorById(R.color.primary_color)
    NoteStatus.USUAL -> getColorById(R.color.usual_color)
    NoteStatus.SOMEDAY -> getColorById(R.color.someday_color)
    NoteStatus.UNNECESSARY -> getColorById(R.color.unnecessary_color)
    NoteStatus.FAILED -> getColorById(R.color.failed_color)
    NoteStatus.DONE -> getColorById(R.color.done_color)
    NoteStatus.CANCELLED -> getColorById(R.color.cancelled_color)
}

fun View.getColorById(id: Int) = ContextCompat.getColor(context, id)

fun setNotification(id: Int, time: Long, context: Context) {
    val notifyIntent = DeadlineReceiver.newIntent(context)
    notifyIntent.putExtra(NOTE_ID_KEY, id)
    val pending = pendingNotifyIntent(notifyIntent, context)
    Log.d("NOTIF", "Set on ${niceFormattedTime(time)}")
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            time,
            pending
    )
}

fun pendingNotifyIntent(notifyIntent: Intent, context: Context) =
        PendingIntent.getBroadcast(
                context,
                NOTIFICATION_ONCE,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
