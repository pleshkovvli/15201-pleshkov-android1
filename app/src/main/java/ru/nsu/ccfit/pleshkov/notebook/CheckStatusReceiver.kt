package ru.nsu.ccfit.pleshkov.notebook

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CheckStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val serviceIntent = Intent(context, NotificationSenderService::class.java)
        context?.startService(serviceIntent)
    }
}