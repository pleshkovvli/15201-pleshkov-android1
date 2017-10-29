package ru.nsu.ccfit.pleshkov.notebook.model

import android.content.Context
import android.content.SharedPreferences
import android.text.format.DateUtils
import ru.nsu.ccfit.pleshkov.notebook.R

class SettingsApi(private val context: Context) {
    private val preferences: SharedPreferences

    private val urgentKey = context.getString(R.string.urgent_key)
    private val primaryKey = context.getString(R.string.primary_key)
    private val usualKey = context.getString(R.string.usual_key)
    private val somedayKey = context.getString(R.string.someday_key)

    init {
        val preferencesKey = context.getString(R.string.preferences_key)
        preferences = context.getSharedPreferences(preferencesKey, Context.MODE_PRIVATE)
    }

    fun initSettings() {
        val urgentTime = preferences.getLong(urgentKey, -1)
        if(urgentTime != -1L) {
            return
        }

        val editor = preferences.edit()
        editor.putLong(urgentKey, DateUtils.MINUTE_IN_MILLIS)
        editor.putLong(primaryKey, 2L * DateUtils.MINUTE_IN_MILLIS)
        editor.putLong(usualKey, 3L * DateUtils.MINUTE_IN_MILLIS)
        editor.putLong(somedayKey,6L * DateUtils.MINUTE_IN_MILLIS)
        editor.apply()
    }

    fun setSettings(settings: Array<StatusSettings>) {
        val editor = preferences.edit()
        editor.putLong(context.getString(R.string.urgent_key), settings[0].getMillis())
        editor.putLong(context.getString(R.string.primary_key), settings[1].getMillis())
        editor.putLong(context.getString(R.string.usual_key), settings[2].getMillis())
        editor.putLong(context.getString(R.string.someday_key), settings[3].getMillis())
        editor.apply()
    }

    fun settingsNames() = arrayOf(
            NoteStatus.URGENT.toString(),
            NoteStatus.PRIMARY.toString(),
            NoteStatus.USUAL.toString(),
            NoteStatus.SOMEDAY.toString()
    )

    fun getSettings() = arrayOf(
            StatusSettings(NoteStatus.URGENT, timeToDoFromStatus(NoteStatus.URGENT)),
            StatusSettings(NoteStatus.PRIMARY,  timeToDoFromStatus(NoteStatus.PRIMARY)),
            StatusSettings(NoteStatus.USUAL,  timeToDoFromStatus(NoteStatus.USUAL)),
            StatusSettings(NoteStatus.SOMEDAY,  timeToDoFromStatus(NoteStatus.SOMEDAY))
    )

    fun timeToDoFromStatus(status: NoteStatus) = when(status) {
        NoteStatus.FAILED -> 0L
        NoteStatus.URGENT -> preferences.getLong(urgentKey, -1L)
        NoteStatus.PRIMARY -> preferences.getLong(primaryKey, -1L)
        NoteStatus.USUAL -> preferences.getLong(usualKey, -1L)
        NoteStatus.SOMEDAY -> preferences.getLong(somedayKey, -1L)
        else -> -1L
    }

    fun statusByTimeToDo(timeToDo: Long, isNext: Boolean) : NoteStatus {
        if(timeToDo < 0) {
            return NoteStatus.UNKNOWN
        }

        val urgentTime = preferences.getLong(urgentKey, -1L)
        val primaryTime = preferences.getLong(primaryKey, -1L)
        val usualTime = preferences.getLong(usualKey, -1L)
        val somedayTime = preferences.getLong(somedayKey, -1L)

        val ttdRest = timeToDo - System.currentTimeMillis()

        return if(!isNext) when {
            ttdRest < 0 -> NoteStatus.FAILED
            ttdRest < urgentTime -> NoteStatus.URGENT
            ttdRest < primaryTime -> NoteStatus.PRIMARY
            ttdRest < usualTime -> NoteStatus.USUAL
            ttdRest < somedayTime -> NoteStatus.SOMEDAY
            else -> NoteStatus.UNNECESSARY
        } else when {
            ttdRest < 0 -> NoteStatus.UNKNOWN
            ttdRest < urgentTime -> NoteStatus.FAILED
            ttdRest < primaryTime -> NoteStatus.URGENT
            ttdRest < usualTime -> NoteStatus.PRIMARY
            ttdRest < somedayTime -> NoteStatus.USUAL
            else -> NoteStatus.SOMEDAY
        }
    }

}
