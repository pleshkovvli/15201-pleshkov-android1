package ru.nsu.ccfit.pleshkov.notebook.model

data class Note(
        val id: Int,
        val title: String,
        val text: String,
        val timeCreated: Long,
        val timeUpdated: Long,
        val timeToDo: Long,
        val status: NoteStatus = NoteStatus.UNKNOWN,
        val statusSetByUser: Boolean = false
)

fun Note.copyAsCancelledByUser() = Note(
        this.id,
        this.title,
        this.text,
        this.timeCreated,
        System.currentTimeMillis(),
        this.timeToDo,
        NoteStatus.CANCELLED,
        true
)

enum class NoteStatus {
    URGENT,
    PRIMARY,
    USUAL,
    SOMEDAY,
    UNNECESSARY,
    DONE,
    FAILED,
    CANCELLED,
    UNKNOWN;
}