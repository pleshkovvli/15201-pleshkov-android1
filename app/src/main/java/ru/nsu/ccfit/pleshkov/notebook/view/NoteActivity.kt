package ru.nsu.ccfit.pleshkov.notebook.view

import android.content.ContentValues
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_note.*
import ru.nsu.ccfit.pleshkov.notebook.R
import ru.nsu.ccfit.pleshkov.notebook.model.NotesDBContract
import java.util.*

abstract class NoteActivity : BaseDatabaseActivity() {

    override val LAYOUT_ID: Int
        get() = R.layout.activity_note

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(newNoteToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        newNoteToolbar.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_note, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.new_note_settings -> return true
            R.id.new_note_save -> {
                val header = noteHeader.text.toString()
                val text = noteText.text.toString()
                val date: Long = Calendar.getInstance().timeInMillis

                writeChangesToDatabase(header, date, text)

                val intent = MainActivity.newIntent(this)
                startActivityAnimated(intent)
                return false
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    protected abstract fun writeChangesToDatabase(header: String, date: Long, text: String)

    protected fun contentValues(header: String, date: Long, text: String): ContentValues {
        val values = ContentValues()
        values.put(NotesDBContract.COLUMN_NAME_TITLE, header)
        values.put(NotesDBContract.COLUMN_NAME_TIME_CREATED, date)
        values.put(NotesDBContract.COLUMN_NAME_TEXT, text)
        return values
    }
}
