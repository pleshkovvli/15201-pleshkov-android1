package ru.nsu.ccfit.pleshkov.notebook.view

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.experimental.android.UI
import ru.nsu.ccfit.pleshkov.notebook.NoteDeleteCallback
import ru.nsu.ccfit.pleshkov.notebook.NotesAdapter
import ru.nsu.ccfit.pleshkov.notebook.R
import ru.nsu.ccfit.pleshkov.notebook.model.*

class MainActivity : BaseDatabaseActivity(), AllNotesDeleter {

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, MainActivity::class.java)
    }

    override val LAYOUT_ID: Int
        get() = R.layout.activity_main

    private lateinit var notesAdapter: NotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(toolbar)
        fab.setOnClickListener { startActivityAnimated(NewNoteActivity.newIntent(this)) }

        initRecyclerView()

        jobAsync(UI) {
            initDatabaseTask.await()
            val getNotesTask = jobAsync { readableDb.getAllNotes() }

            val itemTouchHelper = ItemTouchHelper(NoteDeleteCallback(notesAdapter, writableDb))
            itemTouchHelper.attachToRecyclerView(recyclerView)

            val notes = getNotesTask.await()
            notesAdapter.setNotes(notes)
        }
    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        notesAdapter = NotesAdapter { note ->
            val intent = EditNoteActivity.newIntent(this, note.timeCreated)
            startActivityAnimated(intent)
        }
        recyclerView.adapter = notesAdapter

        val divider = DividerItemDecoration(recyclerView.context, layoutManager.orientation)
        recyclerView.addItemDecoration(divider)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                R.id.action_settings -> true
                R.id.delete_all_notes -> {
                    DeleteAllNotesDialog().show(supportFragmentManager, "delete all")
                    false
                }
                else -> super.onOptionsItemSelected(item)
            }

    override fun deleteAllNotes() {
        writableDb.execSQL(NotesDBContract.DELETE_ALL)
        notesAdapter.deleteAllNotes()
    }
}

class DeleteAllNotesDialog : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
                .setTitle(R.string.delete_all_title)
                .setMessage(R.string.delete_all_message)
                .setPositiveButton(R.string.delete_all_ok) { dialog, id ->
                    (activity as AllNotesDeleter).deleteAllNotes()
                }
                .setNegativeButton(R.string.delete_all_cancel) { _, _ ->}
        return builder.create()
    }
}
