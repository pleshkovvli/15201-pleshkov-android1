package ru.nsu.ccfit.pleshkov.notebook.model

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import ru.nsu.ccfit.pleshkov.notebook.view.EditNoteActivity
import java.util.*

fun Cursor.getNote() = Note(
        getId(),
        getTitle(),
        getText(),
        getTimeCreated(),
        getTimeUpdated(),
        getTimeToDo(),
        getStatus(),
        getStatusSetByUser()
)

suspend fun SQLiteDatabase.getAllNotes(): ArrayList<Note> {
    val notesList = ArrayList<Note>()

    val cursor = this.rawQuery(NotesDBContract.SELECT_ALL, null)
    cursor.use {
        while (cursor.moveToNext()) {
            notesList.add(cursor.getNote())
        }
    }

    return notesList
}

suspend fun SQLiteDatabase.changeToCancelled(noteId: Int) {
    val selection = "${NotesDBContract._ID} = ?"
    val values = ContentValues()
    values.put(NotesDBContract.COLUMN_NAME_STATUS, NoteStatus.CANCELLED.code)
    values.put(NotesDBContract.COLUMN_NAME_SET_BY_USER, 1)

    this.update(NotesDBContract.TABLE_NAME, values, selection, arrayOf("$noteId"))
}

suspend fun SQLiteDatabase.deleteAll() {
    this.execSQL(NotesDBContract.DELETE_ALL)
}

suspend fun SQLiteDatabase.editNote(
        id: Int,
        title: String? = null,
        text: String? = null,
        timeToDo: Long? = null,
        status: NoteStatus? = null,
        changedByUser: Boolean? = null) {
    val values = contentValues(
            title,
            text,
            null,
            timeToDo,
            status,
            changedByUser
    )
    val selection = "${NotesDBContract._ID} = ?"
    this.update(NotesDBContract.TABLE_NAME, values, selection, arrayOf("$id"))
}

suspend fun SQLiteDatabase.getNote(noteId: Int) : Note {
    val projection = arrayOf(
            NotesDBContract._ID,
            NotesDBContract.COLUMN_NAME_TITLE,
            NotesDBContract.COLUMN_NAME_TEXT,
            NotesDBContract.COLUMN_NAME_TIME_CREATED,
            NotesDBContract.COLUMN_NAME_TIME_UPDATED,
            NotesDBContract.COLUMN_NAME_TIME_TO_DO,
            NotesDBContract.COLUMN_NAME_STATUS,
            NotesDBContract.COLUMN_NAME_SET_BY_USER
    )
    val selection = "${NotesDBContract._ID} = ?"
    val cursor = this.query(
            NotesDBContract.TABLE_NAME,
            projection,
            selection,
            arrayOf("$noteId"),
            null,
            null,
            null
    )

    return cursor.use {
        it.moveToNext()
        it.getNote()
    }
}

suspend fun SQLiteDatabase.insertNote(
        title: String? = null,
        text: String? = null,
        timeToDo: Long? = null,
        status: NoteStatus? = null,
        changedByUser: Boolean? = null
) {
    val currentMoment = Calendar.getInstance().timeInMillis
    val values = contentValues(
            title,
            text,
            currentMoment,
            timeToDo,
            status,
            changedByUser
    )

    this.insert(NotesDBContract.TABLE_NAME, null, values)
}

fun contentValues(
        title: String? = null,
        text: String? = null,
        timeCreated: Long? = null,
        timeToDo: Long? = null,
        status: NoteStatus? = null,
        changedByUser: Boolean? = null
): ContentValues {
    val values = ContentValues()
    if(title != null) values.put(NotesDBContract.COLUMN_NAME_TITLE, title)
    if(text != null) values.put(NotesDBContract.COLUMN_NAME_TEXT, text)
    if(timeCreated != null)values.put(NotesDBContract.COLUMN_NAME_TIME_CREATED, timeCreated)
    if(timeToDo != null) values.put(NotesDBContract.COLUMN_NAME_TIME_TO_DO, timeToDo)
    values.put(NotesDBContract.COLUMN_NAME_TIME_UPDATED, Calendar.getInstance().timeInMillis)
    if(status != null) values.put(NotesDBContract.COLUMN_NAME_STATUS, status.code)
    if(changedByUser != null) values.put(NotesDBContract.COLUMN_NAME_SET_BY_USER, if (changedByUser) 1 else 0)
    return values
}

private fun Cursor.getId() = getInt(getColumnIndex(NotesDBContract._ID))
private fun Cursor.getTitle() = getString(getColumnIndex(NotesDBContract.COLUMN_NAME_TITLE))
private fun Cursor.getText() = getString(getColumnIndex(NotesDBContract.COLUMN_NAME_TEXT))
private fun Cursor.getTimeCreated() = getLong(getColumnIndex(NotesDBContract.COLUMN_NAME_TIME_CREATED))
private fun Cursor.getTimeUpdated() = getLong(getColumnIndex(NotesDBContract.COLUMN_NAME_TIME_UPDATED))
private fun Cursor.getTimeToDo() = getLong(getColumnIndex(NotesDBContract.COLUMN_NAME_TIME_TO_DO))
private fun Cursor.getStatus() = NoteStatus.getByCode(getInt(getColumnIndex(NotesDBContract.COLUMN_NAME_STATUS)))
private fun Cursor.getStatusSetByUser() = (getInt(getColumnIndex(NotesDBContract.COLUMN_NAME_SET_BY_USER)) > 0)
