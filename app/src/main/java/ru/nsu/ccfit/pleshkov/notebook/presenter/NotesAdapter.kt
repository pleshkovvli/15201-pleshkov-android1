package ru.nsu.ccfit.pleshkov.notebook.presenter

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.note_item.view.*
import ru.nsu.ccfit.pleshkov.notebook.R
import ru.nsu.ccfit.pleshkov.notebook.model.Note
import ru.nsu.ccfit.pleshkov.notebook.model.NoteStatus
import java.text.SimpleDateFormat

class NotesAdapter(
        private val listener: (Note) -> Unit = {}
) : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

    private val notes: ArrayList<Note> = ArrayList<Note>()
    private val notesSaved: ArrayList<Note> = ArrayList<Note>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = parent.inflate(R.layout.note_item)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int = notes.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(notes[position], listener)
    }

    fun setNotes(newNotes: Collection<Note>) {
        showNotes(newNotes)
        notesSaved.clear()
        notesSaved.addAll(newNotes)
    }

    fun showFiltered(query: String) {
        val filteredNotes = ArrayList<Note>()

        for (note in notesSaved) {
            val text = note.text
            val title = note.title
            if (text.contains(query, true) || title.contains(query, true)) {
                filteredNotes.add(note)
            }
        }

        showNotes(filteredNotes)
    }

    private fun showNotes(newNotes: Collection<Note>) {
        notes.clear()
        notes.addAll(newNotes)

        notes.sortWith(Comparator { a, b ->
            when {
                a.timeCreated < b.timeCreated -> -1
                a.timeCreated == b.timeCreated -> 0
                else -> 1
            }
        })

        notifyDataSetChanged()
    }

    fun moveElementToBottom(position: Int, change: Note.() -> Note) {
        val note = notes.removeAt(position)
        notesSaved.remove(note)
        notifyItemRemoved(position)

        val newNote = note.change()
        notesSaved.add(newNote)
        notes.add(newNote)
        notifyItemInserted(notes.size)
    }

    fun noteOnPosition(position: Int) = notes[position]

    fun removeItem(position: Int) {
        if (position < 0) {
            return  //TODO ?
        }
        val note = notes.removeAt(position)
        notesSaved.remove(note)
        notifyItemRemoved(position)
    }

    fun deleteAllNotes() {
        notes.clear()
        notesSaved.clear()
        notifyDataSetChanged()
    }

    private fun ViewGroup.inflate(layoutRes: Int): View =
            LayoutInflater.from(context).inflate(layoutRes, this, false)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(note: Note, listener: (Note) -> Unit) = with(itemView) {
            noteTitle.text = note.title
            noteText.text = note.text

            val creationDateText = niceFormattedTime(note.timeCreated)
            noteCreationDate.text = "$creationDateText"

            val timeToDo = note.timeToDo
            noteDeadline.text = if (timeToDo != -1L) {
                val deadlineDateText = niceFormattedTime(timeToDo)
                "Deadline is $deadlineDateText"
            } else "No deadline"

            noteStatus.text = note.status.toString()
            noteStatus.setTextColor(note.status.color)
            setOnClickListener { listener(note) }
        }
    }
}

fun niceFormattedTime(time: Long): CharSequence = DateUtils.formatSameDayTime(
        time,
        System.currentTimeMillis(),
        SimpleDateFormat.SHORT,
        SimpleDateFormat.SHORT
)

private const val SWIPE_DIRECTION = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
private const val MOVE_DIRECTION = 0

class NoteDeleteCallback(
        private val notesAdapter: NotesAdapter,
        private val dbPresenter: DatabasePresenter
) : ItemTouchHelper.SimpleCallback(MOVE_DIRECTION, SWIPE_DIRECTION) {
    override fun onMove(
            recyclerView: RecyclerView?,
            viewHolder: RecyclerView.ViewHolder?,
            target: RecyclerView.ViewHolder?
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
        val position = viewHolder?.adapterPosition ?: return
        val note = notesAdapter.noteOnPosition(position)
        if(note.status == NoteStatus.CANCELLED) {
            return
        }

        dbPresenter.changeToCancelled(note.id)

        notesAdapter.moveElementToBottom(position) { this.makeCancelledByUser() }
    }
}