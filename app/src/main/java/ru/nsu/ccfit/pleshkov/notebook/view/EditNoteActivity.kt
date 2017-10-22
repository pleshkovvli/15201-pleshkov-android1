package ru.nsu.ccfit.pleshkov.notebook.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_note.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import ru.nsu.ccfit.pleshkov.notebook.model.*

class EditNoteActivity : NoteActivity() {
    private var oldDate: Long = -1L

    companion object {
        private const val KEY_TIMESTAMP = "TIMESTAMP"

        fun newIntent(context: Context, timestamp: Long) : Intent {
            val intent = Intent(context, EditNoteActivity::class.java)
            intent.putExtra(KEY_TIMESTAMP, timestamp)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        async(UI) {
            val note = getNote().await()
            noteHeader.setText(note.title)
            noteText.setText(note.text)
        }

    }

    private fun getNote() = async {
        val db = dbHelper.readableDatabase

        val projection = arrayOf(
                NotesDBContract.COLUMN_NAME_TITLE,
                NotesDBContract.COLUMN_NAME_TIME_CREATED,
                NotesDBContract.COLUMN_NAME_TEXT,
                NotesDBContract.COLUMN_NAME_TIME_TO_DO
        )
        oldDate = intent.getLongExtra(KEY_TIMESTAMP, -1L)
        val selection = "${NotesDBContract.COLUMN_NAME_TIME_CREATED} = ?"
        val cursor = db.query(
                NotesDBContract.TABLE_NAME,
                projection,
                selection,
                arrayOf("$oldDate"),
                null,
                null,
                null
        )

        cursor.use {
            it.moveToNext()
            it.getNote()
        }
    }

    override fun writeChangesToDatabase(header: String, date: Long, text: String) {
        val db = dbHelper.writableDatabase
        val values = contentValues(header, date, text)
        val selection = "${NotesDBContract.COLUMN_NAME_TIME_CREATED} = ?"   //?
        db.update(NotesDBContract.TABLE_NAME, values, selection, arrayOf("$oldDate"))
    }
}
