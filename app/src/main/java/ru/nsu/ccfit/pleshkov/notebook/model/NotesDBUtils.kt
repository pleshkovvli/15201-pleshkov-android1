package ru.nsu.ccfit.pleshkov.notebook.model

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.util.*

private const val INT_TRUE = 1
private const val INT_FALSE = 0

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

suspend fun SQLiteDatabase.insertNote(
        title: String? = null,
        text: String? = null,
        timeToDo: Long? = null,
        status: NoteStatus? = null,
        changedByUser: Boolean? = null
): Long {
    val currentMoment = Calendar.getInstance().timeInMillis
    val values = contentValues(
            title,
            text,
            currentMoment,
            timeToDo,
            status,
            changedByUser
    )

    return this.insert(NotesDBContract.TABLE_NAME, null, values)
}

suspend fun SQLiteDatabase.editNote(
        id: Int,
        title: String? = null,
        text: String? = null,
        timeToDo: Long? = null,
        status: NoteStatus? = null,
        changedByUser: Boolean? = null
): Int {
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
    return id
}

suspend fun SQLiteDatabase.changeStatus(noteId: Int, status: NoteStatus) {
    val selection = "${NotesDBContract._ID} = ?"

    val values = ContentValues()
    values.put(NotesDBContract.COLUMN_NAME_STATUS, status.ordinal)
    values.put(NotesDBContract.COLUMN_NAME_SET_BY_USER, INT_TRUE)

    this.update(NotesDBContract.TABLE_NAME, values, selection, arrayOf("$noteId"))
}

suspend fun SQLiteDatabase.selectAllNotes(): ArrayList<Note> {
    val notesList = ArrayList<Note>()

    val cursor = this.rawQuery(NotesDBContract.SELECT_ALL, null)
    cursor.use {
        while (cursor.moveToNext()) {
            notesList.add(cursor.getNote())
        }
    }

    return notesList
}

suspend fun SQLiteDatabase.selectRow(rowId: Int) =
        selectElement(IdType.ROWID, arrayOf("$rowId"))

suspend fun SQLiteDatabase.selectNote(noteId: Int) =
        selectElement(IdType.BASE_ID, arrayOf("$noteId"))

suspend fun SQLiteDatabase.deleteAll() {
    this.execSQL(NotesDBContract.DELETE_ALL)
}

private enum class IdType(val selection: String) {
    ROWID("ROWID = ?"),
    BASE_ID("${NotesDBContract._ID} = ?")
}

private suspend fun SQLiteDatabase.selectElement(
        type: IdType,
        values: Array<String>
): Note {
    val cursor = this.query(
            NotesDBContract.TABLE_NAME,
            NotesDBContract.PROJECTION_ALL,
            type.selection,
            values,
            null,
            null,
            null
    )

    return cursor.use {
        it.moveToNext()
        it.getNote()
    }
}

private fun contentValues(
        title: String?,
        text: String?,
        timeCreated: Long?,
        timeToDo: Long?,
        status: NoteStatus?,
        changedByUser: Boolean?
): ContentValues {
    val values = ContentValues()
    if (title != null) {
        values.put(NotesDBContract.COLUMN_NAME_TITLE, title)
    }
    if (text != null) {
        values.put(NotesDBContract.COLUMN_NAME_TEXT, text)
    }
    if (timeCreated != null) {
        values.put(NotesDBContract.COLUMN_NAME_TIME_CREATED, timeCreated)
    }
    if (timeToDo != null) {
        values.put(NotesDBContract.COLUMN_NAME_TIME_TO_DO, timeToDo)
    }
    if (status != null) {
        values.put(NotesDBContract.COLUMN_NAME_STATUS, status.ordinal)
    }
    if (changedByUser != null) {
        values.put(
                NotesDBContract.COLUMN_NAME_SET_BY_USER,
                if (changedByUser) INT_TRUE else INT_FALSE
        )
    }

    values.put(
            NotesDBContract.COLUMN_NAME_TIME_UPDATED,
            System.currentTimeMillis()
    )

    return values
}

private fun Cursor.getId(): Int {
    val idColumnIndex = getColumnIndex(NotesDBContract._ID)
    return getInt(idColumnIndex)
}

private fun Cursor.getTitle(): String {
    val titleColumnIndex = getColumnIndex(NotesDBContract.COLUMN_NAME_TITLE)
    return getString(titleColumnIndex) ?: ""
}

private fun Cursor.getText(): String {
    val textColumnIndex = getColumnIndex(NotesDBContract.COLUMN_NAME_TEXT)
    return getString(textColumnIndex) ?: ""
}

private fun Cursor.getTimeCreated(): Long {
    val createdColumnIndex = getColumnIndex(NotesDBContract.COLUMN_NAME_TIME_CREATED)
    return getLong(createdColumnIndex)
}

private fun Cursor.getTimeUpdated(): Long {
    val updatedColumnIndex = getColumnIndex(NotesDBContract.COLUMN_NAME_TIME_UPDATED)
    return getLong(updatedColumnIndex)
}

private fun Cursor.getTimeToDo(): Long {
    val todoColumnIndex = getColumnIndex(NotesDBContract.COLUMN_NAME_TIME_TO_DO)
    return getLong(todoColumnIndex)
}

private fun Cursor.getStatus(): NoteStatus {
    val statusColumnIndex = getColumnIndex(NotesDBContract.COLUMN_NAME_STATUS)
    return NoteStatus.values()[getInt(statusColumnIndex)]
}

private fun Cursor.getStatusSetByUser(): Boolean {
    val setByUserColumnIndex = getColumnIndex(NotesDBContract.COLUMN_NAME_SET_BY_USER)
    return (getInt(setByUserColumnIndex) > 0)
}
