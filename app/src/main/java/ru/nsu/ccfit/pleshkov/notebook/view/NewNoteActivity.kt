package ru.nsu.ccfit.pleshkov.notebook.view

import android.content.Context
import android.content.Intent
import ru.nsu.ccfit.pleshkov.notebook.model.NotesDBContract

class NewNoteActivity : NoteActivity() {

    companion object {
        fun newIntent(context: Context) = Intent(context, NewNoteActivity::class.java)
    }

    override fun writeChangesToDatabase(header: String, date: Long, text: String) {
        val db = dbHelper.writableDatabase
        val values = contentValues(header, date, text)
        db.insert(NotesDBContract.TABLE_NAME, null, values)
    }
}
