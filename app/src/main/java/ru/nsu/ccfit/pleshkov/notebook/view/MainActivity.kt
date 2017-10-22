package ru.nsu.ccfit.pleshkov.notebook.view

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.experimental.android.UI
import ru.nsu.ccfit.pleshkov.notebook.presenter.NoteDeleteCallback
import ru.nsu.ccfit.pleshkov.notebook.presenter.NotesAdapter
import ru.nsu.ccfit.pleshkov.notebook.R
import ru.nsu.ccfit.pleshkov.notebook.model.*

class MainActivity : BaseDatabaseActivity(), AllNotesDeleter, SearchView.OnQueryTextListener {

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, MainActivity::class.java)
    }

    override val layoutId: Int
        get() = R.layout.activity_main

    private lateinit var notesAdapter: NotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(toolbar)
        addNoteFab.setOnClickListener { startActivityAnimated(NewNoteActivity.newIntent(this)) }

        initRecyclerView()

        jobAsync(UI) {
            initDatabaseTask.await()
            val getNotesTask = dbPresenter.getAllNotes()

            val itemTouchHelper = ItemTouchHelper(NoteDeleteCallback(notesAdapter, dbPresenter))
            itemTouchHelper.attachToRecyclerView(recyclerView)

            val notes = getNotesTask.await()
            notesAdapter.setNotes(notes)
        }
    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        notesAdapter = NotesAdapter { note ->
            val intent = EditNoteActivity.newIntent(this, note.id)
            startActivityAnimated(intent)
        }
        recyclerView.adapter = notesAdapter

        val divider = DividerItemDecoration(recyclerView.context, layoutManager.orientation)
        recyclerView.addItemDecoration(divider)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        if(searchItem != null) {
            val searchView = searchItem.actionView as SearchView
            searchView.setOnQueryTextListener(this)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                R.id.action_search -> true
                R.id.action_settings -> true
                R.id.delete_all_notes -> {
                    DeleteAllNotesDialog().show(supportFragmentManager, DELETE_DIALOG_TAG)
                    false
                }
                else -> super.onOptionsItemSelected(item)
            }

    override fun onQueryTextSubmit(query: String?) = false

    override fun onQueryTextChange(query: String?) = if(query == null) false else {
        notesAdapter.showFiltered(query)
        true
    }

    override fun deleteAllNotes() {
        dbPresenter.deleteAllNotes()
        notesAdapter.deleteAllNotes()
    }
}

const val DELETE_DIALOG_TAG = "delete all"

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
