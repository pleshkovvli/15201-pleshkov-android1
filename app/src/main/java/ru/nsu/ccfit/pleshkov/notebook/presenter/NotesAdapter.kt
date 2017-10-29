package ru.nsu.ccfit.pleshkov.notebook.presenter

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.note_item.view.*
import ru.nsu.ccfit.pleshkov.notebook.R
import ru.nsu.ccfit.pleshkov.notebook.model.Note
import ru.nsu.ccfit.pleshkov.notebook.model.NoteStatus
import ru.nsu.ccfit.pleshkov.notebook.model.copyAsCancelledByUser
import java.text.SimpleDateFormat

class NotesAdapter(
        private val checkDone: (Note) -> Unit,
        private val checkUndone: (Note) -> NoteStatus,
        private val onClickListener: (Note) -> Unit
) : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

    private val notes: ArrayList<Note> = ArrayList()
    private val notesSaved: ArrayList<Note> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = parent.inflate(R.layout.note_item)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int = notes.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(notes[position], checkDone, checkUndone, onClickListener)
    }

    fun setNotes(newNotes: Collection<Note>) {
        showNotes(newNotes)
        notesSaved.clear()
        notesSaved.addAll(newNotes)
    }

    fun renewItem(id: Int, newNote: Note) {
        Log.d("RENEW", "Changed!")
        for(j in notesSaved.indices) {
            if(notesSaved[j].id == id) {
                notesSaved[j] = newNote
                for(i in notes.indices) {
                    if(notes[i].id == id) {
                        notes[i] = newNote
                        Log.d("RENEW", "Changed!")
                        notifyItemChanged(i)
                    }
                }
            }
        }
    }

    fun showFiltered(query: String) {
        val filteredNotes = ArrayList<Note>()

        for (note in notesSaved) {
            val text = note.text
            val title = note.title
            if (text.contains(query, true)
                    || title.contains(query, true)) {
                filteredNotes.add(note)
            }
        }

        showNotes(filteredNotes)
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
            return
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

    private fun showNotes(newNotes: Collection<Note>) {
        notes.clear()
        notes.addAll(newNotes)
        //TODO: Comparators Factory
        notes.sortWith(Comparator { a, b ->
            when {
                a.timeCreated < b.timeCreated -> -1
                a.timeCreated == b.timeCreated -> 0
                else -> 1
            }
        })

        notifyDataSetChanged()
    }

    private fun ViewGroup.inflate(layoutRes: Int): View =
            LayoutInflater
                    .from(context)
                    .inflate(layoutRes, this, false)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(
                note: Note,
                checkDone: (Note) -> Unit,
                checkUndone: (Note) -> NoteStatus,
                onClickListener: (Note) -> Unit
        ) = with(itemView) {
            noteTitle.text = note.title
            noteText.text = note.text

            if(note.status == NoteStatus.DONE) {
                isDone.isChecked = true
            }

            val creationDateText = niceFormattedTime(note.timeCreated)
            noteCreationDate.text = "$creationDateText"

            val timeToDo = note.timeToDo
            noteDeadline.text = if (timeToDo != -1L) {
                val deadlineDateText = niceFormattedTime(timeToDo)
                "Deadline is $deadlineDateText"
            } else "No deadline"

            isDone.setOnCheckedChangeListener { _, checked ->
                    if(checked) {
                        checkDone(note)
                        val done = NoteStatus.DONE
                        noteStatus.text = done.toString()
                        noteStatus.setTextColor(noteStatus.getColor(done))
                    } else {
                        val status = checkUndone(note)
                        noteStatus.text = status.toString()
                        noteStatus.setTextColor(noteStatus.getColor(status))
                    }
            }

            val status = note.status
            noteStatus.text = status.toString()
            noteStatus.setTextColor(getColor(status))
            setOnClickListener { onClickListener(note) }
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
    ) = false

    override fun getSwipeDirs(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder): Int {
        val note = notesAdapter.noteOnPosition(viewHolder.adapterPosition)
        if(note.status == NoteStatus.CANCELLED) {
            return 0
        }
        return super.getSwipeDirs(recyclerView, viewHolder)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
        val position = viewHolder?.adapterPosition ?: return
        val note = notesAdapter.noteOnPosition(position)
        if (note.status == NoteStatus.CANCELLED) {
            return
        }

        dbPresenter.changeStatus(note.id, NoteStatus.CANCELLED)
        notesAdapter.moveElementToBottom(position) { this.copyAsCancelledByUser() }
    }
}