package ru.nsu.ccfit.pleshkov.notebook.view

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.coroutines.experimental.*
import ru.nsu.ccfit.pleshkov.notebook.model.NotesDBHelper
import kotlin.coroutines.experimental.CoroutineContext

interface JobHolder {
    val job: Job
}

val View.contextJob: Job get() = (context as? JobHolder)?.job ?: NonCancellable

abstract class BaseDatabaseActivity : AppCompatActivity(), JobHolder {
    protected abstract val LAYOUT_ID: Int

    override val job = Job()

    protected lateinit var dbHelper: NotesDBHelper
    protected lateinit var writableDb: SQLiteDatabase
    protected lateinit var readableDb: SQLiteDatabase

    protected lateinit var initDatabaseTask: Deferred<Unit>

    protected fun <T> jobAsync(
            context: CoroutineContext = DefaultDispatcher,
            start: CoroutineStart = CoroutineStart.DEFAULT,
            block: suspend CoroutineScope.() -> T
    ) = async(job + context, start, block)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)

        dbHelper = NotesDBHelper(this)

        initDatabaseTask = jobAsync {
            writableDb = dbHelper.writableDatabase
            readableDb = dbHelper.readableDatabase
        }
    }

    protected fun startActivityAnimated(intent: Intent) {
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onDestroy() {
        job.cancel()
        dbHelper.close()
        super.onDestroy()
    }
}
