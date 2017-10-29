package ru.nsu.ccfit.pleshkov.notebook.model

import android.text.format.DateUtils

data class StatusSettings(
        val status: NoteStatus,
        var days: Int,
        var hours: Int,
        var minutes: Int
) {
    constructor(status: NoteStatus, millis: Long) : this(
            status,
            (millis / DateUtils.DAY_IN_MILLIS).toInt(),
            (millis / DateUtils.HOUR_IN_MILLIS  % 24).toInt(),
            (millis / DateUtils.MINUTE_IN_MILLIS % 60).toInt()
    )

    fun getMillis() : Long {
        val daysMillis = days.toLong() * DateUtils.DAY_IN_MILLIS
        val hoursMillis = hours.toLong() * DateUtils.HOUR_IN_MILLIS
        val minutesMillis = minutes.toLong() * DateUtils.MINUTE_IN_MILLIS
        return daysMillis + hoursMillis + minutesMillis
    }
}