package ru.nsu.ccfit.pleshkov.notebook.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import ru.nsu.ccfit.pleshkov.notebook.view.NEXT_STATUS_KEY
import ru.nsu.ccfit.pleshkov.notebook.view.NOTE_ID_KEY

class DeadlineReceiver : BroadcastReceiver() {
    companion object {
        fun newIntent(context: Context): Intent = Intent(context, DeadlineReceiver::class.java)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if(context == null || intent == null) {
            return
        }
        val id = intent.getIntExtra(NOTE_ID_KEY, -1)
        val statusCode = intent.getIntExtra(NEXT_STATUS_KEY, -1)
        val serviceIntent = DeadlineService.newIntent(context, id, statusCode)
        context.startService(serviceIntent)
        Log.d("NOTIF", "service started")
    }
}
