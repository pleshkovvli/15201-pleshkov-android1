package ru.nsu.ccfit.pleshkov.notebook.model

import android.provider.BaseColumns

object NotesDBContract {
    const val _ID = BaseColumns._ID
    const val _COUNT = BaseColumns._COUNT

    const val TABLE_NAME = "notes"
    const val COLUMN_NAME_TITLE = "title"
    const val COLUMN_NAME_TEXT = "text"
    const val COLUMN_NAME_TIME_CREATED = "time_created"
    const val COLUMN_NAME_TIME_UPDATED = "time_updated"
    const val COLUMN_NAME_TIME_TO_DO = "time_to_do"
    const val COLUMN_NAME_STATUS = "status"
    const val COLUMN_NAME_SET_BY_USER = "status_set_by_user"

    const val SELECT_ALL = "SELECT * FROM $TABLE_NAME"
    const val DELETE_ALL = "DELETE FROM $TABLE_NAME"
}

interface AllNotesDeleter {
    fun deleteAllNotes()
}