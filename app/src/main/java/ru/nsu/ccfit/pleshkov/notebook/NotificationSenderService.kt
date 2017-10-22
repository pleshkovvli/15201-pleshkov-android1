package ru.nsu.ccfit.pleshkov.notebook

import android.app.IntentService
import android.app.Notification
import android.content.Intent
import android.support.v4.app.NotificationCompat

const val NOTIFICATION_ID = 3

class NotificationSenderService : IntentService("NotificationSenderService") {
    override fun onHandleIntent(intent: Intent?) {
        val builder = NotificationCompat.Builder(this)
        builder.setContentTitle("")
    }
}