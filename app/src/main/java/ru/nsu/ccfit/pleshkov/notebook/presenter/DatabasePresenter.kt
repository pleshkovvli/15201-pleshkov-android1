package ru.nsu.ccfit.pleshkov.notebook.presenter

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.experimental.Job
import ru.nsu.ccfit.pleshkov.notebook.model.*
import ru.nsu.ccfit.pleshkov.notebook.view.JobHolder

class DatabasePresenter : JobHolder {
    override val job = Job()

    private lateinit var dbHelper: NotesDBHelper
    private lateinit var writableDb: SQLiteDatabase
    private lateinit var readableDb: SQLiteDatabase

    fun begin(context: Context) {
        dbHelper = NotesDBHelper(context)
    }

    fun initDatabases() = jobAsync {
        writableDb = dbHelper.writableDatabase
        readableDb = dbHelper.readableDatabase
    }

    fun addNote(
            title: String? = null,
            text: String? = null,
            timeToDo: Long? = null,
            status: NoteStatus? = null,
            changedByUser: Boolean? = null
    ) = jobAsync {
        val rowId = writableDb.insertNote(title, text, timeToDo, status, changedByUser)
        readableDb.selectRow(rowId.toInt()).id
    }

    fun editNote(
            id: Int,
            title: String,
            text: String,
            timeToDo: Long,
            status: NoteStatus,
            changedByUser: Boolean
    ) = jobAsync {
        writableDb.editNote(id, title, text, timeToDo, status, changedByUser)
    }

    fun changeStatus(noteId: Int, status: NoteStatus) = jobAsync {
        writableDb.changeStatus(noteId, status)
    }

    fun getAllNotes() = jobAsync {
        readableDb.selectAllNotes()
    }

    fun getNote(noteId: Int) = jobAsync {
        readableDb.selectNote(noteId)
    }

    fun deleteAllNotes() = jobAsync {
        writableDb.deleteAll()
    }

    fun end() {
        job.cancel()
        dbHelper.close()
    }
}
