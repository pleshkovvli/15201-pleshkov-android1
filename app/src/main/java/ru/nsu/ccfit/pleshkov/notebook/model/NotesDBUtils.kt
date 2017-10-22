package ru.nsu.ccfit.pleshkov.notebook.model

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

fun Cursor.getNote() = Note(getTitle(), getText(), getTimeCreated(), getTimeToDo())

suspend fun SQLiteDatabase.getAllNotes() : ArrayList<Note> {
    val notesList = ArrayList<Note>()

    val cursor = this.rawQuery(NotesDBContract.SELECT_ALL, null)
    cursor.use {
        while (cursor.moveToNext()) {
            notesList.add(cursor.getNote())
        }
    }

    return notesList
}


private fun Cursor.getTitle() : String = this.getString(this.getColumnIndex(NotesDBContract.COLUMN_NAME_TITLE))
private fun Cursor.getText() : String = this.getString(this.getColumnIndex(NotesDBContract.COLUMN_NAME_TEXT))
private fun Cursor.getTimeCreated() : Long = this.getLong(this.getColumnIndex(NotesDBContract.COLUMN_NAME_TIME_CREATED))
private fun Cursor.getTimeToDo() : Long = this.getLong(this.getColumnIndex(NotesDBContract.COLUMN_NAME_TIME_TO_DO))