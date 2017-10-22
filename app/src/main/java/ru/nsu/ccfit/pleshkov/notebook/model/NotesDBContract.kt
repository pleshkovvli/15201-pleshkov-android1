package ru.nsu.ccfit.pleshkov.notebook.model

import android.database.Cursor
import android.provider.BaseColumns

object NotesDBContract {
    const val _ID = BaseColumns._ID
    const val _COUNT = BaseColumns._COUNT

    const val TABLE_NAME = "notes"
    const val COLUMN_NAME_TITLE = "title"
    const val COLUMN_NAME_TEXT = "text"
    const val COLUMN_NAME_TIME_CREATED = "time_created"
    const val COLUMN_NAME_TIME_TO_DO = "time_to_do"

    const val SELECT_ALL = "SELECT * FROM ${TABLE_NAME}"
    const val DELETE_ALL = "DELETE FROM ${TABLE_NAME}"
}

interface AllNotesDeleter {
    fun deleteAllNotes()
}