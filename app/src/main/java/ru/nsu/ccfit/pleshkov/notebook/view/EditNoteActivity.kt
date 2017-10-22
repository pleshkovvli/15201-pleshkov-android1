package ru.nsu.ccfit.pleshkov.notebook.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.content_note.*
import kotlinx.coroutines.experimental.android.UI
import ru.nsu.ccfit.pleshkov.notebook.model.Note
import ru.nsu.ccfit.pleshkov.notebook.presenter.niceFormattedTime

class EditNoteActivity : NoteActivity() {
    private lateinit var oldNote: Note

    companion object {
        private const val KEY_ID = "NOTE_ID"

        fun newIntent(context: Context, id: Int): Intent {
            val intent = Intent(context, EditNoteActivity::class.java)
            intent.putExtra(KEY_ID, id)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        jobAsync(UI) {
            val noteId = intent.getIntExtra(KEY_ID, -1)
            oldNote = dbPresenter.getNote(noteId).await()

            noteTitle.setText(oldNote.title)
            noteText.setText(oldNote.text)

            val timeToDo = oldNote.timeToDo
            noteDeadline.text = if (timeToDo != -1L)
                "Deadline is ${niceFormattedTime(timeToDo)}" else "No deadline"

            status = oldNote.status
            noteStatus.text = status.toString()
            noteStatus.setTextColor(status.color)
            changedByUser = oldNote.statusSetByUser
        }

    }

    override fun writeChangesToDatabase(title: String, text: String, timeToDo: Long) {
        dbPresenter.editNote(oldNote.id, title, text, timeToDo, status, changedByUser)
    }
}
