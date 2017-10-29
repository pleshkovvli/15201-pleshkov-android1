package ru.nsu.ccfit.pleshkov.notebook.services

import android.app.IntentService
import android.app.Notification.DEFAULT_SOUND
import android.app.Notification.DEFAULT_VIBRATE
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import kotlinx.coroutines.experimental.runBlocking
import ru.nsu.ccfit.pleshkov.notebook.R
import ru.nsu.ccfit.pleshkov.notebook.model.*
import ru.nsu.ccfit.pleshkov.notebook.presenter.setNotification
import ru.nsu.ccfit.pleshkov.notebook.view.MainActivity
import ru.nsu.ccfit.pleshkov.notebook.view.NOTE_ID_KEY

const val NOTIFICATION_ID = 1
const val DEADLINE_SERVICE_NAME = "DeadlineService"

class DeadlineService : IntentService(DEADLINE_SERVICE_NAME) {
    companion object {
        fun newIntent(context: Context, id: Int): Intent {
            val intent = Intent(context, DeadlineService::class.java)
            intent.putExtra(NOTE_ID_KEY, id)
            return intent
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        if(intent == null) return
        val id = intent.getIntExtra(NOTE_ID_KEY, -1)
        if(id == - 1) return

        val helper = NotesDBHelper(this)
        val writableDb = helper.writableDatabase
        val readableDb = helper.readableDatabase
        val note = runBlocking { readableDb.selectNote(id) }
        if(note.status == NoteStatus.DONE) {
            return
        }
        val settingsApi = SettingsApi(this)

        val status = settingsApi.statusByTimeToDo(
                note.timeToDo - 100,
                false
        )
        val nextStatus = settingsApi.statusByTimeToDo(
                note.timeToDo - 100,
                true
        )
        runBlocking { writableDb.editNote(id, status = status) }
        val builder = NotificationCompat.Builder(this)
                .setDefaults(DEFAULT_SOUND or DEFAULT_VIBRATE)
                .setContentTitle(note.title)
                .setContentText("Task now in status $status")
                .setSmallIcon(R.drawable.ic_alarm_white_24dp)
        val notification = builder.build()
        val notificationsManager = NotificationManagerCompat.from(this)
        notificationsManager.notify(NOTIFICATION_ID, notification)

        if(nextStatus != NoteStatus.UNKNOWN) {
            val time = note.timeToDo - settingsApi.timeToDoFromStatus(nextStatus)
            setNotification(id, time, this)
            val intentToRenew = MainActivity.newIntentToRenew(note.id)

            Log.d("RENEW", "Sended??")
            LocalBroadcastManager.getInstance(this).sendBroadcast(intentToRenew)
        }

        helper.close()
    }
}