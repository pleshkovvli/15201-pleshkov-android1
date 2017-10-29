package ru.nsu.ccfit.pleshkov.notebook.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val CREATE_NOTES_TABLE = """
    CREATE TABLE ${NotesDBContract.TABLE_NAME} (
    ${NotesDBContract._ID} INTEGER PRIMARY KEY,
    ${NotesDBContract.COLUMN_NAME_TITLE} TEXT NOT NULL,
    ${NotesDBContract.COLUMN_NAME_TEXT} TEXT NOT NULL,
    ${NotesDBContract.COLUMN_NAME_TIME_CREATED} INTEGER NOT NULL,
    ${NotesDBContract.COLUMN_NAME_TIME_UPDATED} INTEGER NOT NULL,
    ${NotesDBContract.COLUMN_NAME_TIME_TO_DO} INTEGER NOT NULL,
    ${NotesDBContract.COLUMN_NAME_STATUS} INTEGER NOT NULL,
    ${NotesDBContract.COLUMN_NAME_SET_BY_USER} INTEGER NOT NULL);
    """

private const val DELETE_NOTES_TABLE = """
    DROP TABLE IF EXIST ${NotesDBContract.TABLE_NAME}
    """

private const val DATABASE_VERSION = 1
private const val DATABASE_NAME = "Notes.db"

class NotesDBHelper(
        context: Context
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_NOTES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, old: Int, new: Int) {}
}