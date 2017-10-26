package ru.nsu.ccfit.pleshkov.notebook.view

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
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
import ru.nsu.ccfit.pleshkov.notebook.presenter.niceFormattedTime
import ru.nsu.ccfit.pleshkov.notebook.services.DeadlineReceiver
import java.util.*

const val NOTIFICATION_ONCE = 1
const val NOTE_ID_KEY = "NOTE_ID"
const val NEXT_STATUS_KEY = "NEXT_STATUS"

abstract class NoteActivity :
        BaseDatabaseActivity(),
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    override val layoutId: Int
        get() = R.layout.activity_note

    protected var id: Int = -1
    private var deadline: Long = -1L
    var status: NoteStatus = NoteStatus.UNKNOWN
    var changedByUser: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(newNoteToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        newNoteToolbar.setNavigationOnClickListener { onBackPressed() }

        changeDeadline.setOnClickListener { DeadlineDatePickerDialog(this, this).show() }
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

            val dbJob = writeChangesToDatabase(title, text, deadline)

            val id = runBlocking { withTimeoutOrNull(500L) { dbJob.await() } }

            if(!changedByUser && id != null && (status.timeToNext != 0L)) {
                setNotification(id)
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

    private fun setNotification(id: Int) {
        val notifyIntent = DeadlineReceiver.newIntent(this)
        notifyIntent.putExtra(NOTE_ID_KEY, id)
        notifyIntent.putExtra(NEXT_STATUS_KEY, status.nextCode)
        val pending = pendingNotifyIntent(notifyIntent)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + status.timeToNext, pending)
    }

    private fun pendingNotifyIntent(notifyIntent: Intent) =
            PendingIntent.getBroadcast(this, NOTIFICATION_ONCE, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    protected abstract fun writeChangesToDatabase(title: String, text: String, timeToDo: Long) : Deferred<Int>

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
        deadline = Calendar.getInstance().also { c ->
            c.set(year, month, day, 0, 0)
        }.timeInMillis
        DeadlineTimePickerDialog(this, this).show()
    }

    override fun onTimeSet(view: TimePicker?, hour: Int, minute: Int) {
        deadline += (hour * 3600 + minute * 60) * 1000
        status = NoteStatus.getByTimeToDo(deadline - System.currentTimeMillis())
        noteDeadline.text = "Deadline is ${niceFormattedTime(deadline)}"
        noteStatus.text = status.toString()
        noteStatus.setTextColor(status.color)
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
                    noteActivity.noteStatus.setTextColor(noteActivity.status.color)
                    noteActivity.changedByUser = true
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
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