package ru.nsu.ccfit.pleshkov.notebook.view

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import android.view.Menu
import android.view.MenuItem
import android.widget.DatePicker
import kotlinx.android.synthetic.main.activity_note.*
import kotlinx.android.synthetic.main.content_note.*
import ru.nsu.ccfit.pleshkov.notebook.R
import ru.nsu.ccfit.pleshkov.notebook.model.NoteStatus
import ru.nsu.ccfit.pleshkov.notebook.presenter.niceFormattedTime
import java.util.*

abstract class NoteActivity : BaseDatabaseActivity(), DatePickerDialog.OnDateSetListener {

    override val layoutId: Int
        get() = R.layout.activity_note

    private var timeToDo: Long = -1L
    var status: NoteStatus = NoteStatus.UNKNOWN
    var changedByUser: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(newNoteToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        newNoteToolbar.setNavigationOnClickListener { onBackPressed() }

        changeDeadline.setOnClickListener {
            val deadlineDatePickerDialog = DeadlineDatePickerDialog(this, this)
            deadlineDatePickerDialog.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_note, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.note_settings -> false
        R.id.note_save -> {
            val title = noteTitle.text.toString()
            val text = noteText.text.toString()

            writeChangesToDatabase(title, text, timeToDo)

            val intent = MainActivity.newIntent(this)
            startActivityAnimated(intent)
            false
        }
        R.id.note_status -> {
            ChangeStatusDialog().show(supportFragmentManager, "changeStatusDialog")
            false
        }
        else -> super.onOptionsItemSelected(item)
    }

    protected abstract fun writeChangesToDatabase(title: String, text: String, timeToDo: Long)

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
        timeToDo = Calendar.getInstance().also { c -> c.set(year, month, day) }.timeInMillis
        noteDeadline.text = "Deadline is ${niceFormattedTime(timeToDo)}"
    }
}

class ChangeStatusDialog : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val noteActivity = activity as NoteActivity
        var position: Int = noteActivity.status.position

        val builder = AlertDialog.Builder(activity)
                .setTitle(R.string.menu_change_status)
                .setPositiveButton(R.string.change) { dialog, id ->
                    noteActivity.status = NoteStatus.getByPosition(position)
                    noteActivity.noteStatus.text = noteActivity.status.toString()
                    noteActivity.changedByUser = true
                }
                .setNegativeButton(R.string.cancel) { _, _ ->}
                .setSingleChoiceItems(R.array.status_names, position) { _, idSelected ->
                    position = idSelected
                }

        return builder.create()
    }

}

class DeadlineDatePickerDialog(
        context: Context,
        listener: DatePickerDialog.OnDateSetListener,
        date: Calendar = Calendar.getInstance().also { c -> c.add(Calendar.DATE, 1) }
) : DatePickerDialog(
        context,
        listener,
        date.get(Calendar.YEAR),
        date.get(Calendar.MONTH),
        date.get(Calendar.DATE)
)

