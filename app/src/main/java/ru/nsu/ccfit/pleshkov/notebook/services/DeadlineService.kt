package ru.nsu.ccfit.pleshkov.notebook.services

import android.app.AlarmManager
import android.app.IntentService
import android.app.Notification.DEFAULT_SOUND
import android.app.Notification.DEFAULT_VIBRATE
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import ru.nsu.ccfit.pleshkov.notebook.R
import ru.nsu.ccfit.pleshkov.notebook.model.NoteStatus
import ru.nsu.ccfit.pleshkov.notebook.model.NotesDBHelper
import ru.nsu.ccfit.pleshkov.notebook.model.editNote
import ru.nsu.ccfit.pleshkov.notebook.model.getNote
import ru.nsu.ccfit.pleshkov.notebook.view.NEXT_STATUS_KEY
import ru.nsu.ccfit.pleshkov.notebook.view.NOTE_ID_KEY
import ru.nsu.ccfit.pleshkov.notebook.view.NOTIFICATION_ONCE

const val NOTIFICATION_ID = 1
const val DEADLINE_SERVICE_NAME = "DeadlineService"

class DeadlineService : IntentService(DEADLINE_SERVICE_NAME) {
    companion object {
        fun newIntent(context: Context, id: Int, statusCode: Int): Intent {
            val intent = Intent(context, DeadlineService::class.java)
            intent.putExtra(NOTE_ID_KEY, id)
            intent.putExtra(NEXT_STATUS_KEY, statusCode)
            return intent
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        if(intent == null) return
        val id = intent.getIntExtra(NOTE_ID_KEY, -1)
        if(id == - 1) return
        val nextStatusCode = intent.getIntExtra(NEXT_STATUS_KEY, 1)
        val status = NoteStatus.getByCode(nextStatusCode)
        val helper = NotesDBHelper(this)
        val writableDb = helper.writableDatabase
        val readableDb = helper.readableDatabase
        runBlocking { writableDb.editNote(id, status = status) }
        val note = runBlocking { readableDb.getNote(id) }
        val builder = NotificationCompat.Builder(this)
                .setDefaults(DEFAULT_SOUND or DEFAULT_VIBRATE)
                .setContentTitle(note.title)
                .setContentText("Task now in status $status")
                .setSmallIcon(R.drawable.ic_alarm_white_24dp)
        val notification = builder.build()
        val notificationsManager = NotificationManagerCompat.from(this)
        notificationsManager.notify(NOTIFICATION_ID, notification)

        if(status.timeToNext != 0L) {
            setNotificationAgain(id, status)
        }

        helper.close()
    }

    private fun setNotificationAgain(id: Int, status: NoteStatus) {
        val notifyIntent = DeadlineReceiver.newIntent(this)
        notifyIntent.putExtra(NOTE_ID_KEY, id)
        notifyIntent.putExtra(NEXT_STATUS_KEY, status.nextCode)
        val pending = pendingNotifyIntent(notifyIntent)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + status.timeToNext, pending)
    }

    private fun pendingNotifyIntent(notifyIntent: Intent) =
        PendingIntent.getBroadcast(this, NOTIFICATION_ONCE, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)

}