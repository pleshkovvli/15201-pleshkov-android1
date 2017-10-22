package ru.nsu.ccfit.pleshkov.notebook

import android.database.sqlite.SQLiteDatabase
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.note_item.view.*
import ru.nsu.ccfit.pleshkov.notebook.model.Note
import ru.nsu.ccfit.pleshkov.notebook.model.NotesDBContract
import java.text.SimpleDateFormat

class NotesAdapter(val listener: (Note) -> Unit = {}) : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {
    private val notes: ArrayList<Note> = ArrayList<Note>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = parent.inflate(R.layout.note_item)
        return ViewHolder(itemView)
    }
    override fun getItemCount(): Int = notes.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(notes[position], listener)
    }

    fun setNotes(newNotes: Collection<Note>) {
        notes.clear()
        notes.addAll(newNotes)
        notes.sortWith(Comparator { a, b ->
            when {
                a.timeCreated < b.timeCreated -> -1
                a.timeCreated == b.timeCreated -> 0
                else -> 1
            }})
        notifyDataSetChanged()
    }

    fun noteCreatedTime(position: Int) = notes[position].timeCreated

    fun removeItem(position: Int) {
        if(position < 0) {
            return  //TODO ?
        }
        notes.removeAt(position)
        notifyItemRemoved(position)
    }

    fun deleteAllNotes() {
        notes.clear()
        notifyDataSetChanged()  //TODO ?
    }

    private fun ViewGroup.inflate(layoutRes: Int) : View =
         LayoutInflater.from(context).inflate(layoutRes, this, false)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(note: Note, listener: (Note) -> Unit) = with(itemView) {
            noteHeader.text = note.title
            noteDate.text = DateUtils.formatSameDayTime(
                    note.timeCreated,
                    System.currentTimeMillis(),
                    SimpleDateFormat.SHORT,
                    SimpleDateFormat.SHORT)
            noteText.text = note.text
            setOnClickListener { listener(note) }
        }
    }
}

private const val SWIPE_DIRECTION = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
private const val MOVE_DIRECTION = 0

class NoteDeleteCallback(
        private val notesAdapter: NotesAdapter,
        private val writableDb: SQLiteDatabase
) : ItemTouchHelper.SimpleCallback(MOVE_DIRECTION, SWIPE_DIRECTION) {
    override fun onMove(
            recyclerView: RecyclerView?,
            viewHolder: RecyclerView.ViewHolder?,
            target: RecyclerView.ViewHolder?
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
        val position = viewHolder?.adapterPosition ?: return
        val timestamp = notesAdapter.noteCreatedTime(position)
        val selection = "${NotesDBContract.COLUMN_NAME_TIME_CREATED} = ?"   //?
        writableDb.delete(NotesDBContract.TABLE_NAME, selection, arrayOf("$timestamp"))
        notesAdapter.removeItem(position)
    }
}