package ru.nsu.ccfit.pleshkov.notebook.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.content_note.*
import ru.nsu.ccfit.pleshkov.notebook.R
import ru.nsu.ccfit.pleshkov.notebook.model.NoteStatus
import ru.nsu.ccfit.pleshkov.notebook.presenter.getColor

class NewNoteActivity : NoteActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        noteStatus.text = NoteStatus.UNKNOWN.toString()
        noteStatus.setTextColor(noteStatus.getColor(NoteStatus.UNKNOWN))
        noteDeadline.text = resources.getString(R.string.no_deadline)
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, NewNoteActivity::class.java)
    }

    override fun writeChangesToDatabase(title: String, text: String, timeToDo: Long) = jobAsync {
        dbPresenter.addNote(title, text, timeToDo, status, changedByUser).await()
    }
}
