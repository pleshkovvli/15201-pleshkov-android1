package ru.nsu.ccfit.pleshkov.notebook.view

import android.app.*
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.TimePicker
import kotlinx.android.synthetic.main.activity_note.*
import kotlinx.android.synthetic.main.content_note.*
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withTimeoutOrNull
import ru.nsu.ccfit.pleshkov.notebook.R
import ru.nsu.ccfit.pleshkov.notebook.model.NoteStatus
import ru.nsu.ccfit.pleshkov.notebook.model.SettingsApi
import ru.nsu.ccfit.pleshkov.notebook.presenter.getColor
import ru.nsu.ccfit.pleshkov.notebook.presenter.niceFormattedTime
import ru.nsu.ccfit.pleshkov.notebook.presenter.setNotification
import java.util.*

const val NOTIFICATION_ONCE = 1
const val NOTE_ID_KEY = "NOTE_ID"

abstract class NoteActivity :
        BaseDatabaseActivity(),
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    override val layoutId: Int
        get() = R.layout.activity_note

    lateinit var settingsApi: SettingsApi

    protected var id: Int = -1

    private var deadline: Long = -1L
    var status: NoteStatus = NoteStatus.UNKNOWN
    var changedByUser: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(newNoteToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        newNoteToolbar.setNavigationOnClickListener { onBackPressed() }

        settingsApi = SettingsApi(this)

        changeDeadline.setOnClickListener {
            DeadlineDatePickerDialog(this, this).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_note, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.note_settings -> {
            startActivityAnimated(SettingsActivity.newIntent(this))
            true
        }
        R.id.note_save -> {
            val title = noteTitle.text.toString()
            val text = noteText.text.toString()

            val dbJob = writeChangesToDatabase(title, text, deadline)

            val id = runBlocking { withTimeoutOrNull(500L) { dbJob.await() } }

            val nextStatusByTimeToDo = settingsApi.statusByTimeToDo(deadline, true)
            val time = deadline - settingsApi.timeToDoFromStatus(nextStatusByTimeToDo)
            if(!changedByUser && id != null
                    && (nextStatusByTimeToDo != NoteStatus.UNKNOWN)) {
                setNotification(id, time, this)
            }

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

    protected abstract fun writeChangesToDatabase(title: String, text: String, timeToDo: Long) : Deferred<Int>

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
        deadline = Calendar.getInstance().also { c ->
            c.set(year, month, day, 0, 0)
        }.timeInMillis
        DeadlineTimePickerDialog(this, this).show()
    }

    override fun onTimeSet(view: TimePicker, hour: Int, minute: Int) {
        deadline += (hour * 3600 + minute * 60) * 1000
        Log.d("NOTIF", "Seconds: ${deadline / 1000 % 60}")
        status = settingsApi.statusByTimeToDo(deadline, false)
        noteDeadline.text = "Deadline is ${niceFormattedTime(deadline)}"
        noteStatus.text = status.toString()
        noteStatus.setTextColor(view.getColor(status))
    }

    fun changeStatus(newStatus: NoteStatus) {
        status = newStatus
        noteStatus.text = status.toString()
        noteStatus.setTextColor(noteStatus.getColor(status))
        changedByUser = true
    }
}

class ChangeStatusDialog : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val noteActivity = activity as NoteActivity
        var position: Int = noteActivity.status.ordinal

        val builder = AlertDialog.Builder(activity)
                .setTitle(R.string.menu_change_status)
                .setPositiveButton(R.string.change) { dialog, id ->
                    noteActivity.changeStatus(NoteStatus.values()[position])
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .setSingleChoiceItems(
                        noteActivity.settingsApi.settingsNames(),
                        position
                ) { _, idSelected ->
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

class DeadlineTimePickerDialog(
        context: Context,
        listener: TimePickerDialog.OnTimeSetListener,
        time: Calendar = Calendar.getInstance().also { c -> c.add(Calendar.HOUR, 1) }
) : TimePickerDialog(
        context,
        listener,
        time.get(Calendar.HOUR_OF_DAY),
        time.get(Calendar.MINUTE),
        true
)