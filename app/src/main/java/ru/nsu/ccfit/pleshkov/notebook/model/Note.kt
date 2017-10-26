package ru.nsu.ccfit.pleshkov.notebook.model

import android.graphics.Color

data class Note(
        val id: Int,
        val title: String,
        val text: String,
        val timeCreated: Long,
        val timeUpdated: Long,
        val timeToDo: Long,
        val status: NoteStatus = NoteStatus.UNKNOWN,
        val statusSetByUser: Boolean = false
) {
    fun makeCancelledByUser() = Note(
            this.id,
            this.title,
            this.text,
            this.timeCreated,
            this.timeUpdated,
            this.timeToDo,
            NoteStatus.CANCELLED,
            true
    )
}

const val URGENT_TIME_MS: Long = 20000L//24 * 60 * 60 * 1000
const val PRIMARY_TIME_MS: Long = 60000L//48L * 60 * 60 * 1000
const val USUAL_TIME_MS: Long = 80000L//100L * 60 * 60 * 1000
const val SOMEDAY_TIME_MS: Long = 121000L//30L * 24 * 60 * 60 * 1000

enum class NoteStatus(
        val code: Int,
        val position: Int,
        val color: Int,
        val nextCode: Int,
        val timeToNext: Long) {

    URGENT(10, 0, Color.argb(255, 220, 30, 30), 31, URGENT_TIME_MS),
    PRIMARY(11, 1, Color.argb(255, 180, 30, 30), 10, PRIMARY_TIME_MS - URGENT_TIME_MS),
    USUAL(12, 2, Color.argb(255, 200, 200, 40), 11, USUAL_TIME_MS - PRIMARY_TIME_MS),
    SOMEDAY(13, 3, Color.argb(255, 130, 130, 230), 12, SOMEDAY_TIME_MS - PRIMARY_TIME_MS),
    UNNECESSARY(14, 4, Color.argb(255, 40, 30, 240), 13, 0),
    DONE(20, 5, Color.argb(255, 10, 250, 40), 40, 0),
    FAILED(31, 6, Color.argb(255, 250, 10, 10), 40, 0),
    CANCELLED(32, 7, Color.argb(200, 100, 120, 130), 40, 0),
    UNKNOWN(40, 8, Color.argb(150, 40, 40, 0), 40, 0);

    companion object {
        fun getByCode(code: Int) = when (code) {
            10 -> URGENT
            11 -> PRIMARY
            12 -> USUAL
            13 -> SOMEDAY
            14 -> UNNECESSARY
            20 -> DONE
            31 -> FAILED
            32 -> CANCELLED
            else -> UNKNOWN
        }

        fun getByPosition(position: Int) = when (position) {
            0 -> URGENT
            1 -> PRIMARY
            2 -> USUAL
            3 -> SOMEDAY
            4 -> UNNECESSARY
            5 -> DONE
            6 -> FAILED
            7 -> CANCELLED
            else -> UNKNOWN
        }

        fun getByTimeToDo(time: Long) = when {
            time < 0 -> UNKNOWN
            time < URGENT_TIME_MS -> URGENT
            time < PRIMARY_TIME_MS -> PRIMARY
            time < USUAL_TIME_MS -> USUAL
            time < SOMEDAY_TIME_MS -> SOMEDAY
            else -> UNNECESSARY
        }


    }
}